package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

@ExperimentalUnsignedTypes
class CompoundKnolusRestriction<R>(val restrictions: List<KnolusRestriction<R>>, val startingValue: () -> KnolusResult<R>, val emptyResult: () -> KnolusResult<R>) : KnolusRestriction<R> {
    companion object {
        fun <R> of(vararg restrictions: KnolusRestriction<R>, emptyResult: () -> KnolusResult<R>) =
            CompoundKnolusRestriction(restrictions.toList(), KnolusResult.Empty.INSTANCE::invoke, emptyResult)

        fun fromPermissive(vararg restrictions: KnolusRestriction<StaticSuccess>) =
            CompoundKnolusRestriction(restrictions.toList(), KnolusResult.Empty.INSTANCE::invoke, StaticSuccess::invoke)

        fun <R> fromRestrictive(vararg restrictions: KnolusRestriction<R>) =
            CompoundKnolusRestriction(restrictions.toList(), KnolusResult.Empty.INSTANCE::invoke) { KnolusResult.empty<R>() }
    }

    constructor(restrictions: Array<KnolusRestriction<R>>, emptyResult: () -> KnolusResult<R>) : this(restrictions.toList(), KnolusResult.Empty.INSTANCE::invoke, emptyResult)

    override fun canGetVariable(context: KnolusContext<R>, variableName: String): KnolusResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canGetVariable(context, variableName) }
        }.switchIfEmpty(emptyResult)

    override fun canAskParentForVariable(child: KnolusContext<R>, parent: KnolusContext<R>, variableName: String): KnolusResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskParentForVariable(child, parent, variableName) }
        }.switchIfEmpty(emptyResult)

    override fun canSetVariable(
        context: KnolusContext<R>,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: KnolusResult<KnolusTypedValue?>?,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canSetVariable(context, variableName, variableValue, attemptedParentalSet) }
    }.switchIfEmpty(emptyResult)

    override fun <T> canRegisterFunction(
        context: KnolusContext<R>,
        functionName: String,
        function: KnolusFunction<T, R, *>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<T, R, *>>?,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canRegisterFunction(context, functionName, function, attemptedParentalRegister) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForFunction(
        context: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForFunction(context, functionName, functionParameters) }
        }.switchIfEmpty(emptyResult)

    override fun canAskParentForFunction(
        child: KnolusContext<R>,
        parent: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canAskParentForFunction(child, parent, functionName, functionParameters) }
    }.switchIfEmpty(emptyResult)

    override fun canTryFunction(
        context: KnolusContext<R>,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canTryFunction(context, functionName, functionParameters, function) }
    }.switchIfEmpty(emptyResult)

    override fun canRunFunction(
        context: KnolusContext<R>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canRunFunction(context, function, parameters) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForMemberFunction(
        context: KnolusContext<R>,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): KnolusResult<R> = restrictions.fold(startingValue()) { acc, r ->
        acc.switchIfEmpty { r.canAskForMemberFunction(context, member, functionName, functionParameters) }
    }.switchIfEmpty(emptyResult)

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext<R>,
        member: KnolusTypedValue,
        propertyName: String,
    ): KnolusResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForMemberPropertyGetter(context, member, propertyName) }
        }.switchIfEmpty(emptyResult)

    override fun canAskForOperatorFunction(
        context: KnolusContext<R>,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): KnolusResult<R> =
        restrictions.fold(startingValue()) { acc, r ->
            acc.switchIfEmpty { r.canAskForOperatorFunction(context, operator, a, b) }
        }.switchIfEmpty(emptyResult)

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext<R>,
        function: KnolusFunction<KnolusTypedValue?, R, *>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusResult<KnolusRestriction<R>> =
        restrictions.fold(KnolusResult.success(mutableListOf<KnolusRestriction<R>>())) { acc, r ->
            acc.flatMap { list -> r.createSubroutineRestrictions(currentContext, function, parameters).map(list::withElement) }
        }.map { list -> CompoundKnolusRestriction(list, startingValue, emptyResult) }
}