package org.abimon.knolus.modules

import org.abimon.knolus.*

object KnolusStringModule {
    fun register(context: KnolusContext) = with(context) {
        registerMemberPropertyGetter(stringTypeParameter(),
            "length") { context, self -> VariableValue.IntegerType(self.length) }

        registerMemberFunction(stringTypeParameter(), "trim", charArrayParameter("chars")) { context, self, chars ->
            VariableValue.StringType(self.trim { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimEnd", charArrayParameter("chars")) { context, self, chars ->
            VariableValue.StringType(self.trimEnd { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimStart", charArrayParameter("chars")) { context, self, chars ->
            VariableValue.StringType(self.trimStart { it in chars })
        }
    }
}