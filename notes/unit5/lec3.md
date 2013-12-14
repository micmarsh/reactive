# Message Processing

As we know, the only way to deal with actors is messages.

"Single Threaded"
* receives message one after another, blocks (or at least doesn't process any others) while processing message

## Bank Account Example
* good practice to define case classes inside and actor's companion object
object BankAccount {
    case class Deposit(amount:BigInt) {
        require(amount > 0)
    }
    case class Withdraw(amount:BigInt) {
        require(amount > 0)
    }
    case object Done
    case object Failed
}

class BankAccount extends Actor {
    import BankAccount._

    var balance = BigInt(0)

    def receive = {
        case Deposit(amount) balance += amount; sender ! Done
        case Withdraw(amount) ...
        case _ => sender ! Failed
    }
}

class WireTransfer extends Actor {
    def receive = {
        case Transfer(from, to, amount) =>
            from ! BankAccount.Withdraw(amount)
            context.become(awaitWithdraw(to, amount, sender))
    }

    def awaitWithdraw(to, amount, sender) ={
        to ! BankAccount.Deposit(amount)
        context.become(signalDone(sender))
    }
    //this was your guess, but it's too simple. Here's what it actually is
    {
        case Done =>
            to ! BankAccount.Deposit(amount)
            context.become(awaitDeposit(sender))
        case Failed =>
            sender ! Failed // let the original sender know things went wrong
            context.stop(self)
    }

    def awaitDeposit(client) = {
        case Done =>
            client ! Done
            context stop self
    }
}

Thought/Concern what if we context.become something, and some other completely unrelated actor sends over Transfer or somthing? Is it somehow enqueued until current context.becoming is finished or whatever?

## Message Delivery Guarantee
* All communication inherently unreliable (same with synchronous method call, computer/program could crash, whatever)
* example of a guarantee, someone says "please do!", confirmation is "okay"
* This is prolly why we've got all of these Failure and Done case objects
* Messages can be persisted, get unique ids, retried until successful. There's nothing about this that methods don't have, but messages seem kind of built around these things
* It's actually super important for processors to send back acknowledgment

## Message Ordering
* Order is preserved if an actor sends multiple messages to the same location (this is Akka-specific)
* This is *not* true if an actor sends messages to multiple locations, shit will just be concurrent.
* This^ makes sense b/c that would be a tangled mess of state and the exact opposite of distributed
