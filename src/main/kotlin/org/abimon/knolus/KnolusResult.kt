package org.abimon.knolus

import org.abimon.knolus.types.KnolusChar
import org.abimon.knolus.types.KnolusLazyString
import org.abimon.knolus.types.KnolusTypedValue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

interface KnolusResult<out T> {
    @Suppress("UNCHECKED_CAST")
    companion object {
        fun success(): KnolusResult<StaticSuccess> = StaticSuccess
        fun success(value: Boolean): KnolusResult<Boolean> = BooleanInlineSuccess(value)
        fun success(value: Int): KnolusResult<Int> = IntInlineSuccess(value)
        fun success(value: Double): KnolusResult<Double> = DoubleInlineSuccess(value)
        fun success(value: Char): KnolusResult<Char> = CharInlineSuccess(value)
        fun success(value: String): KnolusResult<String> = StringInlineSuccess(value)
        fun success(value: CharArray): KnolusResult<CharArray> = CharArrayInlineSuccess(value)
        fun success(value: Array<String>): KnolusResult<Array<String>> = StringArrayInlineSuccess(value)

        fun successOrEmpty(value: Boolean?): KnolusResult<Boolean> = if (value == null) empty() else BooleanInlineSuccess(value)
        fun successOrEmpty(value: Int?): KnolusResult<Int> = if (value == null) empty() else IntInlineSuccess(value)
        fun successOrEmpty(value: Double?): KnolusResult<Double> = if (value == null) empty() else DoubleInlineSuccess(value)
        fun successOrEmpty(value: Char?): KnolusResult<Char> = if (value == null) empty() else CharInlineSuccess(value)
        fun successOrEmpty(value: String?): KnolusResult<String> = if (value == null) empty() else StringInlineSuccess(value)
        fun successOrEmpty(value: CharArray?): KnolusResult<CharArray> = if (value == null) empty() else CharArrayInlineSuccess(value)
        fun successOrEmpty(value: Array<String>?): KnolusResult<Array<String>> = if (value == null) empty() else StringArrayInlineSuccess(value)

        inline fun <T> success(value: T): KnolusResult<T> = Success(value)
        inline fun <T> successOrEmpty(value: T?): KnolusResult<T> = if (value == null) empty() else success(value)
        inline fun <T> foldingMutableListOf(list: MutableList<T> = ArrayList()): KnolusResult<MutableList<T>> = Success(list)

        inline fun <T> empty(): KnolusResult<T> = Empty.INSTANCE()
        inline fun <T> nullEmpty(): KnolusResult<T> = Empty.Null()
        inline fun <T> undefinedEmpty(): KnolusResult<T> = Empty.Undefined()

        inline fun <T> error(resultCode: Int, message: String): KnolusResult<T> =
            Error<T, Unit>(resultCode, message, IllegalArgumentException(message, null), null)

        inline fun <T, R> error(resultCode: Int, message: String, causedBy: KnolusResult<R>?): KnolusResult<T> =
            Error(resultCode,
                message,
                IllegalArgumentException(message, (causedBy as? Thrown<*, *, *>)?.error),
                causedBy)

        fun <T : KnolusTypedValue.RuntimeValue<*>> knolusLazy(value: T): KnolusResult<KnolusUnion.VariableValue<T>> =
            KnolusTypedVarSuccess(KnolusUnion.VariableValue.Lazy(value))

        fun <T: KnolusTypedValue?> knolusTyped(value: T): KnolusResult<T> = KnolusTypedSuccess(value)

        @ExperimentalUnsignedTypes
        fun <T : KnolusTypedValue> knolusTypedVar(value: T): KnolusResult<KnolusUnion.VariableValue<T>> =
            KnolusTypedVarSuccess(KnolusUnion.VariableValue.Stable(value))

        @ExperimentalUnsignedTypes
        fun knolusCharValue(value: Char): KnolusResult<KnolusUnion.VariableValue<KnolusChar>> =
            KnolusTypedVarSuccess(KnolusUnion.VariableValue.Stable(KnolusChar(value)))

        fun knolusLazyString(value: KnolusLazyString): KnolusResult<KnolusUnion.VariableValue<KnolusLazyString>> =
            KnolusTypedVarSuccess(
                KnolusUnion.VariableValue.Lazy((value)))

        /** Expression Operations */
        @ExperimentalUnsignedTypes
        fun <T : ExpressionOperator> unionExpressionOperation(value: T): KnolusResult<T> =
            UnionExpressionOperationSuccess(value) as KnolusResult<T>

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
        fun <T : KnolusUnion> union(value: T): KnolusResult<T> = UnionSuccess(value) as KnolusResult<T>
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

    interface Empty: KnolusResult<Any?> {
        object INSTANCE : Empty {
            inline operator fun <T> invoke(): KnolusResult<T> = this as KnolusResult<T>
            override fun toString(): String = "Empty()"
        }

        object Null: Empty {
            inline operator fun <T> invoke(): KnolusResult<T> = this as KnolusResult<T>
            override fun toString(): String = "EmptyNull()"
        }

        object Undefined: Empty {
            inline operator fun <T> invoke(): KnolusResult<T> = this as KnolusResult<T>
            override fun toString(): String = "EmptyUndefined()"
        }

        override fun get(): Any? = throw IllegalStateException("Result is empty")
        override fun component1(): Any? = null
    }

    class UnknownImpl<T>(val impl: String) : KnolusResult<T> {
        override fun get(): T = throw IllegalStateException("Unknown result implementation: \"$impl\"")
        override fun component1(): T? = null

        override fun toString(): String =
            "UnknownImpl(impl=$impl)"
    }

    abstract class WithCause<T, R>(val causedBy: KnolusResult<R>?) : KnolusResult<T> {
        abstract infix fun <N> withCause(newCause: KnolusResult<N>?): WithCause<T, N>
    }

    open class Thrown<T, E : Throwable, R>(val error: E, causedBy: KnolusResult<R>?) : WithCause<T, R>(causedBy) {
        companion object {
            operator fun <T, E : Throwable> invoke(error: E): Thrown<T, E, Unit> =
                Thrown(error, null)
        }

        override fun component1(): T? = null
//        operator fun component2(): E = error
//        operator fun component3(): KnolusResult<R>? = causedBy

        override fun get(): T = throw error
        override fun toString(): String =
            "Thrown(error=$error, causedBy=$causedBy)"

        override fun <N> withCause(newCause: KnolusResult<N>?): WithCause<T, N> = Thrown(error, newCause)
    }

    class Error<T, R>(
        val errorCode: Int,
        val errorMessage: String,
        error: IllegalArgumentException,
        causedBy: KnolusResult<R>?,
    ) : Thrown<T, IllegalArgumentException, R>(error, causedBy) {
        companion object {
            @Suppress("NOTHING_TO_INLINE")
            inline operator fun <T> invoke(resultCode: Int, message: String): Error<T, Unit> =
                Error(resultCode, message, IllegalArgumentException(message, null), null)

            @Suppress("NOTHING_TO_INLINE")
            inline operator fun <T, R> invoke(
                resultCode: Int,
                message: String,
                causedBy: KnolusResult<R>?,
                includeResultCode: Boolean = true
            ): Error<T, R> =
                Error(resultCode,
                    message,
                    IllegalArgumentException("$message (0x${resultCode.toString(16)})", (causedBy as? Thrown<*, *, *>)?.error),
                    causedBy)
        }

        override fun component1(): T? = null
        operator fun component2(): Int = errorCode
        operator fun component3(): String = errorMessage
//        operator fun component4(): KnolusResult<R>? = causedBy

//        override fun get(): T = throw IllegalStateException("Result is errored", error)

        override fun toString(): String =
            "Error(errorCode=$errorCode, errorMessage='$errorMessage', causedBy=$causedBy)"

        override fun <N> withCause(newCause: KnolusResult<N>?): WithCause<T, N> =
            Error(errorCode, errorMessage, error, newCause)
    }

    abstract fun get(): T

    abstract operator fun component1(): T?
}

/** Inline classes */

object StaticSuccess : KnolusResult.Successful<StaticSuccess> {
    operator fun invoke(): KnolusResult<StaticSuccess> = this

