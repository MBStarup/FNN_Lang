grammar FNN
	;
program: stmt* EOF;

stmt: expr | function_declaration | assign | train_stmt;

function_declaration: ID ':' '(' ID* ')' '{' stmt* '}';
assign: ID* ':' expr_in_assign = expr;
train_stmt: 'train' '<' model = expr epochs = expr batch_size = expr '>';

expr
	: INT																								# intlit
	| FLOAT																								# floatlit
	| left_op = expr OPERATOR right_op = expr															# biop
	| OPERATOR op = expr																				# unop
	| '(' expr_in_parens = expr ')'																		# parens
	| ID																								# eval
	| ID '(' expr* ')'																					# call
	| 'dense' '(' input_size = expr output_size = expr activation_function = ACTIVATION_FUNCTION ')'	# layerlit
	| 'model' '<' expr_in_model = expr* '>'																# modellit
	| '"' STR_CONTENT '"'																				# strlit
	;

OPERATOR: '*' | '/' | '+' | '-';
ACTIVATION_FUNCTION: 'sigmoid' | 'relu';
INT: [0-9]+;
FLOAT: INT | [0-9]* '.' [0-9]+;
ID: [a-z_]+;
STR_CONTENT: [a-zA-Z0-9/._]*;
// match lower-case identifiers
WS: [ \t\r\n]+ -> skip;
// skip spaces, tabs, newlines