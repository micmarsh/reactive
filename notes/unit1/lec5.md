# Monads!

A type M[T] w/ two ops: flatMap[U](f: T => M[U]): M[U], and unit[T](x: T): M[T]
(flatMap is more commonly called bind)

Fancy proof!

m map f == m flatMap (x => unit(f(x)))
OR m flatMap (f andThen unit), andThen is function comp

properties of monads

left unit
unix(x) flatMap f = f(x)

right unit
opt flatMap unit = opt

Now maybe you can research stuff later?
