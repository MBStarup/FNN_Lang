@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]])) 

(in): 784 
(out): 10 

(train_data_out train_data_in): load_csv!("mnist_train.csv" out in 13000 6) 
(test_data_out test_data_in): load_csv!("mnist_test.csv" out in 10000 6) 

(sigmoid): (in:FLT) -> { RETURN 1.0/(1.0 + ((2.71828)^(-in))) }

(sigmoid_derivative): (in:FLT) -> {
    (sigmoid): (in:FLT) -> { RETURN 1.0/(1.0 + (((2.71828)^(-in)))) }
    RETURN sigmoid!(in) * (1.0 - sigmoid!(in))
}


(m): NN(sigmoid sigmoid_derivative)(in 128 out)

(err): 99.9
WHILE (err > 0.02) {
    TRAIN(m 0.15 100 train_data_in train_data_out)
    (err): TEST(m test_data_in test_data_out)
} 