    override val value
        get() = this

    override fun get() = this
    override fun component1() = this

    override fun toString(): String = "[UnitSuccess]"
}

private inline class BooleanInlineSuccess(override val value: Boolean) : KnolusResult.Successful<Boolean> {
    override fun get(): Boolean = value
    override fun component1(): Boolean = value

    override fun toString(): String = "BooleanSuccess(value=$value)"
}

private inline class IntInlineSuccess(override val value: Int): KnolusResult.Successful<Int> {
    override fun get(): Int = value
    override fun component1(): Int = value

    override fun toString(): String = "IntSuccess(value=$value)"
}

private inline class DoubleInlineSuccess(override val value: Double): KnolusResult.Successful<Double> {
    override fun get(): Double = value
    override fun component1(): Double = value

    override fun toString(): String = "DoubleSuccess(value=$value)"
}

private inline class CharInlineSuccess(override val value: Char): KnolusResult.Successful<Char> {
    override fun get(): Char = value
    override fun component1(): Char = value

    override fun toString(): String = "CharSuccess(value=$value)"
}

private inline class StringInlineSuccess(override val value: String): KnolusResult.Successful<String> {
    override fun get(): String = value
    override fun component1(): String = value

    override fun toString(): String = "StringSuccess(value=$value)"
}

private inline class CharArrayInlineSuccess(override val value: CharArray): KnolusResult.Successful<CharArray> {
    override fun get(): CharArray = value
    override fun component1(): CharArray = value

    override fun toString(): String = "CharArraySuccess(value=$value)"
}

private inline class StringArrayInlineSuccess(override val value: Array<String>): KnolusResult.Successful<Array<String>> {
    override fun get(): Array<String> = value
    override fun component1(): Array<String> = value

    override fun toString(): String = "StringArraySuccess(value=$value)"
}

@ExperimentalUnsignedTypes
private inline class UnionSuccess(override val value: KnolusUnion) : KnolusResult.Successful<KnolusUnion> {
    override fun get(): KnolusUnion = value

    override operator fun component1(): KnolusUnion = value

    override fun toString(): String =
        "UnionSuccess(value=$value)"
}

@ExperimentalUnsignedTypes
private inline class UnionExpressionOperationSuccess(override val value: ExpressionOperator) :
    KnolusResult.Successful<ExpressionOperator> {
    override fun get(): ExpressionOperator = value

    override operator fun component1(): ExpressionOperator = value

    override fun toString(): String =
        "UnionSuccess(value=$value)"
}

@Suppress("UNCHECKED_CAST")
private inline class KnolusTypedSuccess<T: KnolusTypedValue?>(private val _value: KnolusTypedValue?): KnolusResult.Successful<T> {
    override val value: T
        get() = _value as T

    override fun get(): T = value
    override fun component1(): T = value
}

@ExperimentalUnsignedTypes
private inline class KnolusTypedVarSuccess<T : KnolusTypedValue>(override val value: KnolusUnion.VariableValue<T>) :
    KnolusResult.Successful<KnolusUnion.VariableValue<T>> {
    override fun get(): KnolusUnion.VariableValue<T> = value

    override operator fun component1(): KnolusUnion.VariableValue<T> = value

    override fun toString(): String =
        "UnionVariableSuccess(value=$value)"
}

/** Extension methods */

inline fun KnolusResult<*>.hierarchy(): List<KnolusResult<*>> = ArrayList<KnolusResult<*>>().apply(this::hierarchy)
inline fun KnolusResult<*>.hierarchy(list: MutableList<KnolusResult<*>>) {
    var self: KnolusResult<*>? = this
    while (self is KnolusResult.WithCause<*, *>) {
        list.add(self)
        self = self.causedBy
    }

    self?.let(list::add)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified R> KnolusResult<*>.cast(): KnolusResult<R> =
    when (this) {
        is KnolusResult.Successful -> if (value is R) this as KnolusResult<R> else KnolusResult.empty()
        else -> this as KnolusResult<R>
//        is KnolusResult.Empty -> KnolusResult.Empty()
//        is KnolusResult.UnknownImpl -> KnolusResult.UnknownImpl(toString())
//        is KnolusResult.Error<*, *> -> KnolusResult.error(errorCode, errorMessage, causedBy)
//        is KnolusResult.Thrown<*, *, *> -> KnolusResult.Thrown(error, causedBy)
//        else -> KnolusResult.UnknownImpl(toString())
    }

inline fun <T, reified R> KnolusResult<T>.map(transform: (T) -> R): KnolusResult<R> =
    if (this is KnolusResult.Successful) KnolusResult.Success(transform(value)) else cast()

inline fun <T> KnolusResult<T>.mapCausedBy(transform: (KnolusResult<*>?) -> KnolusResult<*>?): KnolusResult<T> =
    if (this is KnolusResult.WithCause<T, *>) this withCause transform(causedBy) else this

@Suppress("UNCHECKED_CAST")
inline fun <T> KnolusResult<T>.mapRootCausedBy(rootCause: KnolusResult<*>?): KnolusResult<T> =
    hierarchy().asReversed().fold(rootCause) { causedBy, result ->
        (result as? KnolusResult.WithCause<*, *>)?.withCause(causedBy) ?: result
    } as? KnolusResult<T> ?: this

inline fun <T, reified R> KnolusResult<T>.flatMap(transform: (T) -> KnolusResult<R>): KnolusResult<R> =
    if (this is KnolusResult.Successful<T>) transform(value) else cast()

inline fun <T> KnolusResult<T>.flatMapOrSelf(transform: (T) -> KnolusResult<T>?): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) transform(value) ?: this else this

inline fun <T> KnolusResult<T>.filter(predicate: (T) -> Boolean): KnolusResult<T> =
    if (this is KnolusResult.Successful<T> && !predicate(value)) KnolusResult.empty() else this

inline fun <T> KnolusResult<T>.filterTo(transform: (T) -> KnolusResult<T>?): KnolusResult<T> =
    if (this is KnolusResult.Successful<T>) transform(value) ?: this else this

inline fun <reified R> KnolusResult<*>.filterToInstance(): KnolusResult<R> =
    if (this is KnolusResult.Successful && value !is R) KnolusResult.empty() else this.cast()

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
    if (this is KnolusResult.Empty) block() else this

inline fun <T> KnolusResult<T>.switchIfHasCause(block: (KnolusResult.WithCause<T, *>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.WithCause<T, *>) block(this) else this

inline fun <T> KnolusResult<T>.switchIfThrown(block: (KnolusResult.Thrown<T, *, *>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Thrown<T, *, *>) block(this) else this

inline fun <T> KnolusResult<T>.switchIfError(block: (KnolusResult.Error<T, *>) -> KnolusResult<T>): KnolusResult<T> =
    if (this is KnolusResult.Error<T, *>) block(this) else this

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
    if (this is KnolusResult.Empty) block()
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

inline fun KnolusResult<*>.wasSuccessful(): Boolean = this is KnolusResult.Successful<*>

@ExperimentalContracts
public inline fun <T : Any> requireSuccessful(value: KnolusResult<T>): T {
    contract {
        returns() implies (value is KnolusResult.Successful<T>)
    }

    when (value) {
        is KnolusResult.Successful<T> -> return value.value
        is KnolusResult.Thrown<T, *, *> -> throw value.error
        else -> throw IllegalArgumentException(value.toString())
    }
}

@ExperimentalContracts
public inline fun <T : Any> requireSuccessful(value: KnolusResult<T>, lazyMessage: () -> Any): T {
    contract {
        returns() implies (value is KnolusResult.Successful<T>)
    }

    if (value !is KnolusResult.Successful<T>) {
        val message = lazyMessage()
        throw IllegalArgumentException(message.toString(), (value as? KnolusResult.Thrown<T, *, *>)?.error)
    } else {
        return value.value
    }
}