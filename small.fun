@loadcsv: (STR) -> (([FLT] [FLT]))
@number: INT
@square: (INT) -> (INT)
(input expected): loadcsv("mnist.csv")
(l_one): DENSE(784 64 sigmoid)
(l_two): DENSE(32 10 sigmoid)
(m): MODEL<l_one DENSE(64 32 sigmoid) l_two>
TRAIN<m 3000 4 input expected>