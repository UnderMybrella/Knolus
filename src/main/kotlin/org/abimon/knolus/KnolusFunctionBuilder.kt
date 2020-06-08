package org.abimon.knolus

import org.abimon.knolus.types.KnolusBoolean
import org.abimon.knolus.types.KnolusObject
import org.abimon.knolus.types.KnolusTypedValue

sealed class KnolusDeclaredFunctionParameter<T: KnolusTypedValue> {
    abstract val name: String
    abstract val defaultValue: T?

    data class Concrete<T: KnolusTypedValue>(override val name: String, val typeInfo: KnolusTypedValue.TypeInfo<T>, override val defaultValue: T?): KnolusDeclaredFunctionParameter<T>() {
        override fun matches(param: KnolusUnion.FunctionParameterType): Boolean = param.name == name && typeInfo.isInstance(param.parameter)
        override fun fits(param: KnolusUnion.FunctionParameterType): Boolean = (param.name == null || param.name == name) && typeInfo.isInstance(param.parameter)
    }

    data class Named(override val name: String, val typeName: String, override val defaultValue: KnolusTypedValue?): KnolusDeclaredFunctionParameter<KnolusTypedValue>() {
        override fun matches(param: KnolusUnion.FunctionParameterType): Boolean = param.name == name && typeName in param.parameter.typeInfo.typeHierarchicalNames
        override fun fits(param: KnolusUnion.FunctionParameterType): Boolean = (param.name == null || param.name == name) && typeName in param.parameter.typeInfo.typeHierarchicalNames
    }

    /** Do the passed parameter values match the declaration? */
    abstract fun matches(param: KnolusUnion.FunctionParameterType): Boolean
    /** Is the passed parameter compatible with this declaration */
    abstract fun fits(param: KnolusUnion.FunctionParameterType): Boolean
}

fun KnolusUnion.FunctionParameterType.matches(decl: KnolusDeclaredFunctionParameter<*>): Boolean = decl.matches(this)
fun KnolusUnion.FunctionParameterType.fits(decl: KnolusDeclaredFunctionParameter<*>): Boolean = decl.fits(this)

@ExperimentalUnsignedTypes
class KnolusFunction<T>(val parameters: Array<KnolusDeclaredFunctionParameter<*>>, val variadicSupported: Boolean = false, val func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> T) {
    suspend fun suspendInvoke(context: KnolusContext, parameters: Map<String, KnolusTypedValue>) = func(context, parameters)
}

class KnolusFunctionBuilder<T> {
    val parameters: MutableList<KnolusDeclaredFunctionParameter<*>> =
        ArrayList()
    var variadicSupported = false
    lateinit var func: suspend (context: KnolusContext, parameters: Map<String, KnolusTypedValue>) -> T

    fun addParameter(name: String, default: KnolusTypedValue? = null): KnolusFunctionBuilder<T> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(name.sanitiseFunctionIdentifier(), KnolusObject, default))

        return this
    }

    fun addParameter(spec: ParameterSpec<*, *>): KnolusFunctionBuilder<T> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(spec.name.sanitiseFunctionIdentifier(), spec.type, null))

        return this
    }

    fun <V: KnolusTypedValue> addParameter(spec: ParameterSpec<V, *>, default: V? = null): KnolusFunctionBuilder<T> {
        parameters.add(KnolusDeclaredFunctionParameter.Concrete(spec.name.sanitiseFunctionIdentifier(), spec.type, default))

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

fun <T> KnolusFunctionBuilder<T>.setFunction(func: suspend (context: KnolusContext) -> T): KnolusFunctionBuilder<T> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func(context)
    }
}

fun <T> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(func: suspend (context: KnolusContext) -> Unit): KnolusFunctionBuilder<T?> {
    return setFunction { context: KnolusContext, _: Map<String, KnolusTypedValue> ->
        func(context)

        null
    }
}

fun <T, P> KnolusFunctionBuilder<T>.setFunction(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> T): KnolusFunctionBuilder<T> {
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, P> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<*, P>, func: suspend (context: KnolusContext, parameter: P) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, P1, P2> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T>.setFunction(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, P1, P2, P3> KnolusFunctionBuilder<T?>.setFunctionWithoutReturn(firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}
//fun <T, (?:P|P1)(, P2(?:, P3)?)?> KnolusFunctionBuilder(<T\??>)\.(setFunction(?:WithoutReturn)?)\(
//fun <T, P0, P1$1> KnolusFunctionBuilder$2.$3(typeSpec: ParameterSpec<*, P0>,
/** Member Functions */

fun <T, P0> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)

        func(context, self)
    }
}

fun <T, P0> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, func: suspend (context: KnolusContext, self: P0) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)

        func(context, self)

        null
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)
    }
}

fun <T, P0, P1> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, parameterSpec: ParameterSpec<*, P1>, func: suspend (context: KnolusContext, self: P0, parameter: P1) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(parameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)

        null
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)
    }
}

fun <T, P0, P1, P2> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec.name)
    addParameter(secondParameterSpec.name)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T>.setMemberFunction(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, P0, P1, P2, P3> KnolusFunctionBuilder<T?>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0>, firstParameterSpec: ParameterSpec<*, P1>, secondParameterSpec: ParameterSpec<*, P2>, thirdParameterSpec: ParameterSpec<*, P3>, func: suspend (context: KnolusContext, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec)
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <T, V0 : KnolusTypedValue, P0, V1 : KnolusTypedValue, P1> KnolusFunctionBuilder<T>.setOperatorFunction(typeSpec: ParameterSpec<V0, P0>, parameterSpec: ParameterSpec<V1, P1>, func: suspend (context: KnolusContext, a: P0, b: P1) -> T): KnolusFunctionBuilder<T> {
    addParameter("a")
    addParameter("b")

    return setFunction { context: KnolusContext, parameters: Map<String, KnolusTypedValue> ->
        val a = typeSpec.transform(context, parameters.getValue("A") as V0)
        val b = parameterSpec.transform(context, parameters.getValue("B") as V1)

        func(context, a, b)
    }
}