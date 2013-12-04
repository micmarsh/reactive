# Ops on Observables

# flatMap vs. Map
* flatMap makes new streams and pushed them into new, but has no control over order of sequence when cat'ing streams. (i.e. what you'd think would be [[D, S] [D, S] [D, S]] -> [D, S, D, S, D, S] could end up " -> [D, D, S, D, S, S] or whatever)
* "flatten" -> "non-derministic merge"
## Define flatten: so how do we merge two streams?
* Marble diagram is cool, (out of order), but have to watch out of stream end/error, need to propogate to output and terminate once that's cool for both
* concat v. flatten: concat apparently preserves order
* concat could require arbitrary buffering, no good

# Example: Earthquake Magnitudes
* nothing too shocking so far, Enum class is quite Scala
* Future + Observable example: look out for (country: Future).map, variable inside of lambda is actually a country
* flatten v. concat: prolly want concat b/c order, geocoordinate lookup is async
* slower b/c of slowest, but correct output stream
# groupBy: Observable[T] => Observable[(K, Observable[T])]
* would be super dope if you could convert this shit into a map that returns Futures or Observables
