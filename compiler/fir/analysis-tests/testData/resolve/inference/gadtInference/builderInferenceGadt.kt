// WITH_STDLIB

fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

interface Inv<T>
interface InvInt : Inv<Int>

fun <T> `gadt - builder inference before`() = buildList {
    add(produce<Inv<T>>())
    if (this[0] is InvInt) {
        consume<T>(produce<Int>())
        consume<Int>(produce<T>())
    }
}

fun <T> `gadt - builder inference after`() = buildList {
    if (this[0] is InvInt) {
        consume<T>(produce<Int>())
        consume<Int>(produce<T>())
    }
    add(produce<Inv<T>>())
}

fun <T> `gadt - builder inference consume before`() = buildList {
    consume<Inv<T>>(this[1])
    if (this[0] is InvInt) {
        consume<T>(produce<Int>())
        consume<Int>(produce<T>())
    }
}

fun <T> `gadt - builder inference consume after`() = buildList {
    if (this[0] is InvInt) {
        consume<T>(produce<Int>())
        consume<Int>(produce<T>())
    }
    consume<Inv<T>>(this[1])
}

