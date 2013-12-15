/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor._
import scala.collection.immutable.Queue

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection*/
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply
  
  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}


class BinaryTreeSet extends Actor {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = { 
    case op: Operation => {
      //println("op it up")
      root ! op
    }
    case GC => {
      //println("***********Starting to GC***************")
      val newRoot = createRoot
      context become garbageCollecting(newRoot) 
      root ! CopyTo(newRoot)
    }
  }

  // optional
  /** Handles messages while garbage collection is performed.
    * `newRoot` is the root of the new binary tree where we want to copy
    * all non-removed elements into.
    */
  def garbageCollecting(newRoot: ActorRef): Receive = {
    case op: Operation => pendingQueue = pendingQueue enqueue op
    case CopyFinished => {
      while(!pendingQueue.isEmpty) {
        val (op, newQueue) = pendingQueue.dequeue
        newRoot ! op
        pendingQueue = newQueue
      }
      root = newRoot
      context become normal
    }
  }

}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode],  elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = { 
    case Insert(requestor, id, newElem) => handleInsert(requestor, id, newElem)
    case Contains(requestor, id, checkElem) => handleContains(requestor, id, checkElem)
    case Remove(r, id, e) => handleRemove(r, id, e)
    case copy:CopyTo => {
        val actors = subtrees.values.toSet
        context become copying(actors, false)
        val CopyTo(tree) = copy
        
        if (removed)
          self ! OperationFinished(1)
        else 
          tree ! Insert(self, 1, elem)

        if (actors.isEmpty)
          self ! CopyFinished
        else
          actors foreach (_ ! copy)
        
    }
   }
  
  
  
  def handleInsert(requestor:ActorRef, id:Int, newElem:Int) = {
    def insertIntoSide(side: Position) = subtrees get side match {
        case Some(node) => node ! Insert(requestor, id, newElem)
        case None => 
          subtrees = subtrees + (side -> context.actorOf(BinaryTreeNode.props(newElem, false)))
          requestor ! OperationFinished(id)
      }
    if (newElem == elem) {
      removed = false
      requestor ! OperationFinished(id)
    } else if (newElem > elem) {
      insertIntoSide(Right)
    } else {
      insertIntoSide(Left)
    }
  }
  
  def handleContains(requestor: ActorRef, id: Int, checkElem: Int) = {
    def checkSide(side: Position) = subtrees get side match {
        case Some(node) => node ! Contains(requestor, id, checkElem)
        case None => requestor ! ContainsResult(id, false)
      }
    if (elem == checkElem)
      requestor ! ContainsResult(id, !removed)
    else if (checkElem > elem)
      checkSide(Right)
    else 
      checkSide(Left)
  }
  
   def handleRemove(requestor: ActorRef, id: Int, checkElem: Int) = {
    def removeSide(side: Position) = subtrees get side match {
        case Some(node) => node ! Remove(requestor, id, checkElem)
        case None => requestor ! OperationFinished(id)
      }
    if (elem == checkElem){
      removed = true
      requestor ! OperationFinished(id)
    } else if (checkElem > elem)
      removeSide(Right)
    else 
      removeSide(Left)
  }
  

  // optional
  /** `expected` is the set of ActorRefs whose replies we are waiting for,
    * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
    */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    case fin:OperationFinished => {
      if (expected.isEmpty){
        context.parent ! CopyFinished
        context become normal 
      } else
    	 context become copying(expected, true)
    }
    case CopyFinished =>
      val newSet = expected - context.sender
      if (newSet.isEmpty && insertConfirmed){
        context.parent ! CopyFinished
        context become normal
      } else
    	context become copying(newSet, insertConfirmed)

  }

}
