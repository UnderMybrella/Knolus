package org.abimon.knolus

object ArrayCoercion {
    public suspend inline fun <T : Any, reified R : Any> Array<T>.coerceArrayWith(
        context: KnolusContext,
        value: VariableValue,
        block: suspend (context: KnolusContext, array: Array<R>, value: VariableValue) -> Array<R>,
    ): Array<T>? {
        @Suppress("UNCHECKED_CAST")
        return block(context, if (R::class.java.isAssignableFrom(this::class.java.componentType)) this as Array<R> else return null, value) as Array<T>
    }

    suspend fun <T : VariableValue> addByCoercion(array: Array<T>, context: KnolusContext, value: VariableValue): Array<T> =
        array.coerceArrayWith(context, value, this::addByCoercionStringComponents)
            ?: array.coerceArrayWith(context, value, this::addByCoercionStringType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionBooleanType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionCharType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionIntegerType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionDecimalType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionVariableReferenceType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionMemberVariableReferenceType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionFunctionCallType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionMemberFunctionCallType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionArrayType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionExpressionType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionNullType)
            ?: array.coerceArrayWith(context, value, this::addByCoercionUndefinedType)
            ?: (array as Array<VariableValue>).plus(value) as Array<T>

    suspend fun addByCoercionStringComponents(
        context: KnolusContext,
        array: Array<VariableValue.StringComponents>,
        value: VariableValue,
    ): Array<VariableValue.StringComponents> =
        when (value) {
            is VariableValue.StringComponents -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.StringComponents>()) array.plus(value.array as Array<out VariableValue.StringComponents>)
                else array.plus(
                    VariableValue.StringComponents(value.array.mapToArray {
                        addByCoersionStringComponentsFlatten(context, it)
                    })
                )
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionStringComponents(context, array, value.flatten(context))
            else -> array.plus(VariableValue.StringComponents(arrayOf(KnolusUnion.StringComponent.RawText(value.asString(
                context)))))
        }

    suspend fun addByCoersionStringComponentsFlatten(
        context: KnolusContext,
        subval: VariableValue,
    ): KnolusUnion.StringComponent = when (subval) {
        is VariableValue.VariableReferenceType -> KnolusUnion.StringComponent.VariableReference(subval.variableName)
        is VariableValue.MemberVariableReferenceType -> addByCoersionStringComponentsFlatten(context,
            subval.flatten(context))
        is VariableValue.FunctionCallType -> addByCoersionStringComponentsFlatten(context, subval.flatten(context))
        is VariableValue.MemberFunctionCallType -> addByCoersionStringComponentsFlatten(context,
            subval.flatten(context))
        is VariableValue.ExpressionType -> addByCoersionStringComponentsFlatten(context, subval.flatten(context))
        else -> KnolusUnion.StringComponent.RawText(subval.asString(context))
    }

    suspend fun addByCoercionStringType(
        context: KnolusContext,
        array: Array<VariableValue.StringType>,
        value: VariableValue,
    ): Array<VariableValue.StringType> =
        when (value) {
            is VariableValue.StringType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.StringType>()) array.plus(value.array as Array<out VariableValue.StringType>)
                else array.plus(value.array.mapToArray { VariableValue.StringType(it.asString(context)) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionStringType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.StringType(value.asString(context)))
        }

    suspend fun addByCoercionBooleanType(
        context: KnolusContext,
        array: Array<VariableValue.BooleanType>,
        value: VariableValue,
    ): Array<VariableValue.BooleanType> =
        when (value) {
            is VariableValue.BooleanType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.BooleanType>()) array.plus(value.array as Array<out VariableValue.BooleanType>)
                else array.plus(value.array.mapToArray { VariableValue.BooleanType(it.asBoolean(context)) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionBooleanType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.BooleanType(value.asBoolean(context)))
        }

    suspend fun addByCoercionCharType(
        context: KnolusContext,
        array: Array<VariableValue.CharType>,
        value: VariableValue,
    ): Array<VariableValue.CharType> =
        when (value) {
            is VariableValue.CharType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.CharType>()) array.plus(value.array as Array<out VariableValue.CharType>)
                else array.plus(value.array.mapToArray { VariableValue.CharType(it.asNumber(context).toChar()) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionCharType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.CharType(value.asNumber(context).toChar()))
        }

    suspend fun addByCoercionIntegerType(
        context: KnolusContext,
        array: Array<VariableValue.IntegerType>,
        value: VariableValue,
    ): Array<VariableValue.IntegerType> =
        when (value) {
            is VariableValue.IntegerType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.IntegerType>()) array.plus(value.array as Array<out VariableValue.IntegerType>)
                else array.plus(value.array.mapToArray { VariableValue.IntegerType(it.asNumber(context).toInt()) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionIntegerType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.IntegerType(value.asNumber(context).toInt()))
        }

    suspend fun addByCoercionDecimalType(
        context: KnolusContext,
        array: Array<VariableValue.DecimalType>,
        value: VariableValue,
    ): Array<VariableValue.DecimalType> =
        when (value) {
            is VariableValue.DecimalType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.DecimalType>()) array.plus(value.array as Array<out VariableValue.DecimalType>)
                else array.plus(value.array.mapToArray { VariableValue.DecimalType(it.asNumber(context).toDouble()) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionDecimalType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.DecimalType(value.asNumber(context).toDouble()))
        }

    suspend fun addByCoersionVariableReferenceTypeFlatten(
        context: KnolusContext,
        subval: VariableValue,
    ): VariableValue.VariableReferenceType = when (subval) {
        is VariableValue.VariableReferenceType -> subval

        is VariableValue.MemberVariableReferenceType -> addByCoersionVariableReferenceTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.FunctionCallType -> addByCoersionVariableReferenceTypeFlatten(context, subval.flatten(context))
        is VariableValue.MemberFunctionCallType -> addByCoersionVariableReferenceTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.ExpressionType -> addByCoersionVariableReferenceTypeFlatten(context, subval.flatten(context))

        else -> VariableValue.VariableReferenceType(subval.asString(context))
    }

    suspend fun addByCoercionVariableReferenceType(
        context: KnolusContext,
        array: Array<VariableValue.VariableReferenceType>,
        value: VariableValue,
    ): Array<VariableValue.VariableReferenceType> =
        when (value) {
            is VariableValue.VariableReferenceType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.VariableReferenceType>()) array.plus(value.array as Array<out VariableValue.VariableReferenceType>)
                else array.plus(value.array.mapToArray { addByCoersionVariableReferenceTypeFlatten(context, it) })
            }
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionVariableReferenceType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.VariableReferenceType(value.asString(context)))
        }

    suspend fun addByCoersionMemberVariableReferenceTypeFlatten(
        context: KnolusContext,
        subval: VariableValue,
    ): VariableValue.MemberVariableReferenceType = when (subval) {
        is VariableValue.MemberVariableReferenceType -> subval

        is VariableValue.VariableReferenceType -> addByCoersionMemberVariableReferenceTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.FunctionCallType -> addByCoersionMemberVariableReferenceTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.MemberFunctionCallType -> addByCoersionMemberVariableReferenceTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.ExpressionType -> addByCoersionMemberVariableReferenceTypeFlatten(context,
            subval.flatten(context))

        else -> VariableValue.MemberVariableReferenceType(subval.asString(context), "type")
    }

    suspend fun addByCoercionMemberVariableReferenceType(
        context: KnolusContext,
        array: Array<VariableValue.MemberVariableReferenceType>,
        value: VariableValue,
    ): Array<VariableValue.MemberVariableReferenceType> =
        when (value) {
            is VariableValue.MemberVariableReferenceType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.MemberVariableReferenceType>()) array.plus(value.array as Array<out VariableValue.MemberVariableReferenceType>)
                else array.plus(value.array.mapToArray { addByCoersionMemberVariableReferenceTypeFlatten(context, it) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.MemberVariableReferenceType(value.asString(context), "type"))
        }

    suspend fun addByCoersionFunctionCallTypeFlatten(
        context: KnolusContext,
        subval: VariableValue,
    ): VariableValue.FunctionCallType = when (subval) {
        is VariableValue.FunctionCallType -> subval

        is VariableValue.MemberVariableReferenceType -> addByCoersionFunctionCallTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.VariableReferenceType -> addByCoersionFunctionCallTypeFlatten(context, subval.flatten(context))
        is VariableValue.MemberFunctionCallType -> addByCoersionFunctionCallTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.ExpressionType -> addByCoersionFunctionCallTypeFlatten(context, subval.flatten(context))

        else -> VariableValue.FunctionCallType(subval.asString(context), emptyArray())
    }

    suspend fun addByCoercionFunctionCallType(
        context: KnolusContext,
        array: Array<VariableValue.FunctionCallType>,
        value: VariableValue,
    ): Array<VariableValue.FunctionCallType> =
        when (value) {
            is VariableValue.FunctionCallType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.FunctionCallType>()) array.plus(value.array as Array<out VariableValue.FunctionCallType>)
                else array.plus(value.array.mapToArray { addByCoersionFunctionCallTypeFlatten(context, it) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionFunctionCallType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.FunctionCallType(value.asString(context), emptyArray()))
        }

    suspend fun addByCoersionMemberFunctionCallTypeFlatten(
        context: KnolusContext,
        subval: VariableValue,
    ): VariableValue.MemberFunctionCallType = when (subval) {
        is VariableValue.MemberFunctionCallType -> subval

        is VariableValue.VariableReferenceType -> addByCoersionMemberFunctionCallTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.FunctionCallType -> addByCoersionMemberFunctionCallTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.MemberVariableReferenceType -> addByCoersionMemberFunctionCallTypeFlatten(context,
            subval.flatten(context))
        is VariableValue.ExpressionType -> addByCoersionMemberFunctionCallTypeFlatten(context, subval.flatten(context))

        else -> VariableValue.MemberFunctionCallType(subval.asString(context), "identify", emptyArray())
    }

    suspend fun addByCoercionMemberFunctionCallType(
        context: KnolusContext,
        array: Array<VariableValue.MemberFunctionCallType>,
        value: VariableValue,
    ): Array<VariableValue.MemberFunctionCallType> =
        when (value) {
            is VariableValue.MemberFunctionCallType -> array.plus(value)
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.MemberFunctionCallType>()) array.plus(value.array as Array<out VariableValue.MemberFunctionCallType>)
                else array.plus(value.array.mapToArray { addByCoersionMemberFunctionCallTypeFlatten(context, it) })
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                addByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.MemberFunctionCallType(value.asString(context), "identify", emptyArray()))
        }

    suspend fun addByCoercionArrayType(
        context: KnolusContext,
        array: Array<VariableValue.ArrayType<*>>,
        value: VariableValue,
    ): Array<VariableValue.ArrayType<*>> =
        when (value) {
            is VariableValue.ArrayType<*> -> array.plus(value)
            is VariableValue.VariableReferenceType -> addByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType -> addByCoercionArrayType(context,
                array,
                value.flatten(context))
            is VariableValue.FunctionCallType -> addByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType -> addByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.ExpressionType -> addByCoercionArrayType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.ArrayType(arrayOf(value)))
        }

    suspend fun addByCoercionExpressionType(
        context: KnolusContext,
        array: Array<VariableValue.ExpressionType>,
        value: VariableValue,
    ): Array<VariableValue.ExpressionType> =
        when (value) {
            is VariableValue.ExpressionType -> array.plus(value)

            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.ExpressionType>()) array.plus(value.array as Array<out VariableValue.ExpressionType>)
                else if (value.array.size == 1) array.plus(VariableValue.ExpressionType(value.array[0], emptyArray()))
                else if (value.array.isNotEmpty()) array.plus(VariableValue.ExpressionType(value.array[0],
                    value.array.mapToArray(1) { Pair(ExpressionOperation.PLUS, it) }))
                else array
            }
            is VariableValue.VariableReferenceType ->
                addByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                addByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                addByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                addByCoercionExpressionType(context, array, value.flatten(context))

            else -> array.plus(VariableValue.ExpressionType(value, emptyArray()))
        }

    suspend fun addByCoercionNullType(
        context: KnolusContext,
        array: Array<VariableValue.NullType>,
        value: VariableValue,
    ): Array<VariableValue.NullType> = array.plus(VariableValue.NullType)

    suspend fun addByCoercionUndefinedType(
        context: KnolusContext,
        array: Array<VariableValue.UndefinedType>,
        value: VariableValue,
    ): Array<VariableValue.UndefinedType> = array.plus(VariableValue.UndefinedType)

    /** REMOVE */

    suspend fun <T : VariableValue> dropByCoercion(array: Array<T>, context: KnolusContext, value: VariableValue): Array<T> =
        array.coerceArrayWith(context, value, this::dropByCoercionStringComponents)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionStringType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionBooleanType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionCharType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionIntegerType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionDecimalType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionVariableReferenceType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionMemberVariableReferenceType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionFunctionCallType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionMemberFunctionCallType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionArrayType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionExpressionType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionNullType)
            ?: array.coerceArrayWith(context, value, this::dropByCoercionUndefinedType)
            ?: (array as Array<VariableValue>).filter { subvalue -> subvalue != value }.toTypedArray() as Array<T>

    suspend fun dropByCoercionStringComponents(
        context: KnolusContext,
        array: Array<VariableValue.StringComponents>,
        value: VariableValue,
    ): Array<VariableValue.StringComponents> =
        when (value) {
            is VariableValue.StringComponents -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.StringComponents>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionStringComponents(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionStringComponents(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionStringType(
        context: KnolusContext,
        array: Array<VariableValue.StringType>,
        value: VariableValue,
    ): Array<VariableValue.StringType> =
        when (value) {
            is VariableValue.StringType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.StringType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionStringType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionStringType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionBooleanType(
        context: KnolusContext,
        array: Array<VariableValue.BooleanType>,
        value: VariableValue,
    ): Array<VariableValue.BooleanType> =
        when (value) {
            is VariableValue.BooleanType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.BooleanType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionBooleanType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionBooleanType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionCharType(
        context: KnolusContext,
        array: Array<VariableValue.CharType>,
        value: VariableValue,
    ): Array<VariableValue.CharType> =
        when (value) {
            is VariableValue.CharType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.CharType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionCharType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionCharType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionIntegerType(
        context: KnolusContext,
        array: Array<VariableValue.IntegerType>,
        value: VariableValue,
    ): Array<VariableValue.IntegerType> =
        when (value) {
            is VariableValue.IntegerType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.IntegerType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionIntegerType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionIntegerType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionDecimalType(
        context: KnolusContext,
        array: Array<VariableValue.DecimalType>,
        value: VariableValue,
    ): Array<VariableValue.DecimalType> =
        when (value) {
            is VariableValue.DecimalType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.DecimalType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionDecimalType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionDecimalType(context, array, value.flatten(context))
            else -> array.plus(VariableValue.DecimalType(value.asNumber(context).toDouble()))
        }

    suspend fun dropByCoercionVariableReferenceType(
        context: KnolusContext,
        array: Array<VariableValue.VariableReferenceType>,
        value: VariableValue,
    ): Array<VariableValue.VariableReferenceType> =
        when (value) {
            is VariableValue.VariableReferenceType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.VariableReferenceType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionVariableReferenceType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionMemberVariableReferenceType(
        context: KnolusContext,
        array: Array<VariableValue.MemberVariableReferenceType>,
        value: VariableValue,
    ): Array<VariableValue.MemberVariableReferenceType> =
        when (value) {
            is VariableValue.MemberVariableReferenceType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.MemberVariableReferenceType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionMemberVariableReferenceType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionFunctionCallType(
        context: KnolusContext,
        array: Array<VariableValue.FunctionCallType>,
        value: VariableValue,
    ): Array<VariableValue.FunctionCallType> =
        when (value) {
            is VariableValue.FunctionCallType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.FunctionCallType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionFunctionCallType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionFunctionCallType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionMemberFunctionCallType(
        context: KnolusContext,
        array: Array<VariableValue.MemberFunctionCallType>,
        value: VariableValue,
    ): Array<VariableValue.MemberFunctionCallType> =
        when (value) {
            is VariableValue.MemberFunctionCallType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array
            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.MemberFunctionCallType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            is VariableValue.ExpressionType ->
                dropByCoercionMemberFunctionCallType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionArrayType(
        context: KnolusContext,
        array: Array<VariableValue.ArrayType<*>>,
        value: VariableValue,
    ): Array<VariableValue.ArrayType<*>> =
        when (value) {
            is VariableValue.ArrayType<*> -> array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
            is VariableValue.VariableReferenceType -> dropByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType -> dropByCoercionArrayType(context,
                array,
                value.flatten(context))
            is VariableValue.FunctionCallType -> dropByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType -> dropByCoercionArrayType(context, array, value.flatten(context))
            is VariableValue.ExpressionType -> dropByCoercionArrayType(context, array, value.flatten(context))
            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionExpressionType(
        context: KnolusContext,
        array: Array<VariableValue.ExpressionType>,
        value: VariableValue,
    ): Array<VariableValue.ExpressionType> =
        when (value) {
            is VariableValue.ExpressionType -> if (value in array) array.filter { subvalue -> subvalue != value }.toTypedArray() else array

            is VariableValue.ArrayType<*> -> {
                if (value.array.isArrayOf<VariableValue.ExpressionType>()) array.filter { subvalue -> subvalue !in value.array }.toTypedArray()
                else array.sliceArray(value.array.size until array.size)
            }
            is VariableValue.VariableReferenceType ->
                dropByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.MemberVariableReferenceType ->
                dropByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.FunctionCallType ->
                dropByCoercionExpressionType(context, array, value.flatten(context))
            is VariableValue.MemberFunctionCallType ->
                dropByCoercionExpressionType(context, array, value.flatten(context))

            else -> array.sliceArray(value.asNumber(context).toInt() until array.size)
        }

    suspend fun dropByCoercionNullType(
        context: KnolusContext,
        array: Array<VariableValue.NullType>,
        value: VariableValue,
    ): Array<VariableValue.NullType> = array.sliceArray(value.asNumber(context).toInt() until array.size)

    suspend fun dropByCoercionUndefinedType(
        context: KnolusContext,
        array: Array<VariableValue.UndefinedType>,
        value: VariableValue,
    ): Array<VariableValue.UndefinedType> = array.sliceArray(value.asNumber(context).toInt() until array.size)
}