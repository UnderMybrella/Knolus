lexer grammar KnolusModesLexer;
import LibLexer;

MODES_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

BEGIN_QUOTED_STRING: '"' -> pushMode(QuotedStringMode);
BEGIN_QUOTED_CHARACTER: '\'' -> pushMode(QuotedCharacterMode);
BEGIN_EXPRESSION: '(' -> pushMode(ExpressionMode);
BEGIN_FUNCTION_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> pushMode(FunctionCall);
BEGIN_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.';
BEGIN_ARRAY: '[' -> pushMode(ArrayMode);

mode ArrayMode;

ARRAY_INTEGER: INTEGER -> type(INTEGER);
ARRAY_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);

ARRAY_NULL: NULL -> type(NULL);
ARRAY_BEGIN_QUOTED_CHARACTER: '\'' -> type(BEGIN_QUOTED_CHARACTER), pushMode(QuotedCharacterMode);
ARRAY_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
ARRAY_TRUE: TRUE -> type(TRUE);
ARRAY_FALSE: FALSE -> type(FALSE);
ARRAY_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), pushMode(ExpressionMode);
ARRAY_RECURSIVE: '[' -> type(BEGIN_ARRAY), pushMode(ArrayMode);

ARRAY_SEPARATOR: ',';

//Identifier has to go LAST!!!
ARRAY_FUNC_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), pushMode(FunctionCall);
ARRAY_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE);
ARRAY_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

ARRAY_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;
END_ARRAY: ']' -> popMode;

mode FunctionCall;

FUNC_CALL_SET_PARAMETER: '=';
FUNC_CALL_INTEGER: INTEGER -> type(INTEGER);
FUNC_CALL_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);

//FUNC_CALL_VARIABLE_REFERENCE: '$' -> type(VARIABLE_REFERENCE), pushMode(Identifier);
//FUNC_CALL_WRAPPED_SCRIPT_CALL: '$(' -> type(WRAPPED_SCRIPT_CALL), pushMode(IdentifierEnd), pushMode(Identifier);
FUNC_CALL_NULL: NULL -> type(NULL);
FUNC_CALL_BEGIN_QUOTED_CHARACTER: '\'' -> type(BEGIN_QUOTED_CHARACTER), pushMode(QuotedCharacterMode);
FUNC_CALL_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
FUNC_CALL_TRUE: TRUE -> type(TRUE);
FUNC_CALL_FALSE: FALSE -> type(FALSE);
FUNC_CALL_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), pushMode(ExpressionMode);

FUNC_CALL_SEPARATOR: ',';

//Identifier has to go LAST!!!
FUNC_CALL_RECURSIVE: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), pushMode(FunctionCall);
FUNC_CALL_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE);
FUNC_CALL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

FUNC_CALL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

END_FUNCTION_CALL: ')' -> popMode;

mode ExpressionMode;

EXPRESSION_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

RECURSIVE_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), pushMode(ExpressionMode);
END_EXPRESSION: ')' -> popMode;

EXPR_EXPONENTIAL: '**';
EXPR_PLUS: '+';
EXPR_MINUS: '-';
EXPR_DIVIDE: '/';
EXPR_MULTIPLY: '*';

EXPR_INTEGER: INTEGER -> type(INTEGER);
EXPR_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER);
//EXPR_WRAPPED_SCRIPT_CALL: '$(' -> type(WRAPPED_SCRIPT_CALL), pushMode(IdentifierEnd), pushMode(Identifier);
EXPR_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), pushMode(QuotedStringMode);
EXPR_BEGIN_QUOTED_CHARACTER: '\'' -> type(BEGIN_QUOTED_CHARACTER), pushMode(QuotedCharacterMode);
EXPR_TRUE: TRUE -> type(TRUE);
EXPR_FALSE: FALSE -> type(FALSE);
EXPR_NULL: NULL -> type(NULL);

EXPR_FUNC_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), pushMode(FunctionCall);
EXPR_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE);
EXPR_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

mode QuotedStringMode;

ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | ESCAPE_CHARACTERS);
STRING_CHARACTERS: ~["\\$& \t]+;
QUOTED_STRING_VARIABLE_REFERENCE: '$' -> pushMode(QuotedStringIdentifierMode);

//QUOTED_STRING_LINE_BREAK: INLINE_WHITESPACE_CHARACTERS* '&br' INLINE_WHITESPACE_CHARACTERS*;
//QUOTED_STRING_LINE_BREAK_NO_SPACE: '&{br}';

QUOTED_STRING_LINE_BREAK: '&' ('{' B R '}') | (B R);

STRING_WHITESPACE: INLINE_WHITESPACE_CHARACTERS+;

fragment ESCAPE_CHARACTERS: ["\\/bfnrt$&];

END_QUOTED_STRING: '"' -> popMode;

mode QuotedStringIdentifierMode;

QUOTED_STRING_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER), popMode;

mode QuotedCharacterMode;

CHARACTER_ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | ESCAPE_CHARACTERS);
QUOTED_CHARACTERS: ~['\\&];

QUOTED_CHARACTER_LINE_BREAK: '&' ('{' B R '}') | (B R);

END_QUOTED_CHARACTER: '\'' -> popMode;