fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

interface Box<T>
interface BoxInt : Box<Int>
interface BoxString : Box<String>
interface Box2<T, V>
interface NestedBox1<T> : Box<Box<Box2<Int, T>>?>
interface NestedBox2<T> : Box<Box<Box2<T, Int>>?>

fun <T> `box basic test`(v: Box<T>): T = when (v) {
    is BoxInt -> 1
    is BoxString -> "1"
    else -> TODO()
}

fun <T> `box basic invalid test`(v: Box<T>): T = when (v) {
    is BoxInt -> 1
    is BoxString -> "1"
    else -> TODO()
}

fun <T> `box basic test multiple return`(v: Box<T>): T {
    when (v) {
        is BoxInt -> return 1
        is BoxString -> return "1"
        else -> TODO()
    }
}

fun <T> `box references test`(v: Box<T>): T = when (v) {
    is BoxInt -> produce<Int>()
    is BoxString -> produce<String>()
    else -> TODO()
}

fun <T> `box consume test`(v: Box<T>): Unit = when (v) {
    is BoxInt -> consume<Int>(produce<T>())
    is BoxString -> consume<String>(produce<T>())
    else -> TODO()
}

fun <T> `box without variable test`(v: Box<T>): T = when (transform(v)) {
    is BoxInt -> {
        consume<Int>(produce<T>())
        1
    }
    is BoxString -> {
        consume<String>(produce<T>())
        "1"
    }
    else -> TODO()
}

fun <T, V> `nested arguments test`(v: Box<Box<Box2<T, V>>?>): Box2<T, V> =
    when (v) {
        is NestedBox1<*> -> when (v) {
            is NestedBox2<*> -> {
                consume<Box2<Int, Int>>(produce<Box2<T, V>>())
                produce<Box2<Int, Int>>()
            }
            else -> TODO()
        }
        else -> TODO()
    }

fun <T, V> `nested type arguments if test`(v: Box<Box<Box2<T, V>>?>): Box2<T, V> =
    if (v is NestedBox1<*> && v is NestedBox2<*>) {
        consume<Box2<Int, Int>>(produce<Box2<T, V>>())
        produce<Box2<Int, Int>>()
    } else TODO()

fun <T, V> `nested type arguments without variable if test`(v: Box<Box<Box2<T, V>>?>): Box2<T, V> =
    if (v is NestedBox1<*> && transform(v) is NestedBox2<*>) {
        consume<Box2<Int, Int>>(produce<Box2<T, V>>())
        produce<Box2<Int, Int>>()
    } else TODO()
