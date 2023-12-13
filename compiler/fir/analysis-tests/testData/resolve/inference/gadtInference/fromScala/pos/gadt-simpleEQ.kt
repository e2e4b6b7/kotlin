// WITH_STDLIB

sealed class EQ<A, B>
class Refl<A>(val u: Unit) : EQ<A, A>()

fun <A, B> conform(a: A, eq: EQ<A, B>): B = when(eq) {
    is Refl<*> -> a
}

fun <A, B, C, D> conform2(a: A, b: B, eq: EQ<Pair<A, B>, Pair<C, D>>): Pair<C, D> =
    when (eq) {
        is Refl<*> -> Pair(a, b)
    }
