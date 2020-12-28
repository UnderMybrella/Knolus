lexer grammar KnolusLexer;
import LibLexer, KnolusModesLexer;

tokens { OPEN_COMPLEX_TYPE }

VARIABLE_DECLARATION: (V A R) | (V A L) | (S E T);

SET_VARIABLE: '=';

DECLARE_FUNCTION_ALIAS: (F N | F U N | D E F) '_'? A L I A S -> pushMode(FunctionAliasDeclaration);

DECLARE_FUNCTION: (D E F | F N | F U N) -> pushMode(FunctionDeclaration);

/** Put any other things here */

GLOBAL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

END_SCOPE: '}' -> popMode;

SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;
SEMICOLON_SEPARATOR: ';' NEW_LINE?;
NL_SEPARATOR: NEW_LINE;

mode FunctionDeclaration;

START_FUNCTION_DECLARATION: '(';
END_FUNCTION_DECLARATION: ')';
END_DECL_WITH_STUB: ';' -> popMode;
END_DECL_WITH_BODY: '{' NEW_LINE -> mode(DEFAULT_MODE);

FN_DECL_OPEN_DEFINE_GENERICS: '<';
FN_DECL_CLOSE_DEFINE_GENERICS: '>';

FN_DECL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);//, pushMode(IdentifierEnd);
FN_DECL_PARAM_SEPARATOR: ',';
FN_DECL_PARAM_TYPE: ':';
FN_DECL_COMPLEX_TYPE: '{' -> type(OPEN_COMPLEX_TYPE), pushMode(ComplexTypeMode);

FN_DECL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

mode ComplexTypeMode;

NEST_TYPE: '(' -> type(OPEN_COMPLEX_TYPE), pushMode(ComplexTypeMode);

TYPE_OR: '|';
TYPE_AND: '&';
TYPE_XOR: '^';
TYPE_NEGATE: '!';

TYPE_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);

END_NEST: ')' -> popMode;
END_TYPE: '}' -> popMode;

TYPE_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

mode FunctionAliasDeclaration;

//fnalias flashPak(path: String) -> convert archive(path, transform = "convert {0} from png to tga") to pak

FNALIAS_DECL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);
FNALIAS_DECL_START_PARAMS: '(';
FNALIAS_DECL_END_PARAMS: ')';

FNALIAS_DECL_SET_COMMAND: '->' -> popMode, pushMode(DEFAULT_MODE);

FNALIAS_DECL_PARAM_SEPARATOR: ',';
FNALIAS_DECL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;