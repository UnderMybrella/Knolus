package org.abimon.knolus.types

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.errors.common.successInline
import org.kornea.toolkit.common.KorneaTypeChecker

interface KnolusTypedValue {
    interface TypeInfo<T>: KorneaTypeChecker<T> {
        val typeHierarchicalNames: Array<String>
        val typeName: String
            get() = typeHierarchicalNames.first()

        fun getMemberPropertyGetterName(typeName: String, propertyName: String): String = "MemberProperty_${typeName}_${propertyName}"
        fun getMemberFunctionName(typeName: String, functionName: String): String = "MemberFunction_${typeName}_${functionName}"
        fun getMemberOperatorName(typeName: String, operator: ExpressionOperator): String = "MemberOperator_${typeName}_${operator.functionCallName}"
    }

    interface UnsureValue<out E: KnolusTypedValue>: KnolusTypedValue {
        suspend fun <T> needsEvaluation(context: KnolusContext<T>): Boolean
        suspend fun <T> evaluate(context: KnolusContext<T>): KorneaResult<E>
    }
    interface RuntimeValue<out E: KnolusTypedValue>: UnsureValue<E> {
        override suspend fun <T> needsEvaluation(context: KnolusContext<T>): Boolean = true

        override suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String> = evaluate(context).flatMap { it.asString(context) }
        override suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number> = evaluate(context).flatMap { it.asNumber(context) }
        override suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean> = evaluate(context).flatMap { it.asBoolean(context) }
    }

    //Generics work to go from TypeInfo -> TypedValue, not so much the other way around
    val typeInfo: TypeInfo<*>

    suspend fun <T> asString(context: KnolusContext<T>): KorneaResult<String>
    suspend fun <T> asNumber(context: KnolusContext<T>): KorneaResult<Number>
    suspend fun <T> asBoolean(context: KnolusContext<T>): KorneaResult<Boolean>
}

fun KnolusTypedValue.TypeInfo<*>.getMemberPropertyGetterNames(propertyName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberPropertyGetterName(typeName, propertyName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberFunctionNames(functionName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberFunctionName(typeName, functionName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberOperatorNames(operator: ExpressionOperator): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberOperatorName(typeName, operator) }

suspend fun <T, E : KnolusTypedValue> KnolusTypedValue.UnsureValue<E> .evaluateOrSelf(context: KnolusContext<T>): KorneaResult<KnolusTypedValue> = when (this) {
    is KnolusTypedValue.RuntimeValue -> evaluate(context)
    else -> if (needsEvaluation(context)) evaluate(context) else KorneaResult.successInline(this)
}