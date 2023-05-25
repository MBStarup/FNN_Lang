@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]])) 
@tstwithprint:   (FNN [FLT]) -> (INT)
@print_flt: (FLT) -> (INT)
@print_int: (INT) -> (INT)
@print: (STR) -> (INT)

print!("hello\n")

(in): 784 
(out): 10 

(train_data_in train_data_out): load_csv!("mnist_train.csv" out in 1300 6)
(test_data_in test_data_out): load_csv!("mnist_test.csv" out in 1000 6)

(sigmoid): (in:FLT) -> { RETURN 1.0/(1.0 + ((2.71828)^(-in))) }

(sigmoid_derivative): (in:FLT) -> {
    (sigmoid): (in:FLT) -> { RETURN 1.0/(1.0 + (((2.71828)^(-in)))) }
    RETURN sigmoid!(in) * (1.0 - sigmoid!(in))
}


(m): NN(sigmoid sigmoid_derivative)(in 128 out)

(err): 99.9
(i): 0
(epochs): 1
WHILE (err > 0.05) {
    TRAIN(m 0.15 epochs train_data_in train_data_out)
    (err): TEST(m test_data_in test_data_out)
    (i): i+epochs
    print_int!(i) print!(": ") print_flt!(err) print!("\n")
    tstwithprint!(m test_data_in[0])
} 