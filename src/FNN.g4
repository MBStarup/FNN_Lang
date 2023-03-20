grammar FNN;
program: expr*;
assignment_expr: ID ':' expr;
function_declaration: '(' (ID ' ')* ')' ':' '{' expr* '}';
array_declaration: type ('[' expr ']')+;
range_declaration: INT '..' FLOAT;
model_declaration: 'model' '<' '>' '<' ID* '>' '<' ID '>';
expr:
	ID
	| function_declaration
	| array_declaration
	| assignment_expr
	| INT
	| FLOAT
	| expr OPERATOR expr;
type: 'num';

COLON: ':';
RPARENS: '(';
LPARENS: ')';
RCURLY: '{';
LCURLY: '}';
RBRACE: '[';
LBRACE: ']';
TYPE: 'num';
OPERATOR: '*' | '/' | '+' | '-' | '<' | '>' | '=';
INT: [-]? [0-9]+;
FLOAT: INT | [-]? [0-9]* '.' [0-9]+;
ID: [a-z_]+;
// match lower-case identifiers
WS: [ \t\r\n]+ -> skip;
// skip spaces, tabs, newlines