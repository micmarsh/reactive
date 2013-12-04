# Lecture 4: Composing futures

* Already know can use for comps (good reminder of how it works, though)

* But what if control flow?

# Example: a "retry" function
* Recurses w/ a "fallbackTo" to keep executing block until fail.
* He doesn't like resursion, us fold/reduce instead
* fold "solution" seems to sacrifice all the readablity for...?
* now you know how other people feel in the face of you reducing to get a boolean or whatever :-P
* Advised to study it, tho, so maybe important conceptually (it is in fact cool)
* Okay, so that's the point, glad he knows it's unreadable "Great programmers write baby code" - Erik Meijer

# Make Effects Implicit
* make T => Future[S] behave as close to T => S as possible
* scala.async! async{ ... await(f:Future[T])}

* ^ "await" inside an "async" reads a Future into a regular value without blocking
* Biggest catch: can't use await inside try/catch, but that's kind of okay cause now you're using Try monad
* Async/await "baby code" is imperative on the inside, but still a pure function

# The Quiz!
* cannot call f on a Future, so elim first 2.
* The key(s): result of async call, and therefore method, is a Future of whatever's returned at the end of it, so C was no good.

# The Filter Example at the End:
* js -> scala: deferred -> promise, promise -> future???

