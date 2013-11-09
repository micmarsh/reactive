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
  
  property("delete min actually deletes the min") = forAll {heap: H =>
    if (heap != empty) {
	  	val min = findMin(heap)
	  	val newHeap = deleteMin(heap)
	  	if (newHeap != empty)
	  		min != findMin(newHeap)
	  	else true
  	} else true
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
//  
//  property("build a sorted list") = forAll { heap: H =>
//       val sortedList = buildList(heap)
//       isSorted(sortedList)
//  }
  
  property("build a sorted list when u meld") = forAll { heaps: (H, H) =>
    val (heap0, heap1) = heaps
    val newHeap = meld(heap0, heap1)
    val sortedList = buildList(newHeap)
    isSorted(sortedList)
  }	
  
//  property("minimum of two heaps") = forAll {heaps: (H, H) =>
//    	val (heap0, heap1) = heaps
//    	val (min0, min1) = (findMin(heap0), findMin(heap1))
//    	val newHeap = meld(heap0, heap1)
//    	if(newHeap == empty) true
//    	else {
//	    	val singleMin = findMin(newHeap)
//	    	(singleMin == min0) || (singleMin == min1)
//    	}
//    }
//  
// 
//  property("inserted nums get to the right place") = forAll { heapAndNum: (H, Int) =>
//    val (heap, num) = heapAndNum
//    val min = findMin(heap)
//    val insertedHeap = insert(num, heap)
//    if (num <= min) {
//      findMin(insertedHeap) == num
//    } else {
//      findMin(insertedHeap) < num
//    }  
//  }
//  
//  property("meld with empty") = forAll { heap: H =>
//    if (heap != empty) {
//        val min = findMin(heap)
//	    val newHeap = meld(heap, empty)
//	    min == findMin(newHeap)	
//    } else true
//  }
  

  lazy val genHeap: Gen[H] = for {
	  int <- arbitrary[Int]
	  heap <-  oneOf(value(empty), genHeap)
	  isEmpty <- arbitrary[Boolean]
  }  yield if(isEmpty) empty else insert(int, heap)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
