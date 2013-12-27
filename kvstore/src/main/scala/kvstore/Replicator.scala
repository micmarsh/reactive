package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._

object Replicator {
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)
  
  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}

class Replicator(val replica: ActorRef) extends Actor {
  import Replicator._
  import Replica._
  import context.dispatcher
  
  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, (ActorRef, Replicate)]
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]
  
  var _seqCounter = 0L
  def nextSeq = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }
  
  def receive: Receive = {
    case replicate: Replicate =>
        val Replicate(key, option, id) = replicate
        val seq = nextSeq
    	replica ! Snapshot(key, option, seq)
    	acks += (seq -> (sender, replicate))
    	
    case SnapshotAck(key, seq) =>
       acks get seq match {
         case None =>
         case Some((respondTo, request)) =>
           val Replicate(key, o, id) = request
           acks -= seq
           respondTo ! Replicated(key, id)
       }
  }
  
  val unit = java.util.concurrent.TimeUnit.MILLISECONDS
  val duration = Duration.create(100, unit)
  val delay = Duration.create(0, unit)
  
  context.system.scheduler.schedule(delay, duration) {
    for ((seq, (a, Replicate(key, option, id))) <- acks) {
      replica ! Snapshot(key, option, seq)
    }
  }

}
