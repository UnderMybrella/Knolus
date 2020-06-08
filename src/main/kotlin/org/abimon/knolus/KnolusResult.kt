package org.abimon.knolus

import org.abimon.knolus.types.KnolusChar
import org.abimon.knolus.types.KnolusLazyString
import org.abimon.knolus.types.KnolusTypedValue

interface KnolusResult<T> {
    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T: KnolusTypedValue.RuntimeValue> knolusLazy(value: T): KnolusResult<KnolusUnion.VariableValue<T>> = KnolusTypedSuccess(KnolusUnion.VariableValue.Lazy(value))

        @ExperimentalUnsignedTypes
        fun <T: KnolusTypedValue> knolusValue(value: T): KnolusResult<KnolusUnion.VariableValue<T>> = KnolusTypedSuccess(KnolusUnion.VariableValue.Stable(value))
        @ExperimentalUnsignedTypes
        fun knolusCharValue(value: Char): KnolusResult<KnolusUnion.VariableValue<KnolusChar>> = KnolusTypedSuccess(KnolusUnion.VariableValue.Stable(KnolusChar(value)))
        fun knolusLazyString(value: KnolusLazyString): KnolusResult<KnolusUnion.VariableValue<KnolusLazyString>> = KnolusTypedSuccess(
            KnolusUnion.VariableValue.Lazy((value)))

        /** Expression Operations */
        @ExperimentalUnsignedTypes
        fun <T: ExpressionOperator> unionExpressionOperation(value: T): KnolusResult<T> = UnionExpressionOperationSuccess(value) as KnolusResult<T>
        @ExperimentalUnsignedTypes
        fun unionExprPlus(): KnolusResult<ExpressionOperator> = UnionExpressionOperationSuccess(
            ExpressionOperator.PLUS)
        @ExperimentalUnsignedTypes
        fun unionExprMinus(): KnolusResult<ExpressionOperator> = UnionExpressionOperationSuccess(
            ExpressionOperator.MINUS)
        @ExperimentalUnsignedTypes
        fun unionExprDivide(): KnolusResult<ExpressionOperator> = UnionExpressionOperationSuccess(
            ExpressionOperator.DIVIDE)
        @ExperimentalUnsignedTypes
        fun unionExprMultiply(): KnolusResult<ExpressionOperator> = UnionExpressionOperationSuccess(
            ExpressionOperator.MULTIPLY)
        @ExperimentalUnsignedTypes
        fun unionExprExponential(): KnolusResult<ExpressionOperator> = UnionExpressionOperationSuccess(
            ExpressionOperator.EXPONENTIAL)

        @ExperimentalUnsignedTypes
        fun <T: KnolusUnion> union(value: T): KnolusResult<T> = UnionSuccess(value) as KnolusResult<T>
    }

    interface Successful<T> : KnolusResult<T> {
        val value: T
    }

    open class Success<T>(override val value: T) : Successful<T> {
        override fun get(): T = value

        override operator fun component1(): T = value

        override fun toString(): String =
            "Success(value=$value)"
    }

    class Empty<T> : KnolusResult<T> {
        override fun get(): T = throw IllegalStateException("Result is empty")
        override fun component1(): T? = null

        override fun toString(): String =
            "Empty()"
    }

    class UnknownImpl<T>(val impl: String) : KnolusResult<T> {
        override fun get(): T = throw IllegalStateException("Unknown result implementation: \"$impl\"")
        override fun component1(): T? = null

        override fun toString(): String =
            "UnknownImpl(impl=$impl)"
    }

    class Error<T, R>(val errorCode: Int, val errorMessage: String, val causedBy: KnolusResult<R>?) : KnolusResult<T> {
        companion object {
            operator fun <T> invoke(resultCode: Int, message: String): Error<T, Unit> =
                Error(resultCode, message, null)
        }

        operator fun component2(): Int = errorCode
        operator fun component3(): String = errorMessage
        operator fun component4(): KnolusResult<R>? = causedBy

        override fun get(): T = throw IllegalStateException("Result is errored", asIllegalArgumentException())

        override fun toString(): String =
            "Error(errorCode=$errorCode, errorMessage='$errorMessage', causedBy=$causedBy)"

        fun asIllegalArgumentException(): IllegalArgumentException =
            IllegalArgumentException(errorMessage, (causedBy as? Error<R, *>)?.asIllegalArgumentException())

        override fun component1(): T? = null
    }

    class Thrown<T, E : Throwable, R>(val error: E, val causedBy: KnolusResult<R>?) : KnolusResult<T> {
        companion object {
            operator fun <T, E : Throwable> invoke(error: E): Thrown<T, E, Unit> =
                Thrown(error, null)
        }

        operator fun component2(): E = error
        operator fun component3(): KnolusResult<R>? = causedBy

        override fun get(): T = throw error
        override fun component1(): T? = null
        override fun toString(): String =
            "Thrown(error=$error, causedBy=$causedBy)"
    }

    abstract fun get(): T

    abstract operator fun component1(): T?
}

