package dev.brella.knolus.modules

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.*
import dev.brella.knolus.types.KnolusBoolean
import dev.brella.knolus.types.KnolusInt
import dev.brella.knolus.types.KnolusString
import dev.brella.kornea.errors.common.getOrElse
import kotlin.math.log

object KnolusStringModule {
    suspend fun concatToString(a: String, b: Char) = KnolusString("${a}${b}")
    suspend fun concatStrings(a: String, b: String) = KnolusString("${a}${b}")

    fun register(context: KnolusContext<*>) = with(context) {
        registerOperatorFunction(
            stringTypeParameter(),
            ExpressionOperator.PLUS,
            charTypeParameter(),
            KnolusStringModule::concatToString
        )

        registerMultiOperatorFunction(
            stringTypeParameter(), ExpressionOperator.PLUS, arrayOf(
                stringTypeParameter(),
                numberTypeAsStringParameter(),
                booleanTypeAsStringParameter(),
                nullTypeAsStringParameter(),
                undefinedTypeAsStringParameter()
            ), KnolusStringModule::concatStrings
        )

        registerMemberPropertyGetter(stringTypeParameter(), "length") { self -> KnolusInt(self.length) }
        registerMemberFunction(stringTypeParameter(), "trim", arrayTypeAsCharArrayParameter("chars")) { self, chars -> KnolusString(self.trim { it in chars }) }
        registerMemberFunction(stringTypeParameter(), "trimEnd", arrayTypeAsCharArrayParameter("chars")) { self, chars -> KnolusString(self.trimEnd { it in chars }) }
        registerMemberFunction(stringTypeParameter(), "trimStart", arrayTypeAsCharArrayParameter("chars")) { self, chars -> KnolusString(self.trimStart { it in chars }) }

        registerMemberFunction(stringTypeParameter(), "padStart", numberTypeAsIntParameter("length"), charTypeParameter("padChar", default = ' ')) { self, length, padChar ->
            KnolusString(self.padStart(length, padChar))
        }

        registerMemberFunction(stringTypeParameter(), "padEnd", numberTypeAsIntParameter("length"), charTypeParameter("padChar", default = ' ')) { self, length, padChar ->
            KnolusString(self.padEnd(length, padChar))
        }

        registerMemberPropertyGetter(stringTypeParameter(), "isEmpty") { self -> KnolusBoolean(self.isEmpty()) }
        registerMemberPropertyGetter(stringTypeParameter(), "isNotEmpty") { self -> KnolusBoolean(self.isNotEmpty()) }
        registerMemberPropertyGetter(stringTypeParameter(), "isBlank") { self -> KnolusBoolean(self.isBlank()) }
        registerMemberPropertyGetter(stringTypeParameter(), "isNotBlank") { self -> KnolusBoolean(self.isNotBlank()) }

        registerMemberFunction(stringTypeParameter(), "substring", numberTypeAsIntParameter("start"), numberTypeAsIntParameter("end").asOptional()) { self, start, end ->
            KnolusString(self.substring(start, end.getOrElse(self.length)))
        }

        registerMemberFunction(stringTypeParameter(), "substringBefore", charTypeParameter("delimiter"), stringTypeParameter("missingDelimiterValue").asOptional()) { self, delimiter, missing ->
            KnolusString(self.substringBefore(delimiter, missing.getOrElse(self)))
        }

        registerMemberFunction(stringTypeParameter(), "substringAfter", charTypeParameter("delimiter"), stringTypeParameter("missingDelimiterValue").asOptional()) { self, delimiter, missing ->
            KnolusString(self.substringAfter(delimiter, missing.getOrElse(self)))
        }

        registerMemberFunction(stringTypeParameter(), "substringBeforeLast", charTypeParameter("delimiter"), stringTypeParameter("missingDelimiterValue").asOptional()) { self, delimiter, missing ->
            KnolusString(self.substringBeforeLast(delimiter, missing.getOrElse(self)))
        }

        registerMemberFunction(stringTypeParameter(), "substringAfterLast", charTypeParameter("delimiter"), stringTypeParameter("missingDelimiterValue").asOptional()) { self, delimiter, missing ->
            KnolusString(self.substringAfterLast(delimiter, missing.getOrElse(self)))
        }

        registerMemberFunction(stringTypeParameter(), "replace", stringTypeParameter("oldValue"), stringTypeParameter("newValue"), booleanTypeParameter("ignoreCase", false)) { self, oldValue, newValue, ignoreCase ->
            KnolusString(self.replace(oldValue, newValue, ignoreCase))
        }

        registerFunction("str", doubleTypeParameter("double")) { double -> KnolusString(double.toString()) }
        registerFunction("str", numberTypeAsIntParameter("int"), intTypeParameter("base", 10)) { int, base -> KnolusString(int.toString(base)) }

        registerIntToString()
    }

    fun KnolusContext<*>.registerIntToString() {
        registerFunction(
            "hex",
            numberTypeAsIntParameter("num"),
            booleanTypeParameter("uppercase", false)
        ) { num, uppercase ->
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
        ) { num, length, uppercase ->
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
        ) { num, uppercase ->
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
        ) { num, length, uppercase ->
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
        ) { num, uppercase ->
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
        ) { num, length, uppercase ->
            buildStringVariable {
                append("0o")
                append(CharArray((length - 1 - (log(num.toDouble(), 8.0).toInt())).coerceAtLeast(0)) { '0' })

                if (uppercase) append(num.toString(8).toUpperCase())
                else append(num.toString(8))
            }
        }
    }
}