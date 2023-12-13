// WITH_STDLIB

// creates type-level "strings" like M[M[M[W]]]
object W
class M<A>

// variable with name A
// Var[W]
// Var[M[W]]
sealed class Var<A>
object VarW : Var<W>()
class VarM<A> : Var<M<A>>()

// \s.e
sealed class Abs<S, E>
class AbsC<S, E>(val v: Var<S>, val b: E) : Abs<Var<S>, E>()

// e1 e2
class App<E1, E2>(val e1: E1, val e2: E2)

// T1 -> T2
class TyFun<T1, T2>(val t1: T1, val t2: T2)

// arbitrary base literal
object Lit

// arbitrary base type
object TyBase

// IN[G, (X, TY)] === evidence that binding (X, TY) is in environment G
// x: ty \in G
sealed class IN<G, P>
class INOne<G, X, TY> : IN<Pair<G, Pair<X, TY>>, Pair<X, TY>>()
class INShift<G0, A, X, TY>(val `in`: IN<G0, Pair<X, TY>>) : IN<Pair<G0, A>, Pair<X, TY>>()

// DER[G, E, TY] === evidence that G |- E : TY
sealed class DER<G, E, TY>
class DVar<G, G0, X, TY>(
    val `in`: IN<Pair<G, G0>, Pair<Var<X>, TY>>
) : DER<Pair<G, G0>, Var<X>, TY>()

class DApp<G, E1, E2, TY1, TY2>(
    val d1: DER<G, E1, TyFun<TY1, TY2>>,
    val d2: DER<G, E2, TY1>
) : DER<G, App<E1, E2>, TY2>()

class DAbs<G, X, E, TY1, TY2>(
    val d1: DER<Pair<G, Pair<Var<X>, TY1>>, E, TY2>
) : DER<G, Abs<Var<X>, E>, TyFun<TY1, TY2>>()

class DLit<G> : DER<G, Lit, TyBase>()

// forall G, a. G |- \x.x : a -> a
fun <G, TY> test1(): DER<G, Abs<Var<W>, Var<W>>, TyFun<TY, TY>> =
    DAbs(DVar(INOne()))

// forall G. G |- \x.x : Lit -> Lit
fun <G> test2(): DER<G, App<Abs<Var<W>, Var<W>>, Lit>, TyBase> =
    DApp(DAbs(DVar(INOne())), DLit())

// TODO: Uncomment, currently cause out of memory
//// forall G, c. G |- \x.\y. x y : (c -> c) -> c -> c
//fun <G, TY> test3(): DER<G, Abs<Var<W>, Abs<Var<M<W>>, App<Var<W>, Var<M<W>>>>>, TyFun<TyFun<TY, TY>, TyFun<TY, TY>>> =
//    DAbs(DAbs(DApp(DVar(INShift(INOne())), DVar(INOne()))))

// evidence that E is a value
sealed class ISVAL<E>
class ISVAL_Abs<X, E> : ISVAL<Abs<Var<X>, E>>()
object ISVAL_Lit : ISVAL<Lit>()

// evidence that E1 reduces to E2
sealed class REDUDER<E1, E2>
class EApp1<E1a, E1b, E2>(
    val ed: REDUDER<E1a, E1b>
) : REDUDER<App<E1a, E2>, App<E1b, E2>>()

class EApp2<V1, E2a, E2b>(
    val isval: ISVAL<V1>,
    val ed: REDUDER<E2a, E2b>
) : REDUDER<App<V1, E2a>, App<V1, E2b>>()

class EAppAbs<X, E, V2, R>(
    val isval: ISVAL<V2>
    // cheating - subst is hard
    // , subst: SUBST[E, X, V2, R]
) : REDUDER<App<Abs<Var<X>, E>, V2>, R>()

// evidence that V is a lambda
sealed class ISLAMBDA<V>
class ISLAMBDAC<X, E> : ISLAMBDA<Abs<Var<X>, E>>()

// evidence that E reduces
typealias REDUCES<E> = REDUDER<E, *>

fun <G, V, TY1, TY2> followsIsLambda(
    isval: ISVAL<V>,
    der: DER<G, V, TyFun<TY1, TY2>>
): ISLAMBDA<V> = when (der) {
    is DAbs<G, *, *, *, *> -> {
        when (isval) {
            is ISVAL_Abs<*, *> -> ISLAMBDAC()
            else -> error("Impossible") // Could be inferred
        }
    }

    else -> error("Impossible") // Could be inferred
}

// \empty |- E : TY ==> E is a value /\ E reduces to some E1
fun <E, TY> progress(der: DER<Unit, E, TY>): Either<ISVAL<E>, REDUCES<E>> = when (der) {
    is DAbs<*, *, *, *, *> -> Either.Left(ISVAL_Abs<E>())
    is DLit -> Either.Left(ISVAL_Lit)
    is DApp<Unit, *, *, *, *> -> when (val r1 = progress(der.d1)) {
        is Either.Right -> Either.Right(EApp1(r1.value))
        is Either.Left -> when (val r2 = progress(der.d2)) {
            is Either.Right -> Either.Right(EApp2(r1.value, r2.value))
            is Either.Left -> when (val lambda = followsIsLambda(r1.value, der.d1)) {
                is ISLAMBDAC<*, *> -> Either.Right(EAppAbs(r2.value))
            }
        }
    }

    else -> throw IllegalArgumentException("Invalid argument")
}

sealed class Either<out L, out R> {
    class Left<L>(val value: L) : Either<L, Nothing>()
    class Right<R>(val value: R) : Either<Nothing, R>()
}
