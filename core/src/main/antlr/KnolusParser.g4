parser grammar KnolusParser;
options { tokenVocab=KnolusLexer; }

scope: line? ((SEMICOLON_SEPARATOR | NL_SEPARATOR+) line)*?;
line: declareVariable | declareFunction | setVariableValue | functionCall | memberFunctionCall;

declareVariable: GLOBAL? VARIABLE_DECLARATION (variableName=IDENTIFIER) (SET_VARIABLE variableValue)?;
setVariableValue: GLOBAL? (variableName=IDENTIFIER) SET_VARIABLE variableValue;

declareFunction: GLOBAL? DECLARE_FUNCTION (functionName=IDENTIFIER) START_FUNCTION_DECLARATION ((parameters+=IDENTIFIER (FN_DECL_PARAM_SEPARATOR parameters+=IDENTIFIER)*?))? END_FUNCTION_DECLARATION (END_DECL_WITH_STUB | declareFunctionBody);
declareFunctionBody: END_DECL_WITH_BODY scope (SEMICOLON_SEPARATOR | NL_SEPARATOR+)? END_SCOPE;

functionCall: (functionName=BEGIN_FUNCTION_CALL) (parameters+=functionCallParameter)? (FUNC_CALL_SEPARATOR (parameters+=functionCallParameter))* END_FUNCTION_CALL;
functionCallParameter: (parameterName=IDENTIFIER FUNC_CALL_SET_PARAMETER)? variableValue;
memberFunctionCall: (memberName=BEGIN_MEMBER_REFERENCE) functionCall;

variableReference: (variableName=IDENTIFIER);
memberVariableReference: (memberName=BEGIN_MEMBER_REFERENCE) variableReference;

variableValue
    : array
    | quotedString
    | quotedCharacter
    | number
    | memberFunctionCall
    | functionCall
    | expression
    | bool
    | NULL
    | memberVariableReference
    | variableReference
    ;

array: BEGIN_ARRAY arrayContents END_ARRAY;

arrayContents
    : ((quotedString | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference) (ARRAY_SEPARATOR (quotedString | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference))*)
    | ((quotedCharacter | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference) (ARRAY_SEPARATOR (quotedCharacter | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference))*)
    | ((wholeNumber | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference) (ARRAY_SEPARATOR (wholeNumber | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference))*)
    | ((decimalNumber | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference) (ARRAY_SEPARATOR (decimalNumber | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference))*)
    | ((bool | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference) (ARRAY_SEPARATOR (bool | memberFunctionCall | functionCall | expression | memberVariableReference | variableReference))*)
    ;
    
bool: TRUE | FALSE;

quotedString
    : BEGIN_QUOTED_STRING
        (quotedStringVariableReference | ESCAPES | STRING_CHARACTERS | QUOTED_STRING_LINE_BREAK | STRING_WHITESPACE)*
      END_QUOTED_STRING
    ;

quotedCharacter
    : BEGIN_QUOTED_CHARACTER
        (CHARACTER_ESCAPES | QUOTED_CHARACTERS | QUOTED_CHARACTER_LINE_BREAK)
      END_QUOTED_CHARACTER
    ;

quotedStringVariableReference: QUOTED_STRING_VARIABLE_REFERENCE variableReference;

number: wholeNumber | decimalNumber;
wholeNumber: INTEGER;
decimalNumber: DECIMAL_NUMBER;

expression
    : BEGIN_EXPRESSION
        (startingValue=variableValue) ((exprOps+=expressionOperation) (exprVals+=variableValue))+?
      END_EXPRESSION
    ;

expressionOperation
    : EXPR_EXPONENTIAL
    | EXPR_DIVIDE
    | EXPR_MULTIPLY
    | EXPR_PLUS
    | EXPR_MINUS
    ;