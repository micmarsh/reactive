# Designing Actor Systems

 Think of it as dividing a task amongst people, except they're disposable

 ## Example
 Download the content of a URL, extract and follow links, recur until a max depth

client - Get(url) -> receptionist (then will send Result(links, url) to client)
                        |
                       Check(url, depth)
                       V
                    controller (then will send Result(links) back to recep)
                    |    ^
                Get(url) Links/Done
                    V    |
                     Getter (many of these)

We don't want to use a wasteful synchronous method, use scala async future to wrap stuff up

# Futures + Actors

future onComplete {
    case Success(body) => self ! body
    case Failure(err) => self ! Status.Failure(err)
}
// this is how you get things out of a future and into an actor's flow.
// this is so common, Akka uses implicit class magic to make

future.pipeTo(self)

//here is where you're piping that data to:

def receive = {
    case body: String =>
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      stop()
    case _: Status.Failure => stop()
}

def stop(): Unit {
  context.parent ! Done
  context.stop(self)
}

Akka's ActorLogging sends a message to another actor with the debug output, so that IO doesn't block important things

According to controller example, a parent should manually keep track of its children

U can use context.system.scheduler to do timeouts system-wide, but U shouldn't do that with just a partial function, instead have scheduler send a message to an Actor that does your shit

Similar problems with Futures. Moral of the story, only send messages to do anything. Another problem with this kind of concurrency: watch out of accessing sender, b/c its global and shitty (jk, mostly). Assign sender to a local var to deal with this.

Also about futures: set the dispatcher/execution context (the thing you never care about) of your Actor's and your other async stuff to be the same


Keeping with the theme of keeping trackk of yo kids, the Receptionist in the example has to maintian to queue of running jobs
