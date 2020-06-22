lexer grammar LibLexer;
tokens { IDENTIFIER, PLAIN_CHARACTERS }

INTEGER: '-'? ('0b' BINARY_DIGITS+) | ('0o' OCTAL_DIGITS+) | ('0x' HEX_DIGITS+) | '0d'? DECIMAL_DIGITS+;
DECIMAL_NUMBER: '-'? ('0' | ([1-9] DECIMAL_DIGITS*)) ('.' DECIMAL_DIGITS+)? ([eE] [+\-]? DECIMAL_DIGITS+)?;

TRUE: T R U E;
FALSE: F A L S E;
NULL: N U L L;
GLOBAL: G L O B A L;

fragment BINARY_DIGITS: [0-1];
fragment OCTAL_DIGITS: [0-7];
fragment DECIMAL_DIGITS: [0-9];
fragment HEX_DIGITS: [0-9a-fA-F];

fragment INLINE_WHITESPACE_CHARACTERS: [ \t];
fragment NEW_LINE: '\r'? '\n';

fragment IDENTIFIER_START: [a-zA-Z_?];
fragment IDENTIFIER_END: [a-zA-Z0-9_\-?];

fragment A : [aA]; // match either an 'a' or 'A'
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];