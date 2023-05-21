@print_int: (INT) -> (INT)

(sum): (x:INT y:INT) -> {RETURN x + y}

(a b c): (1 (2 7) 4)

(x y): b

print_int!(sum!(x y))