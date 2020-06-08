package org.abimon.knolus.modules

import org.abimon.knolus.*
import org.abimon.knolus.modules.functionregistry.functionBuilder
import org.abimon.knolus.modules.functionregistry.registerFunction
import org.abimon.knolus.modules.functionregistry.registerMemberFunction
import org.abimon.knolus.types.KnolusInt
import org.abimon.knolus.types.KnolusNumericalType
import org.abimon.knolus.types.KnolusString
import kotlin.math.log

object KnolusStringModule {
    suspend fun concatToString(context: KnolusContext, a: String, b: Char) = KnolusString("${a}${b}")
    suspend fun concatStrings(context: KnolusContext, a: String, b: String) = KnolusString("${a}${b}")

    fun register(context: KnolusContext) = with(context) {
        registerOperatorFunction(stringTypeParameter(),
            ExpressionOperator.PLUS,
            charTypeParameter(),
            KnolusStringModule::concatToString)
        registerMultiOperatorFunction(stringTypeParameter(), ExpressionOperator.PLUS, arrayOf(
            stringTypeParameter(),
            numberTypeAsStringParameter(),
            booleanTypeAsStringParameter(),
            nullTypeAsStringParameter(),
            undefinedTypeAsStringParameter()
        ), KnolusStringModule::concatStrings)

        registerMemberPropertyGetter(stringTypeParameter(), "length") { context, self -> KnolusInt(self.length) }

        registerMemberFunction(stringTypeParameter(),
            "trim",
            arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trim { it in chars })
        }
        registerMemberFunction(stringTypeParameter(),
            "trimEnd",
            arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimEnd { it in chars })
        }
        registerMemberFunction(stringTypeParameter(),
            "trimStart",
            arrayTypeAsCharArrayParameter("chars")) { context, self, chars ->
            KnolusString(self.trimStart { it in chars })
        }

        registerFunction("str", doubleTypeParameter("double")) { _, double -> KnolusString(double.toString()) }
        registerFunction("str",
            numberTypeAsIntParameter("int"),
            intTypeParameter("base", 10)) { _, int, base -> KnolusString(int.toString(base)) }

        registerIntToString()
    }

    fun KnolusContext.registerIntToString() {
        registerFunction(
            "hex",
            numberTypeAsIntParameter("num"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, uppercase ->
            buildStringVariable {
                append("0x")
                if (uppercase) append(num.toString(16).toUpperCase())
                else append(num.toString(16))
            }
        }

        registerFunction(
            "hex",
            numberTypeAsIntParameter("num"),
            numberTypeAsIntParameter("length"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, length, uppercase ->
            buildStringVariable {
                append("0x")
                append(CharArray((length - 1 - (log(num.toDouble(), 16.0).toInt())).coerceAtLeast(0)) { '0' })

                if (uppercase) append(num.toString(16).toUpperCase())
                else append(num.toString(16))
            }
        }

        registerFunction(
            "bin",
            numberTypeAsIntParameter("num"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, uppercase ->
            buildStringVariable {
                append("0b")
                if (uppercase) append(num.toString(2).toUpperCase())
                else append(num.toString(2))
            }
        }

        registerFunction(
            "bin",
            numberTypeAsIntParameter("num"),
            numberTypeAsIntParameter("length"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, length, uppercase ->
            buildStringVariable {
                append("0b")
                append(CharArray((length - 1 - (log(num.toDouble(), 2.0).toInt())).coerceAtLeast(0)) { '0' })

                if (uppercase) append(num.toString(2).toUpperCase())
                else append(num.toString(2))
            }
        }

        registerFunction(
            "oct",
            numberTypeAsIntParameter("num"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, uppercase ->
            buildStringVariable {
                append("0o")
                if (uppercase) append(num.toString(8).toUpperCase())
                else append(num.toString(8))
            }
        }

        registerFunction(
            "oct",
            numberTypeAsIntParameter("num"),
            numberTypeAsIntParameter("length"),
            booleanTypeParameter("uppercase", false)
        ) { _, num, length, uppercase ->
            buildStringVariable {
                append("0o")
                append(CharArray((length - 1 - (log(num.toDouble(), 8.0).toInt())).coerceAtLeast(0)) { '0' })

                if (uppercase) append(num.toString(8).toUpperCase())
                else append(num.toString(8))
            }
        }
    }
}