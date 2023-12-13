// WITH_STDLIB

sealed interface EQ<A, B>
class Refl<A> : EQ<A, A>

fun <A, B, C, D> m(e1: EQ<A, Pair<B, C>>, e2: EQ<A, Pair<C, D>>, d: D): A = when (e1) {
    is Refl<*> -> when (e2) {
        is Refl<*> -> {
            val r1: Pair<B, B> = Pair(d, d)
            val r2: Pair<C, C> = r1
            val r3: Pair<D, D> = r1
            r1
        }
    }
}

fun <Z, A, B, C, D> m2(e0: EQ<Z, A>, e1: EQ<A, Pair<B, C>>, e2: EQ<Z, Pair<C, D>>, d: D): Z = when {
    e0 is Refl<*> && e1 is Refl<*> && e2 is Refl<*> -> {
        val r1: Pair<B, B> = Pair(d, d)
        val r2: Pair<C, C> = r1
        val r3: Pair<D, D> = r1
        r1
    }
    else -> TODO()
}
