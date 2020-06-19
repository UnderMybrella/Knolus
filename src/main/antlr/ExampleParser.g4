parser grammar ExampleParser;
import KnolusParser;
options { tokenVocab=ExampleLexer; }

command
    : versionCommand
    | echoCommand
    ;

versionCommand: VERSION_COMMAND;

echoCommand: ECHO_COMMAND variableValue;