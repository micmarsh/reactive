# Subscriptions

# Two kinds of Observables
* "Hot": same source, shared by all subscribers, independent of subscribers
* example^ in lecture: lots of subscriptions to some UI events
* "Cold": each subscriber has own private source
* example^ earthquakes from above: doesn't make network requests and do stuff unless someone is subscribed

# Unsubscribe != Cancellation
* may be other observers
* cancel means stop underlying computation
* were going to be sloppy anyway and use them interchangably

# Subscriptions
* base trait: only has unsubscribe
* boolean sub.: get isUnsubscribed
* composite subscription: has += and -=, like fully mutatable version of dual sub from assignment 3
* multiple assignment subscription: a proxy for mutating (replacing) a single subscription, (like "Refs" in clojure?)

# Example of Subs
* composite: what if you -= a new one after you've unsubbed it?
* same question for multiple assingment, answer for both is it is gets unsubbed right as its subscribed
* unsubbing inner makes composite/multiple wrapper still "subscribed", makes sense

In general, lecture was easy, since only "rules" to learn were clear: the 3 bullet points from directly above
