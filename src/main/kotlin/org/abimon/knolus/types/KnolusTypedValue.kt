package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext

interface KnolusTypedValue {
    interface TypeInfo<T> {
        val typeHierarchicalNames: Array<String>
        val typeName: String
            get() = typeHierarchicalNames.first()

        fun getMemberPropertyGetterName(typeName: String, propertyName: String): String = "MemberProperty_${typeName}_${propertyName}"
        fun getMemberFunctionName(typeName: String, functionName: String): String = "MemberFunction_${typeName}_${functionName}"
        fun getMemberOperatorName(typeName: String, operator: ExpressionOperator): String = "MemberOperator_${typeName}_${operator.functionCallName}"

        fun isInstance(value: KnolusTypedValue): Boolean
    }

    interface UnsureValue: KnolusTypedValue {
        suspend fun needsEvaluation(context: KnolusContext): Boolean
        suspend fun evaluate(context: KnolusContext): KnolusTypedValue
    }
    interface RuntimeValue: UnsureValue {
        override suspend fun needsEvaluation(context: KnolusContext): Boolean = true
    }

    //Generics work to go from TypeInfo -> TypedValue, not so much the other way around
    val typeInfo: TypeInfo<*>

    suspend fun asString(context: KnolusContext): String
    suspend fun asNumber(context: KnolusContext): Number
    suspend fun asBoolean(context: KnolusContext): Boolean

}

fun KnolusTypedValue.TypeInfo<*>.getMemberPropertyGetterNames(propertyName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberPropertyGetterName(typeName, propertyName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberFunctionNames(functionName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberFunctionName(typeName, functionName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberOperatorNames(operator: ExpressionOperator): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberOperatorName(typeName, operator) }

suspend fun KnolusTypedValue.evaluateOrSelf(context: KnolusContext): KnolusTypedValue = when (this) {
    is KnolusTypedValue.RuntimeValue -> evaluate(context)
    is KnolusTypedValue.UnsureValue -> if (needsEvaluation(context)) evaluate(context) else this
    else -> this
}