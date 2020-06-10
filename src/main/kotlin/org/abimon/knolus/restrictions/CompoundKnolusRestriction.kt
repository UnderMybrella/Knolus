package org.abimon.knolus.restrictions

import org.abimon.knolus.*
import org.abimon.knolus.context.KnolusContext
import org.abimon.knolus.types.KnolusTypedValue

@ExperimentalUnsignedTypes
class CompoundKnolusRestriction(
    val startingRestriction: KnolusRestrictions,
    val restrictions: List<KnolusRestrictions>,
) :
    KnolusRestrictions {
    companion object {
        fun of(vararg restrictions: KnolusRestrictions) =
            CompoundKnolusRestriction(restrictions[0],
                restrictions.drop(1))

        fun fromPermissive(vararg restrictions: KnolusRestrictions) =
            CompoundKnolusRestriction(KnolusBasePermissiveRestrictions.INSTANCE, restrictions.toList())
    }

    constructor(restrictions: List<KnolusRestrictions>) : this(restrictions[0], restrictions.drop(1))
    constructor(restrictions: Array<KnolusRestrictions>) : this(restrictions[0], restrictions.drop(1))

    override fun canGetVariable(context: KnolusContext, variableName: String): Boolean =
        restrictions.fold(startingRestriction.canGetVariable(context, variableName)) { acc, r ->
            acc && r.canGetVariable(context, variableName)
        }

    override fun canAskParentForVariable(child: KnolusContext, parent: KnolusContext, variableName: String): Boolean =
        restrictions.fold(startingRestriction.canAskParentForVariable(child, parent, variableName)) { acc, r ->
            acc && r.canAskParentForVariable(child, parent, variableName)
        }

    override fun canSetVariable(
        context: KnolusContext,
        variableName: String,
        variableValue: KnolusTypedValue,
        attemptedParentalSet: Boolean,
    ): Boolean = restrictions.fold(startingRestriction.canSetVariable(context,
        variableName,
        variableValue,
        attemptedParentalSet)) { acc, r ->
        acc && r.canSetVariable(context, variableName, variableValue, attemptedParentalSet)
    }

    override fun <T> canRegisterFunction(
        context: KnolusContext,
        functionName: String,
        function: KnolusFunction<T>,
        attemptedParentalRegister: KnolusResult<KnolusFunction<KnolusTypedValue?>>?,
    ): Boolean = restrictions.fold(startingRestriction.canRegisterFunction(context,
        functionName,
        function,
        attemptedParentalRegister)) { acc, r ->
        acc && r.canRegisterFunction(context, functionName, function, attemptedParentalRegister)
    }

    override fun canAskForFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean =
        restrictions.fold(startingRestriction.canAskForFunction(context, functionName, functionParameters)) { acc, r ->
            acc && r.canAskForFunction(context, functionName, functionParameters)
        }

    override fun canAskParentForFunction(
        child: KnolusContext,
        parent: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = restrictions.fold(startingRestriction.canAskParentForFunction(child,
        parent,
        functionName,
        functionParameters)) { acc, r ->
        acc && r.canAskParentForFunction(child, parent, functionName, functionParameters)
    }

    override fun canTryFunction(
        context: KnolusContext,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
        function: KnolusFunction<KnolusTypedValue?>,
    ): Boolean = restrictions.fold(startingRestriction.canTryFunction(context,
        functionName,
        functionParameters,
        function)) { acc, r ->
        acc && r.canTryFunction(context, functionName, functionParameters, function)
    }

    override fun canRunFunction(
        context: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): Boolean = restrictions.fold(startingRestriction.canRunFunction(context, function, parameters)) { acc, r ->
        acc && r.canRunFunction(context, function, parameters)
    }

    override fun canAskForMemberFunction(
        context: KnolusContext,
        member: KnolusTypedValue,
        functionName: String,
        functionParameters: Array<KnolusUnion.FunctionParameterType>,
    ): Boolean = restrictions.fold(startingRestriction.canAskForMemberFunction(context,
        member,
        functionName,
        functionParameters)) { acc, r ->
        acc && r.canAskForMemberFunction(context, member, functionName, functionParameters)
    }

    override fun canAskForMemberPropertyGetter(
        context: KnolusContext,
        member: KnolusTypedValue,
        propertyName: String,
    ): Boolean =
        restrictions.fold(startingRestriction.canAskForMemberPropertyGetter(context, member, propertyName)) { acc, r ->
            acc && r.canAskForMemberPropertyGetter(context, member, propertyName)
        }

    override fun canAskForOperatorFunction(
        context: KnolusContext,
        operator: ExpressionOperator,
        a: KnolusTypedValue,
        b: KnolusTypedValue,
    ): Boolean =
        restrictions.fold(startingRestriction.canAskForOperatorFunction(context, operator, a, b)) { acc, r ->
            acc && r.canAskForOperatorFunction(context, operator, a, b)
        }

    override fun createSubroutineRestrictions(
        currentContext: KnolusContext,
        function: KnolusFunction<KnolusTypedValue?>,
        parameters: Map<String, KnolusTypedValue>,
    ): KnolusRestrictions =
        restrictions.fold(mutableListOf(startingRestriction.createSubroutineRestrictions(currentContext,
            function,
            parameters))) { acc, r ->
            acc.withElement(r.createSubroutineRestrictions(currentContext, function, parameters))
        }.let { list ->
            CompoundKnolusRestriction(list.toTypedArray())
        }
}