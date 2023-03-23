grammar FNN;
program: expr* EOF;

expr: INT 									#intlit
	| FLOAT 								#floatlit
	| left_op=expr OPERATOR right_op=expr	#biop
	| OPERATOR op=expr						#unop
	| '(' expr ')' 							#parens
	;
	

OPERATOR: '*' | '/' | '+' | '-';
INT: [0-9]+;
FLOAT: INT | [0-9]* '.' [0-9]+;
ID: [a-z_]+;
// match lower-case identifiers
WS: [ \t\r\n]+ -> skip;
// skip spaces, tabs, newlines