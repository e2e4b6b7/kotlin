// WITH_STDLIB

sealed class EQ<A, B> {
    class Refl<T> : EQ<T, T>()
}

typealias FooB<X> = Pair<X, X>

fun <X, Y> fooB(x: X, eq: EQ<FooB<X>, FooB<Y>>): Y = when (eq) {
    is EQ.Refl<*> -> x
}

typealias FooD<X> = Pair<Int, Int>

fun <X, Y> fooD(x: X, eq: EQ<FooD<X>, FooD<Y>>): Y = <!RETURN_TYPE_MISMATCH!>when (eq) {
    is EQ.Refl<*> -> x // error
}<!>
