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

    interface UnsureValue<out E: KnolusTypedValue>: KnolusTypedValue {
        suspend fun <T> needsEvaluation(context: KnolusContext<T>): Boolean
        suspend fun <T> evaluate(context: KnolusContext<T>): KnolusResult<E>
    }
    interface RuntimeValue<out E: KnolusTypedValue>: UnsureValue<E> {
        override suspend fun <T> needsEvaluation(context: KnolusContext<T>): Boolean = true

        override suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String> = evaluate(context).flatMap { it.asString(context) }
        override suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number> = evaluate(context).flatMap { it.asNumber(context) }
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean> = evaluate(context).flatMap { it.asBoolean(context) }
    }

    //Generics work to go from TypeInfo -> TypedValue, not so much the other way around
    val typeInfo: TypeInfo<*>

    suspend fun <T> asString(context: KnolusContext<T>): KnolusResult<String>
    suspend fun <T> asNumber(context: KnolusContext<T>): KnolusResult<Number>
    suspend fun <T> asBoolean(context: KnolusContext<T>): KnolusResult<Boolean>
}

fun KnolusTypedValue.TypeInfo<*>.getMemberPropertyGetterNames(propertyName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberPropertyGetterName(typeName, propertyName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberFunctionNames(functionName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberFunctionName(typeName, functionName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberOperatorNames(operator: ExpressionOperator): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberOperatorName(typeName, operator) }

suspend fun <T, E : KnolusTypedValue> KnolusTypedValue.UnsureValue<E> .evaluateOrSelf(context: KnolusContext<T>): KnolusResult<KnolusTypedValue> = when (this) {
    is KnolusTypedValue.RuntimeValue -> evaluate(context)
    else -> if (needsEvaluation(context)) evaluate(context) else KnolusResult.knolusTyped(this)
}