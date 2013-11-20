package simulations

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5
  
  test("andGate example") {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    in1.setSignal(false)
    in2.setSignal(false)
    run
    
    assert(out.getSignal === false, "and 1")

    in1.setSignal(true)
    run
    
    assert(out.getSignal === false, "and 2")

    in2.setSignal(true)
    run
    
    assert(out.getSignal === true, "and 3")
  }

  
  def orGateTest(gate: (Wire, Wire, Wire) => Unit) ={
    val in1, in2, out = new Wire
    gate(in1, in2, out)
    
    in1.setSignal(false)
    in2.setSignal(false)
    run
    assert(out.getSignal === false, "or 1")

    in1.setSignal(true)
    run
    assert(out.getSignal === true, "or 2")

    in2.setSignal(true)
    run
    assert(out.getSignal === true, "or 3")
    
    in1.setSignal(false)
    run
    assert(out.getSignal === true, "or 4")
    
    in2.setSignal(false)
    run
    assert(out.getSignal === false, "or 5")
  }
  
  test("orGate example") {
	  orGateTest(orGate)
  }
  
  test("orGate2 example") {
     orGateTest(orGate2)
  }
  
  test("super trivial demuxer") {
    val in, out = new Wire
    demux(in, List(), List(out))
    
    in setSignal true
    run
    assert(out.getSignal === true, "true goes thru")
    
    in setSignal false
    run
    assert(out.getSignal === false, "false too")
  }

  test("still pretty trivial demuxer") {
    val in, control = new Wire
    val outs = (for (i <- (0 until 2)) yield new Wire).toList
    demux(in, List(control), outs)
    def checkOuts(out0:Boolean, out1:Boolean) =
      outs(0).getSignal == out0 &&
      outs(1).getSignal == out1
    
    in setSignal false
    run
    assert(checkOuts(false, false), "Everything is turned off")
    
    in setSignal true
    run
    assert(checkOuts(true, false), "redirect to first output")
    
    control setSignal true
    run
    assert(checkOuts(false, true), "redirect to second output")
    
    println(outs map (x => x.getSignal))
    
    in setSignal false
    run
    assert(checkOuts(false, false), "turn everything back off")
    
  }
  
  def tenRandomIntegers() = {
    val upperLimit = 10
    for (i <- (1 to 10))
       yield math.floor(math.random * upperLimit).toInt
  }
  
  def someWires(howMany:Int) = 
    (for (i <- (1 to howMany)) yield new Wire).toList
  
  def makeADemuxer(number:Int) = {
    val in = new Wire
    val controls = someWires(number)
    val numOuts = math.pow(2, number).toInt
    val outs = someWires(numOuts)
    demux(in, controls, outs)
    (in, controls, outs)
  }
  
  test("a demuxer w/ two controls") {
    val numControls = 2
    val (in, controls, outputs) = makeADemuxer(numControls)
    
    assert(outputs.forall(w => !w.getSignal), "everything starts turned off")
    
    in setSignal true
    run
    assert(outputs(0).getSignal === true, "output to 0")
    
    controls(1) setSignal true
    run
    println(outputs.map(x => x.getSignal))
    //problem! flip the first switch and shit goes over to 2 instead of one
    //flip the second instead and see what happens
    //flipping just the second maps it to 1. Hypothesis: shit is fucked up in "the middle"
    //but flipping both or none gets it to the right place
    //okay interesting, flipping first when there's 8 outputs also throws this shit over to 2
    
    //right now focus on the case with 4, b/c it will probably lead to victory
    assert(outputs(2).getSignal === true, "output to 1")
    
    
  }
  
}