/** Inline classes */

@ExperimentalUnsignedTypes
private inline class UnionSuccess(override val value: KnolusUnion) : KnolusResult.Successful<KnolusUnion> {
    override fun get(): KnolusUnion = value

    override operator fun component1(): KnolusUnion = value

    override fun toString(): String =
        "UnionSuccess(value=$value)"
}

@ExperimentalUnsignedTypes
private inline class UnionExpressionOperationSuccess(override val value: ExpressionOperator) : KnolusResult.Successful<ExpressionOperator> {
    override fun get(): ExpressionOperator = value

    override operator fun component1(): ExpressionOperator = value

    override fun toString(): String =
        "UnionSuccess(value=$value)"
}

@ExperimentalUnsignedTypes
private inline class KnolusTypedSuccess<T: KnolusTypedValue>(override val value: KnolusUnion.VariableValue<T>) : KnolusResult.Successful<KnolusUnion.VariableValue<T>> {
    override fun get(): KnolusUnion.VariableValue<T> = value

    override operator fun component1(): KnolusUnion.VariableValue<T> = value

    override fun toString(): String =
        "UnionVariableSuccess(value=$value)"
}

/** Extension methods */

@Suppress("UNCHECKED_CAST")
inline fun <reified R> KnolusResult<*>.cast(): KnolusResult<R> =
    when (this) {
        is KnolusResult.Successful -> if (value is R) this as KnolusResult<R> else KnolusResult.Empty()
        is KnolusResult.Empty -> KnolusResult.Empty()
        is KnolusResult.UnknownImpl -> KnolusResult.UnknownImpl(toString())
        is KnolusResult.Error<*, *> -> KnolusResult.Error(errorCode, errorMessage, causedBy)
        is KnolusResult.Thrown<*, *, *> -> KnolusResult.Thrown(error, causedBy)
        else -> KnolusResult.UnknownImpl(toString())
    }

inline fun <T, reified R> KnolusResult<T>.map(transform: (T) -> R): KnolusResult<R> =
    if (this is KnolusResult.Successful) KnolusResult.Success(transform(value)) else cast()

inline fun <T, reified R> KnolusResult<T>.flatMap(transform: (T) -> KnolusResult<R>): KnolusResult<R> =
    if (this is KnolusResult.Successful<T>) transform(value) else cast()

inline fun <T> KnolusResult<T>.flatMapOrSelf(transform: (T) -> KnolusResult<T>?): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) transform(value) ?: this else this

inline fun <T> KnolusResult<T>.filter(predicate: (T) -> Boolean): KnolusResult<T> =
    if (this is KnolusResult.Successful<T> && !predicate(value)) KnolusResult.Empty() else this

inline fun <T> KnolusResult<T>.filterTo(transform: (T) -> KnolusResult<T>?): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) transform(value) ?: this else this

inline fun <reified R> KnolusResult<*>.filterToInstance(): KnolusResult<R> =
    if (this is KnolusResult.Successful && value !is R) KnolusResult.Empty() else this.cast()

