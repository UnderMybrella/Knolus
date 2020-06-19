package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

//<T(.*?)> KnolusFunctionBuilder<(T\??)>
//<T, R$1> KnolusFunctionBuilder<$2, R>

inline fun <R, C : KnolusContext<out R>> functionBuilder() = KnolusFunctionBuilder<KnolusTypedValue?, R, C>()

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T, R, C>.setFunction(func: suspend (context: C) -> T): KnolusFunctionBuilder<T, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> -> func(context) }
}

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(func: suspend (context: C) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> ->
        func(context)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setFunction(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (context: C, parameter: P) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (context: C, parameter: P) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>> KnolusContext<R>.registerFunction(
    functionName: String,
    func: suspend (context: C) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(func)
        .build()
)

fun <R, C: KnolusContext<out R>> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    func: suspend (context: C) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunction(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: P) -> Unit,
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
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
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
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> Unit,
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
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
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
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)


/** Getter */

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberPropertyGetter(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    propertyName: String,
    func: suspend (context: C, self: P0) -> KnolusTypedValue,
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

/** Result Functions */
fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setResultFunction(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (context: C, parameter: KorneaResult<P>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setResultFunction(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunction(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunction(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunctionWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: KorneaResult<P>) -> Unit,
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
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
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
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
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
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
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
    func: suspend (context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
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
    func: suspend (context: C, a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
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
    func: suspend (context: C, a: KorneaResult<P0>, b: KorneaResult<P1>) -> KnolusTypedValue,
) = parameterSpecs.forEach { parameterSpec ->
    register(
        typeSpec.getMemberOperatorName(operator),
        functionBuilder<R, C>()
            .setOperatorResultFunction(typeSpec, parameterSpec, func)
            .build()
    )
}