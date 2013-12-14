# Intro of Actors

## Formal Def
An Actor is:
* an object with "identity"
* has a behavior (side effect?)
* async only interacts (with other actors?) w/ async message passing

trait Actor {
    def receive: PartialFunction[Any, Unit]
    // the least type safe
}

//useless example
class Counter extends Actor {
    var count = 0
    def receive = {
        case "incr" => count += 1
    }
}

//more useful
//useless example
class Counter extends Actor {
    var count = 0
    def receive = {
        case "incr" => count += 1
        case ("get", customer: ActorRef) => customer ! count
                                    //customer "til" count
    }
    def sender: ActorRef
    // this gets address of currently processed message
}

! operator has implicit sender arg, so always sends actor's own address along with message

moar traits

trait ActorContext {
    def become(behavor: Receive ), discardOld: Boolean = true):Unit
    //WTF a receive is? oh, it's a PartialFunction[Any, Unit]
}

so....

class Counter extends Actor {
    var count = 0
    def counter(n: Int):Receive = {
        case "incr" => context.become(counter(n + 1))
        case "get" => sender ! count
                                    //customer "til" count
    }
    def receive = counter()
    // this gets address of currently processed message
}

this doesn't make a whole lot of sense: is become tied to receive? Actually that would makes tons of sense.
This helps clean up variables chillin' in the class, and make state change explicit

Okay, you can dig the last example:
Papa Actor makes a baby actor, sends incr messages until it sends a get, we know from before Baby responds to get so we see what happens in papa's response fn.
