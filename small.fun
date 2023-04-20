@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@number: INT
@square: (INT) -> (INT)
@print: (STR) -> (INT)
@print_int: (INT) -> (INT)
@print_flt: (FLT) -> (INT)
@exit: (INT) -> (INT)

(train_input train_expected): load_csv("../c_ml/mnist_train.csv" 10 784 2000 50)
(test_input test_expected): load_csv("../c_ml/mnist_test.csv" 10 784 500 50)
(l_one): DENSE(784 64 sigmoid)
(l_two): DENSE(32 10 sigmoid)
(m): MODEL<l_one DENSE(64 32 sigmoid) l_two>
TRAIN<m 100 4 train_input train_expected>
print("done")