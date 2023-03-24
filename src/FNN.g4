grammar FNN;
program: stmt* EOF;

stmt: expr | function_declaration;
function_declaration: ID ':' '(' ID* ')' '{' stmt* '}';

expr:
	INT											# intlit
	| FLOAT										# floatlit
	| left_op = expr OPERATOR right_op = expr	# biop
	| OPERATOR op = expr						# unop
	| '(' expr_in_parens = expr ')'				# parens
	| ID ':' expr_in_assign = expr				# assign
	| ID										# eval
	| ID '(' expr* ')'							# call;

OPERATOR: '*' | '/' | '+' | '-';
INT: [0-9]+;
FLOAT: INT | [0-9]* '.' [0-9]+;
ID: [a-z_]+;
// match lower-case identifiers
WS: [ \t\r\n]+ -> skip;
// skip spaces, tabs, newlines