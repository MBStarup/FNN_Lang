@print: (STR) -> (INT)
@print_int: (INT) -> (INT)
@print_flt: (FLT) -> (INT)

(f): (a:INT t:(INT (INT INT))) -> {(b c d):t return a + b + c}
(x): f!(69000 400 20 1)
print_int!(x)