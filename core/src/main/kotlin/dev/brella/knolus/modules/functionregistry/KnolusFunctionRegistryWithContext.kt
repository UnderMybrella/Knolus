package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.KnolusFunction
import dev.brella.knolus.KnolusFunctionBuilder
import dev.brella.knolus.ParameterSpec
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.getValue
import dev.brella.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T, R, C>.setFunctionWithContext(func: suspend (context: C) -> T): KnolusFunctionBuilder<T, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> -> func(context) }
}

fun <T, R, C : KnolusContext<out R>> KnolusFunctionBuilder<T?, R, C>.setFunctionWithContextWithoutReturn(func: suspend (context: C) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    return setFunction { context: C, _: Map<String, KnolusTypedValue> ->
        func(context)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setFunctionWithContext(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (context: C, parameter: P) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setFunctionWithContextWithoutReturn(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend (context: C, parameter: P) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setFunctionWithContext(
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

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setFunctionWithContextWithoutReturn(
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

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setFunctionWithContext(
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

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setFunctionWithContextWithoutReturn(
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

fun <R, C : KnolusContext<out R>> KnolusContext<R>.registerFunctionWithContext(
    functionName: String,
    func: suspend (context: C) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContext(func)
        .build()
)

fun <R, C: KnolusContext<out R>> KnolusContext<R>.registerFunctionWithContextWithoutReturn(
    functionName: String,
    func: suspend (context: C) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunctionWithContext(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: P) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContext(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend (context: C, parameter: P) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContextWithoutReturn(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (context: C, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R> KnolusContext<R>.registerUntypedFunctionWithContext(
    functionName: String,
    func: suspend (context: KnolusContext<out R>) -> KnolusTypedValue,
): KorneaResult<KnolusFunction<KnolusTypedValue?, R, *>> = register(
    functionName,
    functionBuilder<R, KnolusContext<out R>>()
        .setFunctionWithContext(func)
        .build()
)

fun <R> KnolusContext<R>.registerUntypedFunctionWithContextWithoutReturn(
    functionName: String,
    func: suspend (context: KnolusContext<out R>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, KnolusContext<out R>>()
        .setFunctionWithContextWithoutReturn(func)
        .build()
)

/** Getter */

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberPropertyGetterWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    propertyName: String,
    func: suspend (context: C, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberPropertyGetterName(propertyName),
    functionBuilder<R, C>()
        .setFunctionWithContext(typeSpec, func)
        .build()
)

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T, R, C>.setResultFunctionWithContext(parameterSpec: ParameterSpec<*, P, in R, C>, func: suspend(context: C, parameter: KorneaResult<P>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithContextWithoutReturn(
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend(context: C, parameter: KorneaResult<P>) -> Unit
): KnolusFunctionBuilder<T?, R, C> {
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val param = parameters.getValue(context, parameterSpec)

        func(context, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T, R, C>.setResultFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T
): KnolusFunctionBuilder<T, R, C> {
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P1, P2> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit
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

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setResultFunctionWithContext(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T
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

fun <T, R, C : KnolusContext<out R>, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setResultFunctionWithContextWithoutReturn(
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit
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

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunctionWithContext(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend(context: C, parameter: KorneaResult<P>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContext(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P> KnolusContext<R>.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    parameterSpec: ParameterSpec<*, P, in R, C>,
    func: suspend(context: C, parameter: KorneaResult<P>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContextWithoutReturn(parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerResultFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2> KnolusContext<R>.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerResultFunctionWithContext(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> KnolusTypedValue,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContext(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P1, P2, P3> KnolusContext<R>.registerResultFunctionWithContextWithoutReturn(
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend(context: C, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit,
) = register(
    functionName,
    functionBuilder<R, C>()
        .setResultFunctionWithContextWithoutReturn(firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)