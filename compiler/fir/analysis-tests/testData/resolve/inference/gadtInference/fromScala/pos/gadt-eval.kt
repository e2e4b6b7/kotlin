// WITH_STDLIB

sealed class Exp<T>
class Lit(val value: Int) : Exp<Int>()
class PairExp<A, B>(val fst: Exp<A>, val snd: Exp<B>) : Exp<Pair<A, B>>()

fun <T> eval(e: Exp<T>): T = when (e) {
    is Lit -> e.value
    is PairExp<*, *> -> {
        fun <A, B> evalPair(p: PairExp<A, B>): Pair<A, B> = Pair(eval(p.fst), eval(p.snd))
        evalPair(e)
    }
}
