package org.abimon.knolus.modules

import org.abimon.knolus.*

object KnolusStringModule {
    fun register(context: KnolusContext) = with(context) {
        registerMemberPropertyGetter(stringTypeParameter(), "length") { context, self -> VariableValue.IntegerType(self.length) }
        registerMemberFunction(stringTypeParameter(), "trimEnd", charParameter("trim")) { context, self, trim -> VariableValue.StringType(self.trimEnd(trim)) }
    }
}