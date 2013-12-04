# Futures -> Observables

## Future[T] and Try[T] are "dual"
* The "essence" of a Future: (Try[T] => Unit) => Unit
* "reverse and flip the arrows" -> (Unit => (Unit => Try[T])), then simplify to () => (() => Try[T]), then to Try[T], b/c "() =>" only represents side effects
* "Dual" comes from category theory

## Iterable[T]
* trait Iterable {def iterator()}
* Iteratable has flatMap!
* still synchronous, so reading from files (for example), not so good

## Iterable/Iterator to Observables
* Model Iterator: {def hasNext(): Boolean, def next(): T}, as () => Try[Option[T]], options represents next + hasNext since next may return T, and Try accounts for exception in the case where next excepts
* simplify that^: first "flip the arrows": (Try[Option[T]]=>Unit)=>Unit, replace with pattern matching possiblities: (T => Unit, Throwable => Unit, () => Unit) => Unit
* complicate this^:
    trait Observable[T] {def Subscribe: Observer[T] => Subscription (just like in assignment)}
    trait Observer[T] {def onNext(v: T): Unit, def onError(error:Throwable):Unit, def onCompleted():Unit}
* observable (Try[Option[T]] => Unit) => Unit is the same as Future except for Try[T -> Option[T]]
