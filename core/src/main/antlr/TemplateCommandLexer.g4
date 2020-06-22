lexer grammar TemplateCommandLexer;
//Import Knolus so we can reuse those values
import KnolusLexer;

//Define a command rule, which matches 'template' (case insensitive), one or more whitespace characters (tabs or spaces), and then tokens from TemplateCommandMode
TEMPLATE_COMMAND: T E M P L A T E INLINE_WHITESPACE_CHARACTERS+ -> pushMode(TemplateCommandMode);

//Define our new mode - TemplateCommandMode
mode TemplateCommandMode;

//Matches an integer token, then pops back to the default mode
TEMPLATE_COMMAND_INTEGER: INTEGER -> type(INTEGER), popMode;
//Matches a decimal token, then pops back to the default mode
TEMPLATE_COMMAND_DECIMAL_NUMBER: DECIMAL_NUMBER -> type(DECIMAL_NUMBER), popMode;

//Matches the literal 'null', then pops back to the default mode
TEMPLATE_COMMAND_NULL: NULL -> type(NULL), popMode;
//Matches a single quote, pushes to the Quoted Character Mode, then pops back to the default mode
TEMPLATE_COMMAND_BEGIN_QUOTED_CHARACTER: '\'' -> type(BEGIN_QUOTED_CHARACTER), popMode, pushMode(QuotedCharacterMode);
//Matches a double quote, pushes to the Quoted String Mode, then pops back to the default mode
TEMPLATE_COMMAND_BEGIN_QUOTED_STRING: '"' -> type(BEGIN_QUOTED_STRING), popMode, pushMode(QuotedStringMode);
//Matches the literal 'true', then pops back to the default mode
TEMPLATE_COMMAND_TRUE: TRUE -> type(TRUE), popMode;
//Matches the literal 'false', then pops back to the default mode
TEMPLATE_COMMAND_FALSE: FALSE -> type(FALSE), popMode;
//Matches an open parenthesis, pushes to the expression mode, then pops back to the default mode
TEMPLATE_COMMAND_START_EXPRESSION: '(' -> type(BEGIN_EXPRESSION), popMode, pushMode(ExpressionMode);
//Matches an open bracket, pushes to the array mode, then pops back to the default mode
TEMPLATE_COMMAND_START_ARRAY: '[' -> type(BEGIN_ARRAY), popMode, pushMode(ArrayMode);

//Identifier has to go LAST!!!
//Matches an identifier, then an open parenthesis; pushes to the function call mode, then pops back to the default mode
TEMPLATE_COMMAND_FUNCTION_CALL: IDENTIFIER_START IDENTIFIER_END* '(' -> type(BEGIN_FUNCTION_CALL), popMode, pushMode(FunctionCall);
//Matches an identifier, then a single dot; this doesn't change modes, since we will need to follow it up with a function call or a variable reference
TEMPLATE_COMMAND_MEMBER_REFERENCE: IDENTIFIER_START IDENTIFIER_END* '.' -> type(BEGIN_MEMBER_REFERENCE);
//Matches a dollar sign, then an identifier, then pops back to the default mode
TEMPLATE_COMMAND_VARIABLE_REFERENCE: '$' IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER), popMode;

// The following three lexer rules are for matching 'plain strings'; strings outside of quotes.
// Most strings *should* work, but they a) don't support interpolation, and b) form one string for the whole contents

//Matches a backspace, then either the letter u followed by 4 hex digits, or a character from the ESCAPE_CHARACTERS fragment listed below
TEMPLATE_COMMAND_ESCAPES: '\\' ('u' HEX_DIGITS HEX_DIGITS HEX_DIGITS HEX_DIGITS | TEMPLATE_COMMAND_ESCAPE_CHARACTERS) -> type(CHARACTER_ESCAPES);
//Matches any character that isn't a single quote, double quote, dollar sign, open parenthesis, open bracket, or backslash
//NOTE: It is ***very*** important that this is a ***single*** match, and not a multi-match via * or the likes.
// By only matching one character at a time, we ensure that when the conditions for the next parameter are met, we break and switch
TEMPLATE_COMMAND_PLAIN_STRING: ~['"$(\\[] -> type(PLAIN_CHARACTERS);
//A list of escape characters
fragment TEMPLATE_COMMAND_ESCAPE_CHARACTERS: ['"\\/bfnrt$([];

//If you want whitespace to be skipped, uncomment this. Note: this *will* screw with plain strings
//TEMPLATE_COMMAND_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;