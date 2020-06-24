package org.abimon.knolus.modules.functionregistry

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue
import org.abimon.kornea.errors.common.KorneaResult

fun <T, R, C : KnolusContext<out R>, P0> KnolusFunctionBuilder<T, R, C>.setMemberFunction(typeSpec: ParameterSpec<*, P0, in R, C>, func: suspend (self: P0) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(self)
    }
}

fun <T, R, C : KnolusContext<out R>, P0> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, func: suspend (self: P0) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()

        func(self)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T, R, C>.setMemberFunction(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (self: P0, parameter: P1) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(self, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (self: P0, parameter: P1) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec).get()

        func(self, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T, R, C>.setMemberFunction(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(self, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()

        func(self, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setMemberFunction(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(self, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setMemberFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec).get()
        val secondParam = parameters.getValue(context, secondParameterSpec).get()
        val thirdParam = parameters.getValue(context, thirdParameterSpec).get()

        func(self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    func: suspend (self: P0) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunction(typeSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0> KnolusContext<R>.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    func: suspend (self: P0) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithoutReturn(typeSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (self: P0, parameter: P1) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (self: P0, parameter: P1) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusContext<R>.registerMemberFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunction(typeSpec, firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusContext<R>.registerMemberFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    thirdParameterSpec: ParameterSpec<*, P3, in R, C>,
    func: suspend (self: P0, firstParameter: P1, secondParameter: P2, thirdParameter: P3) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, thirdParameterSpec, func)
        .build()
)

/** Result Functions */

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T, R, C>.setMemberResultFunction(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (self: P0, parameter: KorneaResult<P1>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(self, param)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, parameterSpec: ParameterSpec<*, P1, in R, C>, func: suspend (self: P0, parameter: KorneaResult<P1>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(parameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val param = parameters.getValue(context, parameterSpec)

        func(self, param)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T, R, C>.setMemberResultFunction(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(self, firstParam, secondParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)

        func(self, firstParam, secondParam)

        null
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T, R, C>.setMemberResultFunction(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> T): KnolusFunctionBuilder<T, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(self, firstParam, secondParam, thirdParam)
    }
}

fun <T, R, C : KnolusContext<out R>, P0, P1, P2, P3> KnolusFunctionBuilder<T?, R, C>.setMemberResultFunctionWithoutReturn(typeSpec: ParameterSpec<*, P0, in R, C>, firstParameterSpec: ParameterSpec<*, P1, in R, C>, secondParameterSpec: ParameterSpec<*, P2, in R, C>, thirdParameterSpec: ParameterSpec<*, P3, in R, C>, func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>, thirdParameter: KorneaResult<P3>) -> Unit): KnolusFunctionBuilder<T?, R, C> {
    addParameter(typeSpec)
    addParameter(firstParameterSpec)
    addParameter(secondParameterSpec)
    addParameter(thirdParameterSpec)

    return setFunction { context: C, parameters: Map<String, KnolusTypedValue> ->
        val self = parameters.getValue(context, typeSpec).get()
        val firstParam = parameters.getValue(context, firstParameterSpec)
        val secondParam = parameters.getValue(context, secondParameterSpec)
        val thirdParam = parameters.getValue(context, thirdParameterSpec)

        func(self, firstParam, secondParam, thirdParam)

        null
    }
}

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberResultFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (self: P0, parameter: KorneaResult<P1>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunction(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1> KnolusContext<R>.registerMemberResultFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    parameterSpec: ParameterSpec<*, P1, in R, C>,
    func: suspend (self: P0, parameter: KorneaResult<P1>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithoutReturn(typeSpec, parameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberResultFunction(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> KnolusTypedValue,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunction(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)

fun <R, C : KnolusContext<out R>, P0, P1, P2> KnolusContext<R>.registerMemberResultFunctionWithoutReturn(
    typeSpec: ParameterSpec<*, P0, in R, C>,
    functionName: String,
    firstParameterSpec: ParameterSpec<*, P1, in R, C>,
    secondParameterSpec: ParameterSpec<*, P2, in R, C>,
    func: suspend (self: P0, firstParameter: KorneaResult<P1>, secondParameter: KorneaResult<P2>) -> Unit,
) = register(
    typeSpec.getMemberFunctionName(functionName),
    functionBuilder<R, C>()
        .setMemberResultFunctionWithoutReturn(typeSpec, firstParameterSpec, secondParameterSpec, func)
        .build()
)