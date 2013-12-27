package kvstore

import akka.actor.{ OneForOneStrategy, Props, ActorRef, Actor }
import kvstore.Arbiter._
import scala.collection.immutable.Queue
import akka.actor.SupervisorStrategy.Restart
import scala.annotation.tailrec
import akka.pattern.{ ask, pipe }
import akka.actor.Terminated
import scala.concurrent.duration._
import akka.actor.PoisonPill
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy
import akka.util.Timeout

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */
  
  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]
  
  val persistence = context.actorOf(persistenceProps)
  var persisting = Map.empty[Long,(Persist, ActorRef)]
  
  def persist(id: Long, request:Persist, replyTo: ActorRef) {
     persisting += (id -> (request, replyTo))
     persistence ! request   
  }
  
  var cancel:Option[akka.actor.Cancellable] = None
  
  def primaryPersist(id: Long, request:Persist, replyTo: ActorRef) {
	persist(id, request, replyTo)
    //primary only
    val duration = Duration.create(1000, unit)
    
    cancel = Some(context.system.scheduler.scheduleOnce(duration) {
      persisting -= id
      replyTo ! OperationFailed(id)
    })
  }
  
  def persisted(p: Persisted, useOpAck: Boolean) {
      val Persisted(key , id) = p 
	  persisting get id foreach {
	    case (_, replyTo) =>
	      persisting -= id
	      if (useOpAck){
	          println("persisted!!!!!! " + key)
	          if(replicatingIsEmpty(id)) {
	              println("acknowledging op from persisted")
	        	  replyTo ! OperationAck(id)
	        	  cancel foreach (_.cancel)
	          }
	      } else
	    	  replyTo ! SnapshotAck(key, id)
	  }
  } 
  
  val unit = java.util.concurrent.TimeUnit.MILLISECONDS
  val duration = Duration.create(100, unit)
  val delay = Duration.create(0, unit)
  
  context.system.scheduler.schedule(delay, duration) {
    for ((id,(request, _)) <- persisting) {
      persistence ! request
    }
  }
  
  arbiter ! Join

  var replicating = Map.empty[Long, (ActorRef, Set[ActorRef])]
  def replicatingIsEmpty(id: Long):Boolean = {
    replicating get id match {
      case None => true
      case Some((_, set)) => set.isEmpty
    }
  }
  
  def replicated(r:Replicated) {
    val Replicated(key, id) = r
    println("woo about to do replicated stuff "+ id)
    println(replicating.get(id))
    replicating.get(id).foreach{case (replyTo, set) => {
      val newSet = set - sender
//      println("replicated!!!! " + set.size + " -> " + newSet.size)
      if(newSet.isEmpty && persisting.get(id).isEmpty) {
//        println("acknowledging op from replicated")
        replyTo ! OperationAck(id)
        cancel foreach (_.cancel)
      }
      replicating += ( id -> (replyTo, newSet))
    }}
    
  }
  
  def receive = {
    case JoinedPrimary   =>
    	context.become(leader)
    case JoinedSecondary => 
        context.become(replica)
  }

  val leader: Receive = {
    case Insert(key,value, id) =>
      kv += (key -> value)
      
      val option = Some(value)
      
      replicating += (id -> (sender,replicators))
      replicators foreach (_ ! Replicate(key, option, id))
      	
      primaryPersist(id,  Persist(key, option, id), sender)
      
    case Remove(key, id) =>
      kv -= key
      
      replicating += (id -> (sender,replicators))
      replicators foreach (_ ! Replicate(key, None, id))

      primaryPersist(id, Persist(key, None, id), sender)

    case Get(key, id) =>
     val option = kv get key
     sender ! GetResult(key, option, id)
     
    case r:Replicated => replicated(r)
    case r:Replicas => replicate(r)
    case p:Persisted => persisted(p, true)
  }
  
  def replicate(r:Replicas) {
    
      def addNew(replicas:Set[ActorRef]) {
          val newReplicas = replicas -- secondaries.keySet
	      for (replica <- newReplicas) {
	        var replicator = context actorOf Replicator.props(replica)
	        for ((key, value) <- kv)
	          replicator ! Replicate(key, Some(value), 42)
	        secondaries += (replica -> replicator)
	        replicators += replicator
	      }
      }
      
      def removeOld(replicas:Set[ActorRef]) {
        val oldReplicas = secondaries.keySet -- replicas
        for (replica <- oldReplicas) {
          context stop replica 
          secondaries get replica match  {
            case None =>
            case Some(replicator) => 
              context stop replicator
              replicators -= replicator
          }
          secondaries -= replica
        }
      }
    
      val Replicas(replicas) = r
      addNew(replicas)
      removeOld(replicas)
  }

  var seqNum = 0L
  
  val replica: Receive = {
     case Get(key, id) =>
	     val option = kv get key
	     sender ! GetResult(key, option, id)
	     
     case Snapshot(key, option, seq) => {
       if (seq == seqNum) {
         option match {
           case None => kv -= key
           case Some(value) => kv += (key -> value)
         }
         persist(seq, Persist(key, option, seq), sender)
         seqNum += 1
       } else if (seq < seqNum) {
         sender ! SnapshotAck(key, seq)
       }
     }
     
    case p:Persisted => persisted(p, false)
    
  }

}
