package org.abimon.knolus

fun Double.asReasonablePercentage(): Double =
    if (this >= -0.0001 && this <= 1.0001) this * 100.0 else this.coerceIn(0.0, 100.0)

suspend inline fun VariableValue.flattenIfPresent(context: KnolusContext): VariableValue? =
    flatten(context).takeIfPresent()

suspend inline fun VariableValue.asFlattenedStringIfPresent(context: KnolusContext): String? =
    flatten(context).takeIfPresent()?.asString(context)

@ExperimentalUnsignedTypes
public inline fun <T : VariableValue> T.takeIfPresent(): T? = when (this) {
    is VariableValue.NullType -> null
    is VariableValue.UndefinedType -> null
    else -> this
}

public inline fun <T> T.takeIf(predicate: Boolean): T? {
    return if (predicate) this else null
}

fun String.toIntBaseN(): Int = when {
    startsWith("0b") -> substring(2).toInt(2)
    startsWith("0o") -> substring(2).toInt(8)
    startsWith("0x") -> substring(2).toInt(16)
    startsWith("0d") -> substring(2).toInt()
    else -> toInt()
}

fun String.toIntOrNullBaseN(): Int? = when {
    startsWith("0b") -> substring(2).toIntOrNull(2)
    startsWith("0o") -> substring(2).toIntOrNull(8)
    startsWith("0x") -> substring(2).toIntOrNull(16)
    startsWith("0d") -> substring(2).toIntOrNull()
    else -> toIntOrNull()
}

@Suppress("UNCHECKED_CAST")
fun <T> Array<T>.copyWithStripe(count: Int): Array<T> {
    val newArray = this.copyOf(count)
    for (i in 1 until count / size) copyInto(newArray, i * size)
    if (count > size && count % size > 0) {
        val lastSize = count % size
        copyInto(newArray, count - lastSize, 0, lastSize)
    }

    return newArray as Array<T>
}
@Suppress("UNCHECKED_CAST")
fun <T> Array<T>.copyWithGrouping(count: Int): Array<T> {
    val newArray = java.lang.reflect.Array.newInstance(this::class.java.componentType, size * count) as Array<T>
    for (i in newArray.indices) {
        newArray[i] = this[i / count]
    }

    return newArray
}
inline fun <T, reified R> Array<T>.mapToArray(startingIndex: Int = 0, block: (T) -> R): Array<R> = Array(size - startingIndex) { i -> block(get(startingIndex + i)) }

public inline fun buildStringVariable(builderAction: StringBuilder.() -> Unit): VariableValue.StringType =
    VariableValue.StringType(StringBuilder().apply(builderAction).toString())

@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any> Array<*>.asArrayOf(): Array<T>? = if (T::class.java.isAssignableFrom(this::class.java.componentType)) this as Array<T> else null

//@Suppress("UNCHECKED_CAST")
//public inline fun <reified T : Any, R> Array<*>.ifArrayIs(block: (Array<T>) -> R): R? = if (T::class.java.isAssignableFrom(this::class.java.componentType)) block(this as Array<T>) else null

public inline fun <reified T : Any, R> Array<*>.ifArrayIs(block: (Array<T>) -> R): R? {
    @Suppress("UNCHECKED_CAST")
    return block(this as? Array<T> ?: return null)
}

public inline fun <T : Any, reified R : Any> Array<T>.coerceArrayBetween(block: (Array<R>) -> Array<R>): Array<T>? {
    @Suppress("UNCHECKED_CAST")
    return block(this as? Array<R> ?: return null) as Array<T>
}

public suspend inline fun <T : Any, reified R : Any> Array<T>.coerceArrayBetweenSuspending(block: suspend (Array<R>) -> Array<R>): Array<T>? {
    @Suppress("UNCHECKED_CAST")
    return block(this as? Array<R> ?: return null) as Array<T>
}

public suspend inline fun VariableValue.fullyFlattened(context: KnolusContext): VariableValue {
    var lastResult: VariableValue
    var flattened: VariableValue = this
    do {
        lastResult = flattened
        flattened = lastResult.flatten(context)
    } while (flattened !== lastResult)

    return flattened
}