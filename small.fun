@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@print: (STR) -> (INT)
@exp: (FLT) -> (FLT)

(sigmoid): (in:FLT) -> {
    1 
    return 
    1.0
    /
    (
        1.0 
        + 
        exp(in)
        )
}
(sigmoid_derivative): (in:FLT) -> {1 sigmoid(in) * (1 - sigmoid(in))}

(in): 784
(out): 10

(train_input train_expected): load_csv!("../c_ml/mnist_train.csv" out in 2000 50)
(test_input test_expected): load_csv!("../c_ml/mnist_test.csv" out in 500 50)

(m): MODEL<sigmoid sigmoid_derivative><out 64 32 in>
TRAIN<m 100 4 train_input train_expected>
print!("done")