fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

interface Eq<A, B>
class ReflEq<T> : Eq<T, T>

fun <A, B> `relate eq`(eq: Eq<A, B>) {
    if (eq is ReflEq<*>) {
        consume<A>(produce<B>())
        consume<B>(produce<A>())
    }
}

class Foo<T>

fun <X, Y> foo(x: X, eq: Eq<Foo<X>, Foo<Y>>): Y = when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}

fun <X, Y> `foo false out x`(x: X, eq: Eq<Foo<out X>, Foo<Y>>): Y = when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}

fun <X, Y> `foo false out y`(x: X, eq: Eq<Foo<X>, Foo<out Y>>): Y = when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}

fun <X, Y> `foo out x`(x: X, eq: Eq<out Foo<out X>, Foo<Y>>): Y = <!RETURN_TYPE_MISMATCH!>when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}<!>

fun <X, Y> `foo out y`(x: X, eq: Eq<Foo<X>, out Foo<out Y>>): Y = when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}

typealias FooInt<X> = Int

fun <X, Y> foo2(x: X, eq: Eq<FooInt<X>, FooInt<Y>>): Y = <!RETURN_TYPE_MISMATCH!>when (eq) {
    is ReflEq<*> -> x
    else -> TODO()
}<!>

interface Sub<A, out B>
class ReflSub<T> : Sub<T, T>

fun <A, B> `relate sub`(sub: Sub<A, B>) {
    if (sub is ReflSub<*>) {
        consume<A>(<!ARGUMENT_TYPE_MISMATCH!>produce<B>()<!>)
        consume<B>(produce<A>())
    }
}

fun <A, B> `relate sub with temporal value`(sub: Sub<A, B>) {
    if (transform(sub) is ReflSub<*>) {
        consume<A>(<!ARGUMENT_TYPE_MISMATCH!>produce<B>()<!>)
        consume<B>(produce<A>())
    }
}

fun <A, B, C, D> `complex relation`(eq: Eq<A, B>, sub: Sub<B, C>, eq2: Eq<C, D>) {
    if (eq is ReflEq<*> && sub is ReflSub<*> && eq2 is ReflEq<*>) {
        consume<A>(<!ARGUMENT_TYPE_MISMATCH!>produce<D>()<!>)
        consume<D>(produce<A>())
    }
}
