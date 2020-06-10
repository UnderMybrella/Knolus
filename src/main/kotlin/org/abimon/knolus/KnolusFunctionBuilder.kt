package org.abimon.knolus

import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusObject
import org.abimon.knolus.types.KnolusTypedValue

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
class KnolusFunction<T>(
    val parameters: Array<KnolusDeclaredFunctionParameter<*>>,
    val variadicSupported: Boolean = false,
    val func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> T,
) {
    suspend fun suspendInvoke(context: KnolusContext, parameters: Map<String, KnolusTypedValue>) =
        func(context, parameters)
}

class KnolusFunctionBuilder<T> {
    var defaultMissingPolicy: KnolusFunctionParameterMissingPolicy = KnolusFunctionParameterMissingPolicy.Mandatory
    val parameters: MutableList<KnolusDeclaredFunctionParameter<*>> =
        ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> T

    fun addMandatoryParameter(name: String) = addParameter(name, KnolusFunctionParameterMissingPolicy.Mandatory)
    fun addOptionalParameter(name: String) = addParameter(name, KnolusFunctionParameterMissingPolicy.Optional)
    fun addSubstitutedParameter(name: String, default: KnolusTypedValue) =
        addParameter(name, KnolusFunctionParameterMissingPolicy.Substitute(default))

    fun addParameter(
        name: String,
        missingPolicy: KnolusFunctionParameterMissingPolicy? = null,
    ): KnolusFunctionBuilder<T> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(
            name.sanitiseFunctionIdentifier(),
            KnolusObject,
            missingPolicy ?: defaultMissingPolicy
        ))

        return this
    }

    fun addMandatoryParameter(spec: ParameterSpec<*, *>) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Mandatory)

    fun addOptionalParameter(spec: ParameterSpec<*, *>) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Optional)

    fun <T : KnolusTypedValue> addSubstitutedParameter(spec: ParameterSpec<T, *>, default: T) =
        addParameter(spec, KnolusFunctionParameterMissingPolicy.Substitute(default))

    fun addParameter(
        spec: ParameterSpec<*, *>,
        missingPolicy: KnolusFunctionParameterMissingPolicy? = null,
    ): KnolusFunctionBuilder<T> {
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

    fun setFunction(func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> T): KnolusFunctionBuilder<T> {
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


fun <T, V0 : KnolusTypedValue, P0, V1 : KnolusTypedValue, P1> KnolusFunctionBuilder<T>.setOperatorFunction(
    typeSpec: ParameterSpec<V0, P0>,
    parameterSpec: ParameterSpec<V1, P1>,
    func: suspend (context: KnolusContext, a: P0, b: P1) -> T,
): KnolusFunctionBuilder<T> {
    addParameter(typeSpec.withName("a"))
    addParameter(parameterSpec.withName("b"))

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val a = typeSpec.transform(context, parameters.getValue("A") as V0)
        val b = parameterSpec.transform(context, parameters.getValue("B") as V1)

        func(context, a, b)
    }
}