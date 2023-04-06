@loadcsv: (STR INT o_size INT i_size INT amount INT) -> (([[FLT]¤amount] [[FLT]¤amount]))
@number: INT
@square: (INT) -> (INT)
(a): square(69420)
(train_input train_expected): loadcsv("train_mnist.csv" 10 784 20000 5)
(test_input test_expected): loadcsv("test_mnist.csv" 10 784 500 5)
(l_one): DENSE(784 64 sigmoid)
(l_two): DENSE(32 10 sigmoid)
(m): MODEL<l_one DENSE(64 32 sigmoid) l_two>
TRAIN<m 3000 4 input expected>