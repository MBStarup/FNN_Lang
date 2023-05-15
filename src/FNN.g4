grammar FNN
	;

program: stmts = stmtlist EOF;

stmt: assign | extern | train_stmt | expr | while_stmt;

stmtlist: stmt*;
assign: '(' ID* ')' ':' expr_in_assign = expr;
extern: '@' ID ':' type;
train_stmt: 'TRAIN' '(' nn = ID epochs = expr input = expr expected = expr ')';
while_stmt: 'WHILE' predicate = expr '{' stmts = stmtlist '}';

expr
	: STR																					# strlit
	| INT																					# intlit
	| FLOAT																					# floatlit
	| func = expr '!(' exprs = exprlist ')'													# call
	| '(' exprs = exprlist ')'																# tuplelit
	| left_op = expr OPERATOR right_op = expr												# biop
	| OPERATOR op = expr																	# unop
	| ID																					# eval
	| 'NN' '(' activation = expr derivative = expr ')' '(' sizes = exprlist ')'				# nnlit
	| arr = expr '[' index = expr ']'														# arraccess
	| '(' params = paramdecllist ')' '->' '{' stmts = stmtlist 'RETURN' return = expr '}'	# functionlit
	| 'TEST' '(' nn = expr in = expr out = expr ')'											# testexpr
	;
exprlist: expr*;
paramdecl: ID ':' param_type = type;
paramdecllist: paramdecl*;

type: BASETYPE # basetypelit | '(' args = typelist ')' '->' '(' rets = typelist ')' # functypelit | '[' arrtype = type ']' # arrtypelit | '(' tupletypes = typelist ')' # tupletypelit;
typelist: type*;

BASETYPE: 'STR' | 'FLT' | 'INT' | 'FNN';

OPERATOR: '*' | '/' | '+' | '-' | '<' | '>' | '=' | '^';
INT: [0-9]+;
FLOAT: [0-9]* '.' [0-9]+;
ID: [a-z_]+;
STR: '"' (~'"')* '"';
WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines