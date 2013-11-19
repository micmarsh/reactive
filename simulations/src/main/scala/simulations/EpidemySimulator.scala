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
        val noSick = rooms.diff(sickLocations)
        val wrapped = noSick.map{case (r, c) => (if (r >= roomRows) r - roomRows else r,
        										if ( c >= roomColumns) c - roomColumns else c)}
        wrapped
      }
    }
    
    def postMove():Unit = {
      
      if(!infected && !immune && locations(_.infected).exists( _ == (row, col)))
        //TODO: in this case, get infected with appropriate probability
        // if that happens then you've got a while other function with lots of timing duties
        // random note: on that: immune is basically useless, but be sure to use it when appropriate
        // in case its tested for
        
      //TODO: get infected (a whole thing with lots of other events) if relevant
      //add next move <- this is fine if stuff before can handle deadness and stuff
      addNextMove()
    }
    
    def makeMove(choices: Rooms) {
		  val (newRow, newCol) = choices(randomBelow(choices.length))
		  row = newRow
		  col = newCol
    }
    
    def move():Unit = {
      val adjacent = Vector((row, col+1), (row+1, col), (row-1, col), (row, col-1))
      val choices = elminateRooms(adjacent)
      if(choices.length > 0) makeMove(choices)	
      postMove()
    }
    
    def addNextMove() {
      val when = randomBelow(4) + 1
      afterDelay(when)(move)
    }
    
    addNextMove() //start things off
  }
 
}
