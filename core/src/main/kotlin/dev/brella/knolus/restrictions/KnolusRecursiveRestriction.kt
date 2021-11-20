package dev.brella.knolus.restrictions

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.KnolusFunction
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.errors.common.KorneaResult

data class KnolusRecursiveRestriction<T>(val maxDepth: Int = 0xFF, val maxRecursiveCount: Int = 0x80, val hideMaxDepth: Boolean = false, val hideMaxRecursiveCount: Boolean = false): KnolusBaseRestriction<T> {
    companion object {
        const val MAX_DEPTH_REACHED = 0x2000
        const val MAX_RECURSION_REACHED = 0x2001
    }

    inline fun <T> maxDepthReached() = KorneaResult.errorAsIllegalState<T>(MAX_DEPTH_REACHED, "Max depth reached ($maxDepth)")
    inline fun <T> maxRecursionReached() = KorneaResult.errorAsIllegalState<T>(MAX_RECURSION_REACHED, "Max recursion reached ($maxRecursiveCount)")

    inline fun <T> hiddenMaxDepthReached() = KorneaResult.errorAsIllegalState<T>(MAX_DEPTH_REACHED, "Max depth reached")
    inline fun <T> hiddenMaxRecursionReached() = KorneaResult.errorAsIllegalState<T>(MAX_RECURSION_REACHED, "Max recursion reached")

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>
    ): KorneaResult<T> {
        if (context.depth >= maxDepth) {
            if (hideMaxDepth) return hiddenMaxDepthReached()
            return maxDepthReached()
        }
        if (context.recursiveCountFor(function) >= maxRecursiveCount) {
            if (hideMaxRecursiveCount) return hiddenMaxRecursionReached()
            return maxRecursionReached()
        }

        return KorneaResult.empty()
    }
}