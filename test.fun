@print_int: (INT) -> (INT)

(sum): (x:INT y:INT) -> {RETURN x + y}

(a b): (1 2)

print_int!(sum!(a b))