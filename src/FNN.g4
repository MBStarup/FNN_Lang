grammar FNN;
program: stmts = stmtlist EOF;

stmt: expr | function_declaration | assign | train_stmt | extern;

function_declaration: '(' ID* ')' '->' '{' stmts = stmtlist '}';
stmtlist: stmt*;
assign: '(' ID* ')' ':' expr_in_assign = expr;
extern: '@' ID ':' type;
train_stmt: 'TRAIN' '<' model = expr epochs = expr batch_size = expr input = expr expected = expr '>';

expr:
	'"' STR_CONTENT '"'																					# strlit
	| INT																								# intlit
	| FLOAT																								# floatlit
	| func = expr '(' exprs = exprlist ')'																# call
	| '(' expr_in_parens = expr ')'																		# parens
	| '(' exprs = exprlist ')'																			# tuplelit
	| left_op = expr OPERATOR right_op = expr															# biop
	| OPERATOR op = expr																				# unop
	| ID																								# eval
	| 'DENSE' '(' input_size = expr output_size = expr activation_function = ACTIVATION_FUNCTION ')'	# layerlit
	| 'MODEL' '<' expr_in_model = expr* '>'																# modellit;
exprlist: expr*;

type:
	BASETYPE												# basetypelit
	| '(' args = typelist ')' '->' '(' rets = typelist ')'	# functypelit
	| '[' arrtype = type ']'								# arrtypelit
	| '(' tupletypes = typelist ')'							# tupletypelit;
typelist: type*;

BASETYPE: 'STR' | 'FLT' | 'INT' | 'LYR' | 'MDL';

OPERATOR: '*' | '/' | '+' | '-';
ACTIVATION_FUNCTION: 'sigmoid' | 'relu';
INT: [0-9]+;
FLOAT: INT | [0-9]* '.' [0-9]+;
ID: [a-z_]+;
STR_CONTENT: [a-zA-Z0-9/._\\]*;
WS: [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines