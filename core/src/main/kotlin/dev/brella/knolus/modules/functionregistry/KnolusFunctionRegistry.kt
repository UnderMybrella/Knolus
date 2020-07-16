@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.kornea.annotations.AvailableSince
import dev.brella.kornea.errors.common.KorneaResult

//<T(.*?)> KnolusFunctionBuilder<(T\??)>
//<T, R$1> KnolusFunctionBuilder<$2, R>

inline fun <R, C : KnolusContext<out R>> functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?, R, C>()
inline fun <T: KnolusTypedValue?, R, C : KnolusContext<out R>> typedFunctionBuilder() = KnolusFunctionBuilder<T, R, C>()

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T, R, C>.setFunction(func: suspend () -> T): KnolusFunctionBuilder<T, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> -> func() }
}

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(func: suspend () -> Unit): KnolusFunctionBuilder<T?, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> ->
        func()

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setFunction(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (parameter: P) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (parameter: P) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>> KnolusContext<R>.registerFunction(
    functionName: String,
    func: suspend () -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(func)
        .build()
)

fun <R, C: KnolusContext<out R>> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    func: suspend () -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (parameter: P) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R> KnolusContext<R>.registerUntypedFunction(
    functionName: String,
    func: suspend (context: KnolusContext<out R>) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> = register(
    functionName,
    functionBuilder<R, KnolusContext<out R>>()
        .setFunctionWithContext(func)
        .build()
)

fun <R> KnolusContext<R>.registerUntypedFunctionWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext<out R>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, KnolusContext<out R>>()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

/** Getter */

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberPropertyGetter(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    propertyName: String,
    func: suspend (self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder<R, C>()
        .setFunction(typeSpec, func)
        .build()
)

/** Operator */

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerOperatorFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    operator: ExpressionOperator,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (a: P0, b: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberOperatorName(operator),
    functionBuilder<R, C>()
        .setOperatorFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiOperatorFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    operator: ExpressionOperator,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (a: P0, b: P1) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder<R, C>()
            .setOperatorFunction(typeSpec, parameterSpec, func)
            .build()
    )
}

@AvailableSince(Knolus.VERSION_1_4_0)
fun <R, C : KnolusContext<out R>, P0, R0: KnolusTypedValue?, T: KnolusTypedValue.TypeInfo<R0>> KnolusContext<R>.registerCastingOperatorFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    castingTo: T,
    func: suspend (self: P0) -> R0,
) = register(
    typeSpec.getMemberCastingOperatorName(castingTo),
    typedFunctionBuilder<R0, R, C>()
        .setMemberFunction(typeSpec, func)
        .build()
)

/** Result Functions */
fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setResultFunction(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (parameter: KorneaResult<P>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (parameter: KorneaResult<P>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (parameter: KorneaResult<P>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunction(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (parameter: KorneaResult<P>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithoutReturn(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerResultFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunction(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerResultFunction(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunction(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerResultFunctionWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

/** Operator */

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerOperatorResultFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    operator: ExpressionOperator,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberOperatorName(operator),
    functionBuilder<R, C>()
        .setOperatorResultFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMultiOperatorResultFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    operator: ExpressionOperator,
    parameterSpecs: Array<ParameterSpec<*, P1, in R, C>>,
    func: suspend (a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder<R, C>()
            .setOperatorResultFunction(typeSpec, parameterSpec, func)
            .build()
    )
}