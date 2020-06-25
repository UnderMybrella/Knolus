package org.abimon.knolus

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusObject
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

sealed class KnolusDeclaredFunctionParameter<T : KnolusTypedValue> {
    abstract val name: String
    abstract val missingPolicy: KnolusFunctionParameterMissingPolicy

    data class Concrete<T : KnolusTypedValue>(
        override val name: String, val typeInfo: KnolusTypedValue.TypeInfo<T>,
        override val missingPolicy: KnolusFunctionParameterMissingPolicy,
    ) : KnolusDeclaredFunctionParameter<T>() {
        override fun matches(param: KnolusUnion.FunctionParameterType): Boolean =
            param.name == name && typeInfo.isInstance(param.parameter)

        override fun fits(param: KnolusUnion.FunctionParameterType): Boolean =
            (param.name == null || param.name == name) && typeInfo.isInstance(param.parameter)

        init {
            if (missingPolicy is KnolusFunctionParameterMissingPolicy.Substitute<*>) require(typeInfo.isInstance(missingPolicy.default))
        }
    }

    data class Named(
        override val name: String,
        val typeName: String,
        override val missingPolicy: KnolusFunctionParameterMissingPolicy,
    ) : KnolusDeclaredFunctionParameter<KnolusTypedValue>() {
        override fun matches(param: KnolusUnion.FunctionParameterType): Boolean =
            param.name == name && typeName in param.parameter.typeInfo.typeHierarchicalNames

        override fun fits(param: KnolusUnion.FunctionParameterType): Boolean =
            (param.name == null || param.name == name) && typeName in param.parameter.typeInfo.typeHierarchicalNames

        init {
            if (missingPolicy is KnolusFunctionParameterMissingPolicy.Substitute<*>) require(typeName in missingPolicy.default.typeInfo.typeHierarchicalNames)
        }
    }

    /** Do the passed parameter values match the declaration? */
    abstract fun matches(param: KnolusUnion.FunctionParameterType): Boolean

    /** Is the passed parameter compatible with this declaration */
    abstract fun fits(param: KnolusUnion.FunctionParameterType): Boolean
}

sealed class KnolusFunctionParameterMissingPolicy {
    object Mandatory : KnolusFunctionParameterMissingPolicy()
    object Optional : KnolusFunctionParameterMissingPolicy()
    data class Substitute<T : KnolusTypedValue>(val default: T) : KnolusFunctionParameterMissingPolicy()
}

fun KnolusUnion.FunctionParameterType.matches(decl: KnolusDeclaredFunctionParameter<*>): Boolean = decl.matches(this)
fun KnolusUnion.FunctionParameterType.fits(decl: KnolusDeclaredFunctionParameter<*>): Boolean = decl.fits(this)

@ExperimentalUnsignedTypes
class KnolusFunction<out T, R, in C: KnolusContext<out R>>(
    val parameters: Array<KnolusDeclaredFunctionParameter<*>>,
    val variadicSupported: Boolean = false,
    val func: suspend (context: C, parameters: Map<String, KnolusTypedValue>) -> T,
) {
    suspend fun suspendInvoke(context: C, parameters: Map<String, KnolusTypedValue>) =
        func(context, parameters)
}

class KnolusFunctionBuilder<T, R, C: KnolusContext<out R>> {
    var defaultMissingPolicy: KnolusFunctionParameterMissingPolicy = KnolusFunctionParameterMissingPolicy.Mandatory
    val parameters: MutableList<KnolusDeclaredFunctionParameter<*>> =
        ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (context: C, parameters: Map<String, KnolusTypedValue>) -> T

    fun addMandatoryParameter(name: String) = addParameter(name, KnolusFunctionParameterMissingPolicy.Mandatory)
    fun addOptionalParameter(name: String) = addParameter(name, KnolusFunctionParameterMissingPolicy.Optional)
    fun addSubstitutedParameter(name: String, default: KnolusTypedValue) =
        addParameter(name, KnolusFunctionParameterMissingPolicy.Substitute(default))

    fun addParameter(
        name: String,
        missingPolicy: KnolusFunctionParameterMissingPolicy? = null,
    ): KnolusFunctionBuilder<T, R, C> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(
            name.sanitiseFunctionIdentifier(),
            KnolusObject,
            missingPolicy ?: defaultMissingPolicy
        ))

        return this
    }

    fun addMandatoryParameter(spec: ParameterSpec<*, *, in R, C>) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Mandatory)

    fun addOptionalParameter(spec: ParameterSpec<*, *, in R, C>) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Optional)

    fun <T : KnolusTypedValue> addSubstitutedParameter(spec: ParameterSpec<T, *, in R, C>, default: T) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Substitute(default))

    fun addParameter(
        spec: ParameterSpec<*, *, *, *>,
        missingPolicy: KnolusFunctionParameterMissingPolicy? = null,
    ): KnolusFunctionBuilder<T, R, C> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(
            spec.name.sanitiseFunctionIdentifier(),
            spec.type,
            missingPolicy ?: if (spec.default == null) KnolusFunctionParameterMissingPolicy.Mandatory
            else KnolusFunctionParameterMissingPolicy.Optional
        ))

        return this
    }

//    fun addFlag(name: String, default: Boolean = false): KnolusFunctionBuilder<T> = addParameter(booleanTypeParameter(name),
//        KnolusBoolean(default)
//    )

    fun setFunction(func: suspend (context: C, parameters: Map<String, KnolusTypedValue>) -> T): KnolusFunctionBuilder<T, R, C> {
        this.func = func

        return this
    }

    fun build() = KnolusFunction(
        parameters.toTypedArray(),
        variadicSupported = variadicSupported,
        func = func
    )
}

//fun <T, (?:P|P1)(, P2(?:, P3)?)?> KnolusFunctionBuilder(<T\??>)\.(setFunction(?:WithoutReturn)?)\(
//fun <T, P0, P1$1> KnolusFunctionBuilder$2.$3(typeSpec: ParameterSpec<*, P0>,
/** Member Functions */


fun <R, C: KnolusContext<out R>, T, V0 : KnolusTypedValue, P0, V1 : KnolusTypedValue, P1> KnolusFunctionBuilder<T, R, C>.setOperatorFunction(
    typeSpec: ParameterSpec<V0, P0, in R, C>,
    parameterSpec: ParameterSpec<V1, P1, in R, C>,
    func: suspend (a: P0, b: P1) -> T,
): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec.withName("a"))
    addParameter(parameterSpec.withName("b"))

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val a = typeSpec.transform(parameters.getValue("A") as V0, context).get()
        val b = parameterSpec.transform(parameters.getValue("B") as V1, context).get()

        func(a, b)
    }
}

fun <T, R, C: KnolusContext<out R>, V0 : KnolusTypedValue, P0, V1 : KnolusTypedValue, P1> KnolusFunctionBuilder<T, R, C>.setOperatorResultFunction(
    typeSpec: ParameterSpec<V0, P0, in R, C>,
    parameterSpec: ParameterSpec<V1, P1, in R, C>,
    func: suspend (a: KorneaResult<P0>, b: KorneaResult<P1>) -> T,
): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec.withName("a"))
    addParameter(parameterSpec.withName("b"))

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val a = typeSpec.transform(parameters.getValue("A") as V0, context)
        val b = parameterSpec.transform(parameters.getValue("B") as V1, context)

        func(a, b)
    }
}