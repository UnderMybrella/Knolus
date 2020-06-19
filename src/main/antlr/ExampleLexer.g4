lexer grammar ExampleLexer;
import KnolusLexer;

VERSION_COMMAND: 'version';
ECHO_COMMAND: 'echo' INLINE_WHITESPACE_CHARACTERS+ -> pushMode(EchoCommandMode);

mode EchoCommandMode;

ECHO_COMMAND_INTEGER: INTEGER -> type(INTEGER), popMode;
ECHO_COMMAND_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER), popMode;

ECHO_COMMAND_NULL: NULL -> type(NULL), popMode;
ECHO_COMMAND_BEGIN_QUOTED_CHARACTER: '\'' -> type(BEGIN_QUOTED_CHARACTER), popMode, pushMode(QuotedCharacterMode);
ECHO_COMMAND_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), popMode, pushMode(QuotedStringMode);
ECHO_COMMAND_TRUE: TRUE -> type(TRUE), popMode;
ECHO_COMMAND_FALSE: FALSE -> type(FALSE), popMode;
ECHO_COMMAND_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), popMode, pushMode(ExpressionMode);
ECHO_COMMAND_START_ARRAY: '[' -> type(BEGIN_ARRAY), popMode, pushMode(ArrayMode);

//Identifier has to go LAST!!!
ECHO_COMMAND_FUNC_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), popMode, pushMode(FunctionCall);
ECHO_COMMAND_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE), popMode;
ECHO_COMMAND_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER), popMode;

ECHO_COMMAND_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;