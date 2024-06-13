// WITH_STDLIB

interface TagA<A>
interface TagB<B>
interface TagC<C>
interface TriTag<A, B, C> : TagA<A>, TagB<B>, TagC<C>
open class IntStrCharTag : TagA<Int>, TagB<String>, TagC<Char>

fun <A, B, C> get(it: TriTag<A, B, C>): Triple<A, B, C> =
    when (it) {
        is IntStrCharTag -> Triple(0, "zero", '0')
        else -> TODO()
    }

fun main() {
    val ret = get(object : IntStrCharTag(), TriTag<Int, String, Char> {})
    println(ret)
}
