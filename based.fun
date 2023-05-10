@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@print: (STR) -> (INT)
@print_flt: (FLT) -> (INT)

(in): 784
(out): 10

(train_data_out train_data_in): load_csv!("mnist_train.csv" out in 13000 6)
(test_data_out test_data_in): load_csv!("mnist_test.csv" out in 10000 6)

(sigmoid): (in:FLT) -> {
    @exp: (FLT) -> (FLT)
    return 1.0/(1.0 + exp!(-in))
}

(sigmoid_derivative): (in:FLT) -> {
    @exp: (FLT) -> (FLT)
    (sigmoid): (in:FLT) -> {
        @exp: (FLT) -> (FLT)
        return 1.0/(1.0 + exp!(-in))
    }
    return sigmoid!(in) * (1.0 - sigmoid!(in))
}

(m): NN(sigmoid sigmoid_derivative)(in 128 out)

(err): 10.0
WHILE (err > 0.01) { 
    TRAIN(m 1 train_data_in train_data_out)
    print!("\n epoch done, err: ")
    print_flt!(err)
    (err): TEST(m test_data_in test_data_out)
}

print!("done")