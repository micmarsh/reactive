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
        toInfect.infectMe
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

    
    //TODO move a lot of this shit out of person class, maybe keep only mutators in here
    type Rooms = Vector[(Int, Int)]
    
    def locations(personCheck: Person => Boolean)= {
      persons.withFilter(personCheck).map((p:Person) => (p.row, p.col))
    }
    
    def elminateRooms(rooms:Rooms):Rooms = {
      if(dead) Vector[(Int, Int)]()
      else {
        val sickLocations = locations(_.sick)
        //should be cool cause deadsys stay sick
        val wrapped = rooms.map{case (r, c) => (if (r >= roomRows) r - roomRows else r,
        										if ( c >= roomColumns) c - roomColumns else c)}
        val noSick = wrapped.diff(sickLocations)
        
        noSick
      }
    }
    
    def infectMe:Unit = {
      infected = true
      afterDelay(6) {sick = true}
      afterDelay(14) {if(random < 0.25) dead = true}
      afterDelay(16) {
        if (!dead){
          immune = true
          sick = false
        } 
      }
      afterDelay(18) {
        if(!dead) {
          immune = false
          infected = false
        }
      }
    }
    
    def maybeInfectMe: Unit = {
      if (random < transmisability) infectMe
    }
    
    def postMove:Unit = {
      if(!infected && !immune && locations(_.infected).exists( _ == (row, col)))
        maybeInfectMe
      if (!dead) //checking deadness in two places  
    	 addNextMove
    }
    
    def makeMove(choices: Rooms) {
		  val (newRow, newCol) = choices(randomBelow(choices.length))
		  row = newRow
		  col = newCol
    }
    
    def move:Unit = {
      val adjacent = Vector((row, col+1), (row+1, col), (row-1, col), (row, col-1))
      val choices = elminateRooms(adjacent)
      if(choices.length > 0) makeMove(choices)
      postMove
    }
    
    def addNextMove: Unit = {
      val when = randomBelow(4) + 1
      afterDelay(when)(move)
    }
    
    addNextMove //start things off
  }
 
}
