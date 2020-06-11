package org.abimon.knolus.restrictions

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.KnolusFunction
import org.abimon.knolus.KnolusResult
import org.abimon.knolus.types.KnolusTypedValue

data class KnolusRecursiveRestriction<T>(val maxDepth: Int = 0xFF, val maxRecursiveCount: Int = 0x80, val hideMaxDepth: Boolean = false, val hideMaxRecursiveCount: Boolean = false): KnolusBaseRestriction<T> {
    companion object {
        const val MAX_DEPTH_REACHED = 0x2000
        const val MAX_RECURSION_REACHED = 0x2001
    }

    inline fun <T> maxDepthReached() = KnolusResult.Error<T>(MAX_DEPTH_REACHED, "Max depth reached ($maxDepth)")
    inline fun <T> maxRecursionReached() = KnolusResult.Error<T>(MAX_RECURSION_REACHED, "Max recursion reached ($maxRecursiveCount)")

    inline fun <T> hiddenMaxDepthReached() = KnolusResult.Error<T>(MAX_DEPTH_REACHED, "Max depth reached")
    inline fun <T> hiddenMaxRecursionReached() = KnolusResult.Error<T>(MAX_RECURSION_REACHED, "Max recursion reached")

    @ExperimentalUnsignedTypes
    override fun canRunFunction(
        context: KnolusContext<T>,
        function: KnolusFunction<KnolusTypedValue?, T, *>,
        parameters: Map<String, KnolusTypedValue>
    ): KnolusResult<T> {
        if (context.depth > maxDepth) {
            if (hideMaxDepth) return hiddenMaxDepthReached()
            return maxDepthReached()
        }
        if (context.recursiveCountFor(function) >= maxRecursiveCount) {
            if (hideMaxRecursiveCount) return hiddenMaxRecursionReached()
            return maxRecursionReached()
        }

        return KnolusResult.empty()
    }
}