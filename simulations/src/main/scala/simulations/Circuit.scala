package simulations

import common._

class Wire {
  private var sigVal = false
  private var actions: List[Simulator#Action] = List()

  def getSignal: Boolean = sigVal
  
  def setSignal(s: Boolean) {
    if (s != sigVal) {
      sigVal = s
      actions.foreach(action => action())
    }
  }

  def addAction(a: Simulator#Action) {
    actions = a :: actions
    a()
  }
}

abstract class CircuitSimulator extends Simulator {

  val InverterDelay: Int
  val AndGateDelay: Int
  val OrGateDelay: Int

  def probe(name: String, wire: Wire) {
    wire addAction {
      () => afterDelay(0) {
        println(
          "  " + currentTime + ": " + name + " -> " +  wire.getSignal)
      }
    }
  }

  def inverter(input: Wire, output: Wire) {
    def invertAction() {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) { output.setSignal(!inputSig) }
    }
    input addAction invertAction
  }

  def andGate(a1: Wire, a2: Wire, output: Wire) {
    def andAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) { output.setSignal(a1Sig & a2Sig) }
    }
    a1 addAction andAction
    a2 addAction andAction
  }

  //
  // to complete with orGates and demux...
  //

  def orGate(a1: Wire, a2: Wire, output: Wire) {
    def orAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) { output.setSignal(a1Sig | a2Sig) }
    }
    a1 addAction orAction
    a2 addAction orAction
  }
  
  def orGate2(a1: Wire, a2: Wire, output: Wire) {
    val a11, a21, out0 = new Wire
    inverter(a1, a11)
    inverter(a2, a21)
    andGate(a11,a21,out0)
    inverter(out0, output)
  }

  def demux(in: Wire, c: List[Wire], out: List[Wire]):Unit = {
    def demuxHelper (in: Wire, c: List[Wire], position: List[Char]): Unit = {
      c match {
        case Nil => {
		  val List(out0) = out
		  val dummy = new Wire
		  inverter(in, dummy)
		  inverter(dummy, out0)
        }
        case control::rest => {
	        val (out0, out1) = if(rest.isEmpty) {
		        val actualPos = position.mkString
		        println("position: "+actualPos)
		        val index = Integer.parseInt(actualPos, 2)
		        println("translate to: "+index)
	            (out(index), out(index+1))
	        } else (new Wire, new Wire)
	        val invControl = new Wire
	        
		    inverter(control, invControl)
		    andGate(invControl, in, out0)
		    andGate(control, in, out1)
		    
		    if (!rest.isEmpty) {
		      demuxHelper(out0, rest, '0'::position)
		      demuxHelper(out1, rest, '1'::position)
		    }
        }
      }
    }
    demuxHelper(in, c, List('0'))
  }
}

object Circuit extends CircuitSimulator {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  def andGateExample {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    probe("in1", in1)
    probe("in2", in2)
    probe("out", out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    in1.setSignal(true)
    run

    in2.setSignal(true)
    run
  }

  //
  // to complete with orGateExample and demuxExample...
  //
}

object CircuitMain extends App {
  // You can write tests either here, or better in the test class CircuitSuite.
  Circuit.andGateExample
}
