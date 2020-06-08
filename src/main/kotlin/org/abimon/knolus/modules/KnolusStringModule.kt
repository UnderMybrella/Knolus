package org.abimon.knolus.modules

import org.abimon.knolus.*
import org.abimon.knolus.types.KnolusInt
import org.abimon.knolus.types.KnolusNumericalType
import org.abimon.knolus.types.KnolusString

object KnolusStringModule {
    suspend fun concatStrings(context: KnolusContext, a: String, b: String) = KnolusString("${a}${b}")

    fun register(context: KnolusContext) = with(context) {
        registerOperatorFunction(stringTypeParameter(), ExpressionOperator.PLUS, stringTypeParameter(), KnolusStringModule::concatStrings)
//        registerOperatorFunction(stringTypeParameter(), ExpressionOperator.PLUS, KnolusNumericalType.parameterSpecWith(KnolusTransformations.NUMBER_TO_8_BYTE_HEX_STRING), KnolusStringModule::concatStrings)
        registerOperatorFunction(stringTypeParameter(), ExpressionOperator.PLUS, booleanTypeAsStringParameter(), KnolusStringModule::concatStrings)

        registerMemberPropertyGetter(stringTypeParameter(), "length") { context, self -> KnolusInt(self.length) }

        registerMemberFunction(stringTypeParameter(), "trim", arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trim { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimEnd", arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimEnd { it in chars })
        }
        registerMemberFunction(stringTypeParameter(), "trimStart", arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimStart { it in chars })
        }
    }
}