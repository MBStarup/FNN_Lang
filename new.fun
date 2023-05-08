@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@print: (STR) -> (INT)

(in): 784
(out): 10
(data_amount): 1000

(data_out data_in): load_csv!("mnist_train.csv" out in data_amount 6)

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

(m): MODEL<sigmoid sigmoid_derivative><in 128 out>

(i): 3
WHILE i { 
    TRAIN<m 10 data_in data_out>
    print!("10 epochs done\n")
    (i): i-1 
}

print!("done")