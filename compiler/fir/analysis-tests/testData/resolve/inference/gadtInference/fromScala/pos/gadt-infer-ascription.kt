// WITH_STDLIB

sealed class Var<G, A>
class Z<G, A> : Var<Pair<A, G>, A>()
class S<G, A, B>(val x: Var<G, A>) : Var<Pair<B, G>, A>()

fun <G, A> evalVar(x: Var<G, A>, rho: G): A = when (x) {
    is Z<*, A> -> rho.first
    is S<*, A, *> -> {
        fun <G1, B> evalVarS(x1: S<G1, A, B>, x2: Var<G, A>, rho: G): A {
            require(x1 === x2)
            return evalVar(x1.x, rho.second)
        }
        evalVarS(x, x, rho)
    }
}
