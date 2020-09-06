package dev.brella.knolus

import dev.brella.knolus.types.*
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees
import java.math.BigInteger

fun Double.asReasonablePercentage(): Double =
    if (this >= -0.0001 && this <= 1.0001) this * 100.0 else this.coerceIn(0.0, 100.0)

//suspend inline fun KnolusTypedValue.flattenIfPresent(context: KnolusContext): KnolusTypedValue? =
//    flatten(context).takeIfPresent()
//
//suspend inline fun KnolusTypedValue.asFlattenedStringIfPresent(context: KnolusContext): String? =
//    flatten(context).takeIfPresent()?.asString(context)

@ExperimentalUnsignedTypes
public inline fun <T : KnolusTypedValue> T.takeIfPresent(): T? = when (this) {
    is KnolusConstants.Null -> null
    is KnolusConstants.Undefined -> null
    else -> this
}

public inline fun <T> T.takeIf(predicate: Boolean): T? {
    return if (predicate) this else null
}

inline fun String.baseN(): Int = when {
    startsWith("0b") -> 2
    startsWith("0o") -> 8
    startsWith("0x") -> 16
    startsWith("0d") -> 10
    else -> 10
}

fun String.stripBase(): Pair<String, Int> = when {
    startsWith("0b") -> substring(2) to 2
    startsWith("0o") -> substring(2) to 8
    startsWith("0x") -> substring(2) to 16
    startsWith("0d") -> substring(2) to 10
    else -> this to 10
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

fun String.toLongBaseN(): Long = when {
    startsWith("0b") -> substring(2).toLong(2)
    startsWith("0o") -> substring(2).toLong(8)
    startsWith("0x") -> substring(2).toLong(16)
    startsWith("0d") -> substring(2).toLong()
    else -> toLong()
}

fun String.toLongOrNullBaseN(): Long? = when {
    startsWith("0b") -> substring(2).toLongOrNull(2)
    startsWith("0o") -> substring(2).toLongOrNull(8)
    startsWith("0x") -> substring(2).toLongOrNull(16)
    startsWith("0d") -> substring(2).toLongOrNull()
    else -> toLongOrNull()
}

fun String.toBigIntBaseN(): BigInteger = when {
    startsWith("0b") -> substring(2).toBigInt(2)
    startsWith("0o") -> substring(2).toBigInt(8)
    startsWith("0x") -> substring(2).toBigInt(16)
    startsWith("0d") -> substring(2).toBigInt()
    else -> toBigInt()
}

fun String.toBigIntOrNullBaseN(): BigInteger? = when {
    startsWith("0b") -> substring(2).toBigIntOrNull(2)
    startsWith("0o") -> substring(2).toBigIntOrNull(8)
    startsWith("0x") -> substring(2).toBigIntOrNull(16)
    startsWith("0d") -> substring(2).toBigIntOrNull()
    else -> toBigIntOrNull()
}

inline fun String.toBigInt(): BigInteger = BigInteger(this, 10)
inline fun String.toBigIntOrNull(): BigInteger? = if (isValidInBase(10)) BigInteger(this, 10) else null
inline fun String.toBigInt(radix: Int): BigInteger = BigInteger(this, radix)
inline fun String.toBigIntOrNull(radix: Int): BigInteger? = if (isValidInBase(radix)) BigInteger(this, radix) else null

inline fun String.isValidInBase(radix: Int): Boolean = (if (startsWith('-')) substring(1) else this).minOf { Character.digit(it, radix) } >= 0

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

inline fun <T, reified R> Array<T>.mapToArray(startingIndex: Int = 0, block: (T) -> R): Array<R> =
    Array(size - startingIndex) { i -> block(get(startingIndex + i)) }

public inline fun buildStringVariable(builderAction: StringBuilder.() -> Unit): KnolusString =
    KnolusString(StringBuilder().apply(builderAction).toString())

@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any> Array<*>.asArrayOf(): Array<T>? =
    if (T::class.java.isAssignableFrom(this::class.java.componentType)) this as Array<T> else null

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

inline fun String.toFormattedBoolean(): Boolean {
    if (isBlank()) return false

    if (equals("true", true))
        return true
    if (equals("false", true))
        return false

    if (this[0].toInt().or(0x20) == 0x79) //Y
        return true
    if (this[0].toInt().or(0x20) == 0x6E) //N
        return false

    return (toIntOrNullBaseN() ?: 0) != 0
}

public inline fun <T, R> Array<out T>.mapWith(transform: (T) -> R): Array<Pair<T, R>> = mapWith(::Pair, transform)
public inline fun <T, R, reified P> Array<out T>.mapWith(zip: (T, R) -> P, transform: (T) -> R): Array<P> =
    Array(size) { i ->
        val element = get(i)
        zip(element, transform(element))
    }

//public suspend inline fun KnolusType.fullyFlattened(context: KnolusContext): KnolusType {
//    var lastResult: KnolusType
//    var flattened: KnolusType = this
//    do {
//        lastResult = flattened
//        flattened = lastResult.flatten(context)
//    } while (flattened != lastResult)
//
//    return flattened
//}

//inline fun <T> T?.switchIfNull(other: T): T = this ?: other
//inline fun <T> T?.switchIfNull(other: () -> T): T = this ?: other()

inline fun <T, C: MutableList<T>> C.withElement(t: T): C {
    add(t)
    return this
}

inline fun <T, C: MutableList<T>> C.withElements(t: List<out T>): C {
    addAll(t)
    return this
}

inline fun <T, C: MutableList<T>> C.withElements(t: Array<out T>): C {
    addAll(t)
    return this
}

inline fun <T, C: MutableList<T>> T.addTo(c: C): T {
    c.add(this)
    return this
}

internal val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
internal fun String.sanitiseFunctionIdentifier(): String = toUpperCase().replace(SEPARATOR_CHARACTERS, "")


fun toStringTree(t: Tree, recog: Parser?): String? {
    val ruleNames = recog?.ruleNames
    val ruleNamesList = if (ruleNames != null) listOf(*ruleNames) else null
    return toStringTree(t, ruleNamesList)
}

/** Print out a whole tree in LISP form. [.getNodeText] is used on the
 * node payloads to get the text for the nodes.
 */
fun toStringTree(t: Tree, ruleNames: List<String?>?, indent: Int = 0): String? {
    var s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    if (t.childCount == 0) return s
    val buf = StringBuilder()
//    buf.append("(")
    buf.appendln()
    repeat(indent) { buf.append('\t') }
    buf.append("> ")
    s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false)
    buf.append(s)
    buf.append(' ')
    for (i in 0 until t.childCount) {
        if (i > 0) buf.append(' ')
        buf.append(toStringTree(t.getChild(i), ruleNames, indent + 1))
    }
//    buf.append(")")
    return buf.toString()
}

inline fun <reified T> asNull(): T? = null