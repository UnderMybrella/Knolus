package dev.brella.knolus.types

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.toolkit.common.KorneaTypeChecker

interface KnolusTypedValue {
    interface TypeInfo<T> : KorneaTypeChecker<T> {
        val typeHierarchicalNames: Array<String>
        val typeName: String
            get() = typeHierarchicalNames.first()

        fun getMemberPropertyGetterName(typeName: String, propertyName: String): String = "MemberProperty_${typeName}_${propertyName}"
        fun getMemberFunctionName(typeName: String, functionName: String): String = "MemberFunction_${typeName}_${functionName}"
        fun getMemberOperatorName(typeName: String, operator: ExpressionOperator): String = "MemberOperator_${typeName}_${operator.functionCallName}"
        fun getMemberCastingOperatorName(typeName: String, castingTo: String): String = "MemberCast_${typeName}_${castingTo}"
    }

    interface UnsureValue<out E : KnolusTypedValue> : KnolusTypedValue {
        suspend fun needsEvaluation(context: KnolusContext): Boolean
        suspend fun evaluate(context: KnolusContext): KorneaResult<E>
    }

    interface RuntimeValue<out E : KnolusTypedValue> : UnsureValue<E> {
        override suspend fun needsEvaluation(context: KnolusContext): Boolean = true

        override suspend fun <R : KnolusTypedValue, I : TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> = evaluate(context).flatMap { it.asType(context, typeInfo) }
    }

    //Generics work to go from TypeInfo -> TypedValue, not so much the other way around
    val typeInfo: TypeInfo<*>

    suspend fun <R : KnolusTypedValue, I : TypeInfo<R>> asTypeImpl(context: KnolusContext, typeInfo: I): KorneaResult<R> = KorneaResult.empty()
}

//suspend fun <S: KnolusTypedValue, T, R: KnolusTypedValue, I: KnolusTypedValue.TypeInfo<R>> S.asTypeAbs(context: KnolusContext<T>, typeInfo: I): KorneaResult<R> = context.invokeCastingOperator(this, typeInfo)
suspend fun <S : KnolusTypedValue, R : KnolusTypedValue, I : KnolusTypedValue.TypeInfo<R>> S.asType(context: KnolusContext, typeInfo: I): KorneaResult<R> =
    asTypeImpl(context, typeInfo).switchIfEmpty { context.invokeCastingOperator(this, typeInfo) }

suspend fun KnolusTypedValue.asString(context: KnolusContext): KorneaResult<String> = asType(context, KnolusString).map(KnolusString::string)
suspend fun KnolusTypedValue.asNumber(context: KnolusContext): KorneaResult<Number> = asType(context, KnolusNumericalType).map(KnolusNumericalType::number)
suspend fun KnolusTypedValue.asBoolean(context: KnolusContext): KorneaResult<Boolean> = asType(context, KnolusBoolean).map(KnolusBoolean::boolean)

fun KnolusTypedValue.TypeInfo<*>.getMemberPropertyGetterNames(propertyName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberPropertyGetterName(typeName, propertyName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberFunctionNames(functionName: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberFunctionName(typeName, functionName) }
fun KnolusTypedValue.TypeInfo<*>.getMemberOperatorNames(operator: ExpressionOperator): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberOperatorName(typeName, operator) }
fun KnolusTypedValue.TypeInfo<*>.getMemberCastingOperatorNames(castingTo: String): Array<String> = typeHierarchicalNames.mapToArray { typeName -> getMemberCastingOperatorName(typeName, castingTo) }

fun <T> KnolusTypedValue.TypeInfo<T>.asResult(instance: Any?): KorneaResult<T> = KorneaResult.success(asInstance(instance), null)
fun <T> KnolusTypedValue.TypeInfo<T>.asResultOrEmpty(instance: Any?): KorneaResult<T> = KorneaResult.successOrEmpty(asInstanceSafe(instance), null)

suspend fun <E : KnolusTypedValue> KnolusTypedValue.UnsureValue<E>.evaluateOrSelf(context: KnolusContext): KorneaResult<KnolusTypedValue> = when (this) {
    is KnolusTypedValue.RuntimeValue -> evaluate(context)
    else -> if (needsEvaluation(context)) evaluate(context) else KorneaResult.successInline(this)
}