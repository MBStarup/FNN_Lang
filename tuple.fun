@print: (STR) -> (INT)
@print_int: (INT) -> (INT)
@print_flt: (FLT) -> (INT)

(sum): (x:INT y:INT) -> {return x + y}

(a b c): (1 (2 3) 4)

print_int!(sum!(b))