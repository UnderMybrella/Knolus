package dev.brella.knolus.modules.functionregistry

import dev.brella.knolus.*
import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult


fun <T, R, C : KnolusContext<out R>, P0> KnolusFunctionBuilder<T, R, C>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, func: suspend (context: C, self: P0) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(context, self)
    }
}

fun <T, R, C : KnolusContext<out R>, P0> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, func: suspend (context: C, self: P0) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(context, self)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T, R, C>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (context: C, self: P0, parameter: P1) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, self, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (context: C, self: P0, parameter: P1) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(context, self, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T, R, C>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, self, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setMemberFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    func: suspend (context: C, self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContext(typeSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    func: suspend (context: C, self: P0) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (context: C, self: P0, parameter: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContext(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (context: C, self: P0, parameter: P1) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

/** Result Functions */

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T, R, C>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(context, self, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T, R, C>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(context, self, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setMemberResultFunctionWithContext(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithContextWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(context, self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberResultFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithContext(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberResultFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (context: C, self: P0, parameter: KorneaResult<P1>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithContextWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberResultFunctionWithContext(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithContext(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberResultFunctionWithContextWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (context: C, self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithContextWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)