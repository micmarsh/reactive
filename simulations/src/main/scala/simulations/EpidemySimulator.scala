package simulations

import math.random

class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8
    
    val prevalence = 0.01
    val transmisability = 0.4

  }

  import SimConfig._

  val persons: List[Person] = {
    val people = for (i <- (0 until population)) yield new Person(i) 
    var infected = 0
    val maxToInfect = (population * prevalence).toInt
    while (infected < maxToInfect ) {
      var toInfect = people(randomBelow(population))
      if (!toInfect.infected){
        toInfect.infected = true
        infected += 1
      }
    }
    people.toList
  }
  
  class Person (val id: Int) {
    var infected = false
    var sick = false
    var immune = false
    var dead = false

    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    def elminateRooms(rooms: Vector[(Int, Int)]) ={
      val result = if(dead) Vector[(Int, Int)]()
      else rooms
      //TODO: eliminate rooms with vis infectious, handle deadness
      // edges of map should wrap around world
      result
    }
    
    def postMove():Unit = {
      //TODO: get infected if relevant
      //add next move <- this is fine if stuff before can handle deadness and stuff
    }
    
    def move():Unit = {
      val adjacent = Vector((row, col+1),(row+1, col), (row-1, col), (row, col-1))
      val choices = elminateRooms(adjacent)
      if(choices.length > 0){
	      val (newRow, newCol) = choices(randomBelow(choices.length))
	      row = newRow
	      col = newCol
      }
      postMove()
    }
    
    def addNextMove() {
      val when = randomBelow(4) + 1
      afterDelay(when)(move)
    }
    
    addNextMove() //start things off
    //
    // to complete with simulation logic
    //
  }
 
}
