package org.abimon.knolus.restrictions

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.KnolusFunction
import org.abimon.knolus.types.KnolusTypedValue

data class KnolusRecursiveRestriction(val maxDepth: Int = 0xFF, val maxRecursiveCount: Int = 0x80): KnolusBasePermissiveRestrictions {
    @ExperimentalUnsignedTypes
    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>
    ): Boolean {
        if (context.recursionLevel > maxDepth) return false
        if (context.countFunctionRecursion(function) >= maxRecursiveCount) return false

        return true
    }
}