inline fun <reified R> KnolusResult<*>.filterToInstance(onEmpty: () -> KnolusResult<R>): KnolusResult<R> =
    if (this is KnolusResult.Successful && value !is R) onEmpty() else this.cast()

inline fun <reified R> KnolusResult<*>.filterToInstance(default: KnolusResult<R>): KnolusResult<R> =
    if (this is KnolusResult.Successful && value !is R) default else this.cast()

inline fun <T, reified R : T> KnolusResult<T>.filterToInstance(transform: (T) -> KnolusResult<R>): KnolusResult<R> =
    if (this is KnolusResult.Successful<T> && value !is R) transform(value) else this.cast()

inline fun <T> KnolusResult<T>.getOrNull(): T? = if (this is KnolusResult.Successful<T>) value else null
inline fun <T> KnolusResult<T>.getOrElse(default: T): T = if (this is KnolusResult.Successful<T>) value else default
inline fun <T> KnolusResult<T>.getOrElseRun(block: () -> T): T =
    if (this is KnolusResult.Successful<T>) value else block()

inline fun <T> KnolusResult<T>.orElse(default: KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) this else default

inline fun <T> KnolusResult<T>.switchIfFailure(block: (KnolusResult<T>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) this else block(this)

inline fun <T> KnolusResult<T>.switchIfEmpty(block: () -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Empty<T>) block() else this

inline fun <T> KnolusResult<T>.switchIfError(block: (KnolusResult.Error<T, *>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Error<T, *>) block(this) else this

inline fun <T> KnolusResult<T>.switchIfThrown(block: (KnolusResult.Thrown<T, *, *>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Thrown<T, *, *>) block(this) else this

inline fun <T> KnolusResult<T>.switchIfUnknownImpl(block: () -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.UnknownImpl<T>) block() else this

/** Run when this result is any failed state */
public inline fun <T> KnolusResult<T>.doOnFailure(block: (KnolusResult<T>) -> Unit): T {
    if (this is KnolusResult.Successful<T>) return value
    else {
        block(this)
        throw IllegalStateException()
    }
}

/** Run when this result is specifically a known error */
public inline fun <T> KnolusResult<T>.doOnError(block: (KnolusResult.Error<T, *>) -> Unit): KnolusResult<T> {
    if (this is KnolusResult.Error<T, *>) block(this)
    return this
}

public inline fun <T> KnolusResult<T>.doOnEmpty(block: () -> Unit): KnolusResult<T> {
    if (this is KnolusResult.Empty<T>) block()
    return this
}

public inline fun <T> KnolusResult<T>.doOnThrown(block: (KnolusResult.Thrown<T, *, *>) -> Unit): KnolusResult<T> {
    if (this is KnolusResult.Thrown<T, *, *>) block(this)
    return this
}

public inline fun <T> KnolusResult<T>.doOnUnknownImpl(block: () -> Unit): KnolusResult<T> {
    if (this is KnolusResult.UnknownImpl<T>) block()
    return this
}

inline fun <T> KnolusResult<T>.doOnSuccess(block: (T) -> Unit): KnolusResult<T> {
    if (this is KnolusResult.Successful<T>) block(value)
    return this
}


//@ExperimentalContracts
//public inline fun <T : Any> requireSuccessful(value: KorneaResult<T>): T {
//    contract {
//        returns() implies (value is KorneaResult.Success<T>)
//    }
//
//    when (value) {
//        is KorneaResult.Success<T> -> return value.value
//        is KorneaResult.Failure<T, *> -> throw value.asIllegalArgumentException()
//        else -> throw IllegalArgumentException("(empty)")
//    }
//}
//
//@ExperimentalContracts
//public inline fun <T : Any> requireSuccessful(value: KorneaResult<T>, lazyMessage: () -> Any): T {
//    contract {
//        returns() implies (value is KorneaResult.Success<T>)
//    }
//
//    if (value !is KorneaResult.Success<T>) {
//        val message = lazyMessage()
//        throw IllegalArgumentException(message.toString())
//    } else {
//        return value.value
//    }
//}