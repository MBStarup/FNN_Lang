@print_int: (INT) -> (INT)
(f): (h:INT) -> {(nn): NN((x:FLT) -> {RETURN x} (x:FLT) -> {RETURN 1.0})(784 h 10) RETURN (420 nn)}

(n): f!(69)

(_t): (420 69)
(_tt): (1 2 _t n 3)

(add): (t:(INT INT)) -> {(a b): t RETURN a + b}

print_int!(add!(_t))

(q w e r t): _tt
(y u): e

print_int!(add!((q u)))