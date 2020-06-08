package org.abimon.knolus.modules

import org.abimon.knolus.*
import org.abimon.knolus.types.KnolusInt
import org.abimon.knolus.types.KnolusString

object KnolusStringModule {
    fun register(context: KnolusContext) = with(context) {
        registerMemberPropertyGetter(stringTypeParameter(),
            "length") { context, self -> KnolusInt(self.length) }

        registerMemberFunction(stringTypeParameter(), "trim", charArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trim { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimEnd", charArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimEnd { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimStart", charArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimStart { it in chars })
        }
    }
}