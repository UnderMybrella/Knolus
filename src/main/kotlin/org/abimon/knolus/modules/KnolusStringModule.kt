package org.abimon.knolus.modules

import org.abimon.knolus.KnolusContext
import org.abimon.knolus.VariableValue
import org.abimon.knolus.registerMemberPropertyGetter
import org.abimon.knolus.stringTypeParameter

object KnolusStringModule {
    fun register(context: KnolusContext) = with(context) {
        registerMemberPropertyGetter(stringTypeParameter(), "length") { context, self -> VariableValue.IntegerType(self.length) }
    }
}