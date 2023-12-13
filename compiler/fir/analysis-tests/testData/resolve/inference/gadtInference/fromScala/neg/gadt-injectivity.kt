// WITH_STDLIB

sealed class EQ<A, B>
class Refl<A> : EQ<A, A>()

fun <A, B, C, D> conform(a: A, b: B, eq: EQ<Pair<A, B>, Pair<C, D>>): C {
    return when (eq) {
        is Refl<*> -> {
            val ab: Pair<A, B> = Pair(a, b)
            val cd: Pair<C, D> = ab
            val bb: Pair<B, B> = <!INITIALIZER_TYPE_MISMATCH!>ab<!> // error
            val cc: Pair<C, C> = <!INITIALIZER_TYPE_MISMATCH!>cd<!> // error
            val dd: Pair<D, D> = <!INITIALIZER_TYPE_MISMATCH!>cd<!> // error
            a
        }
    }
}
