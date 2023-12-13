sealed class Foo<T>
class Bar1 : Foo<Int>()
class Bar2 : Foo<String>()
class Bar3 : Foo<Any>()

fun <T> fail1(x: Foo<T>, y: Foo<T>) {
    when {
        x is Bar1 && y is Bar1 -> {}
        x is Bar1 && y is Bar2 -> {
            // TODO: Should be an Error or Warning (Unreachable code)
            // (Not covered by initial KEEP, but may be done in th future)
        }
        else -> {}
    }
}
