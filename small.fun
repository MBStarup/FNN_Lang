@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@number: INT
@square: (INT) -> (INT)
(a): square(69420)
(train_input train_expected): load_csv("train_mnist.csv" 10 784 20000 5)
(test_input test_expected): load_csv("test_mnist.csv" 10 784 500 5)
(l_one): DENSE(784 64 sigmoid)
(l_two): DENSE(32 10 sigmoid)
(m): MODEL<l_one DENSE(64 32 sigmoid) l_two>
TRAIN<m 3000 4 train_input train_expected>