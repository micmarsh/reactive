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
  
  
  
  
  property("min of 2") = forAll { pair: (Int, Int) =>
    val (first, second) = pair
    val heap = insert(second,insert(first, empty))
    if (first < second) findMin(heap) == first
    else  findMin(heap) == second
  }
  

  lazy val genHeap: Gen[H] = ??? //need to wrap your head around this non-oo heap stuff,
  //prolly pretty simple: Heap is just a wrapper so you can do scala-style 00 testing

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
