package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }
  
  property("delete min") = forAll { a: Int =>
    val heap = insert(a, empty)
    val newEmpty = deleteMin(heap)
    newEmpty == empty
  }
  
  
  property("min of 2") = forAll { pair: (Int, Int) =>
    val (first, second) = pair
    val heap = insert(second,insert(first, empty))
    if (first < second) findMin(heap) == first
    else  findMin(heap) == second
  }
  
  def buildList(heap: H): List[Int] = {
    def loop(acc:List[Int], heap: H): List[Int] =
      if (heap == empty) acc.reverse
      else {
        val min = findMin(heap)
        loop(min::acc, deleteMin(heap))
      }
    loop(Nil, heap)
  }
  
  def isSorted(list: List[Int]): Boolean = {
    def matchAndCheck(soFar: Boolean, list: List[Int]):Boolean = {
      list match {
        case Nil => soFar
        case head::Nil => soFar
        case first::second::tail => {
          if(first > second) false
          else matchAndCheck(true, second::tail)
        }
      }
    }
    matchAndCheck(true, list)
  }
  
  property("build a sorted list") = forAll { heap: H =>
       val sortedList = buildList(heap)
       isSorted(sortedList)
  }
  

  lazy val genHeap: Gen[H] = for {
	  int <- arbitrary[Int]
	  heap <-  oneOf(value(empty), genHeap)
  }  yield insert(int, heap)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
