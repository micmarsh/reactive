# Promises!

* From lecture before: js -> scala: deferred -> promise, promise -> future

* can just call promise.complete(Success(value) | Failure(t)) instead of p.success or p.error, because types (but those functions helpd)

# Race Example:
* Takes two futures, returns a new one that returns result of first finished Future
* Race could actually be useful as a "timeout with a value" or something

