// TODO: all of this functions or their calls shoud fail to typecheck

open class Box<T>(val v: T)
class BoxBox<T>(v: Box<T>) : Box<Box<T>>(v)

interface A
interface B
interface C : A, B
interface D : C
interface E : C

fun <V> unsound1(box1: Box<V>): V = <!RETURN_TYPE_MISMATCH!>when (box1) {
    is BoxBox<*> -> Box(object : B {})
    else -> box1.v
}<!>

fun <V> unsound1_(box1: Box<V>): V {
    when (box1) {
        is BoxBox<*> -> return Box(object : B {})
        else -> return box1.v
    }
}

fun <V> unsound2(box1: Box<V>, box2: BoxBox<*>): V {
    if (box1 === box2) {
        return Box(object : B {})
    }
    return box1.v
}

fun <V> unsound3(box1: Box<V>, box2: BoxBox<in C>): V {
    if (box1 === box2) {
        return Box(object : B {})
    }
    return box1.v
}

fun <V> unsound4(box1: Box<V>, box2: BoxBox<out C>): V {
    if (box1 === box2) {
        return Box(object : E {})
    }
    return box1.v
}

fun classCastException() {
    val bba: BoxBox<A> = BoxBox(Box(object : A {}))
    val bbd: BoxBox<D> = BoxBox(Box(object : D {}))

    val ba1: Box<A> = unsound1(bba)
    val a1: A = ba1.v
    val ba1_: Box<A> = unsound1_(bba)
    val a1_: A = ba1.v
    val ba2: Box<A> = unsound2(bba, bba)
    val a2: A = ba2.v
    val ba3: Box<A> = unsound3(bba, bba)
    val a3: A = ba3.v
    val bd4: Box<D> = unsound4(bbd, bbd)
    val d4: D = bd4.v
}
