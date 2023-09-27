fun <T> transform(v: T): T = TODO()
fun <T> consume(v: T): Unit = TODO()
fun <T> produce(): T = TODO()

interface Box<T>
interface BoxInt : Box<Int>
class BoxIntImpl : BoxInt

fun <T> `while condition inference`(): T {
    var v1 = produce<Box<T>>()
    while (v1 !is BoxInt) {
        TODO()
    }
    return 1
}

fun <T> `while condition inference with temporal value`(v: Box<T>): T {
    while (transform(v) !is BoxInt) {
        TODO()
    }
    return 1
}

fun <T> `interface eq inference`(v: Box<T>, v1: BoxInt): T {
    if (v1 == v) {
        return <!RETURN_TYPE_MISMATCH!>1<!>
    }
    TODO()
}

fun <T> `interface eq inference with temporal rvalue`(v: Box<T>, v1: BoxInt): T {
    if (v1 == transform(v)) {
        return <!RETURN_TYPE_MISMATCH!>1<!>
    }
    TODO()
}

fun <T> `interface eq inference with temporal lvalue`(v: Box<T>, v1: BoxInt): T {
    if (transform(v1) == v) {
        return <!RETURN_TYPE_MISMATCH!>1<!>
    }
    TODO()
}

fun <T> `interface eq inference with temporal lvalue and rvalue`(v: Box<T>, v1: BoxInt): T {
    if (transform(v1) == transform(v)) {
        return <!RETURN_TYPE_MISMATCH!>1<!>
    }
    TODO()
}

fun <T> `refeq inference`(v: Box<T>, v1: BoxInt): T {
    if (v1 === v) {
        return 1
    }
    TODO()
}

fun <T> `refeq inference with temporal rvalue`(v: Box<T>, v1: BoxInt): T {
    if (v1 === transform(v)) {
        return 1
    }
    TODO()
}

fun <T> `refeq inference with temporal lvalue`(v: Box<T>, v1: BoxInt): T {
    if (transform(v1) === v) {
        return 1
    }
    TODO()
}

fun <T> `refeq inference with temporal lvalue and rvalue`(v: Box<T>, v1: BoxInt): T {
    if (transform(v1) === transform(v)) {
        return 1
    }
    TODO()
}


fun <T> `final class eq inference`(v: Box<T>, v1: BoxIntImpl): T {
    if (v1 == v) {
        return 1
    }
    TODO()
}

fun <T> `final class eq inference with temporal rvalue`(v: Box<T>, v1: BoxIntImpl): T {
    if (v1 == transform(v)) {
        return 1
    }
    TODO()
}

fun <T> `final class eq inference with temporal lvalue`(v: Box<T>, v1: BoxIntImpl): T {
    if (transform(v1) == v) {
        return 1
    }
    TODO()
}

fun <T> `final class eq inference with temporal lvalue and rvalue`(v: Box<T>, v1: BoxIntImpl): T {
    if (transform(v1) == transform(v)) {
        return 1
    }
    TODO()
}
