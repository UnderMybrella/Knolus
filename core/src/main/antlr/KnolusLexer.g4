lexer grammar KnolusLexer;
import LibLexer, KnolusModesLexer;

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

FN_DECL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);//, pushMode(IdentifierEnd);
FN_DECL_PARAM_SEPARATOR: ',';

FN_DECL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;

mode FunctionAliasDeclaration;

//fnalias flashPak(path: String) -> convert archive(path, transform = "convert {0} from png to tga") to pak

FNALIAS_DECL_IDENTIFIER: IDENTIFIER_START IDENTIFIER_END* -> type(IDENTIFIER);
FNALIAS_DECL_START_PARAMS: '(';
FNALIAS_DECL_END_PARAMS: ')';

FNALIAS_DECL_SET_COMMAND: '->' -> popMode, pushMode(DEFAULT_MODE);

FNALIAS_DECL_PARAM_SEPARATOR: ',';
FNALIAS_DECL_SKIP_WS: INLINE_WHITESPACE_CHARACTERS+ -> skip;