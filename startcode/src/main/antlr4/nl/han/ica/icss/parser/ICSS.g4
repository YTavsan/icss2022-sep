grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';


//--- PARSER: ---
stylesheet: (assignment | stylerule)* EOF|EOF;

stylerule: selector OPEN_BRACE (assignment | declaration | if)* CLOSE_BRACE;

selector: (class | tag | id);

assignment: variable ASSIGNMENT_OPERATOR expression SEMICOLON;

declaration: property COLON expression SEMICOLON;

if: IF BOX_BRACKET_OPEN (variable | boolean) BOX_BRACKET_CLOSE OPEN_BRACE (assignment | declaration | if)* CLOSE_BRACE else?;
else: ELSE OPEN_BRACE (assignment | declaration | if)* CLOSE_BRACE;

literal: (scalar | variable | boolean | pixel | percentage | color);
expression: literal #none| expression MUL expression #multiply | expression PLUS expression #sum | expression MIN expression #sub | variable #none;

class: CLASS_IDENT;
tag: LOWER_IDENT;
id: ID_IDENT;
property: LOWER_IDENT;
scalar: SCALAR;
variable: CAPITAL_IDENT;
boolean: TRUE | FALSE;
pixel: PIXELSIZE;
percentage: PERCENTAGE;
color: COLOR;
