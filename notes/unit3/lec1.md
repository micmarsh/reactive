# Monads and Effects

* Throwing exceptions gets messy, even (especially?) with java forced try/catch block

* Instead! A try Monad! Try[S], fn T => S to T => Try[S]

* In failure pattern match: "case failure @ Failure(t) => failure", huh?
* Given stuff from later in lecture, that^ must mean, "pattern match on the thing to the right of @, but we want to give a new name to the thing being matched for clarity"

# Monads Guide You Through the Happy Path
* Practical application: Option, Try, (or IO), things that make risky things explicit
* Flatmap can be used to automatically propogate exceptions instead of pattern matching, I guess T and Throwable are somehow related (also for comps)

# Implementation of Things
* Definition of Try in Lecture helps clear up above a little, given explicit exception handling of Success[T], but still doesn't really explain how you're passing in =>T,
