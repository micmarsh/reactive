# Fn's and Pattern Matching

## Example: Case classes and JSON

* Case classes: Base is JSON, define JSeq, JObj, JNum, etc.

* Had not at all thought of this example, even after OO in previous class. Good to couple data format with classes?

## Case Blocks

* The type of {case (key, value) => key + ": " + value} ?
* If not plugged into higher order function, can't be typed
* JBinding => String, where type JBinding = (String, JSON)
*   "      =>   "  is also equivalent of scala.Function1[JBinding, String]
* block above expands to:
    new Function1[JBinding, String] {
        def apply(x: Binding) = x match {
            case (key, value) => key + ": " + show(value)
                                    // "show" is closed over!
        }
    }

## Functional Traits/Subclassing Functions

* Map extends Function1, as does Seq! That's why you can do blah("key") for maps or blah(1) for indexing seqs. Cool stuff!

* type sigs: trait Map[T, U] extends (T => U) and trait Seq[T] extend Int => T

## Partial Matches

* val f: String => String {case "ping" => "pong"},
* f("pong") //MatchError!
* val f: PartialFunction[String, String]
* defines f.isDefinedAt
* above expands to
    new PartialFunction[String,String] {
        ...blah apply blah...
        def isDefinedAt(x: String) = x match {
            case "ping" => true
            case _ => false
        }
    }

* Last Exercise (nested pattern match which would throw exception): returns true! Why?
* isDefinedAt only applies to outermost pattern match block ( static analysis is  not as awesome as you thought)
* kind of makes sense, since thing inside block could throw an array index exception or whatever, example is only coincidentally includes a nested pattern match
