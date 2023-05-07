@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@print: (STR) -> (INT)

(in): 784
(out): 10

(data_in data_out): load_csv!("mnist_train.csv" out in 2000 50)

(sigmoid): (in:FLT) -> {
    @exp: (FLT) -> (FLT)
    return 1.0/(1.0 + exp!(in))
}

(sigmoid_derivative): (in:FLT) -> {
    @exp: (FLT) -> (FLT)
    (sigmoid): (in:FLT) -> {
        @exp: (FLT) -> (FLT)
        return 1.0/(1.0 + exp!(in))
    }
    return sigmoid!(in) * (1.0 - sigmoid!(in))
}

(i): 3
WHILE i { 
    (a): "a"
    print!(a) 
    (i): i-1 
}

(m): MODEL<sigmoid sigmoid_derivative><in 32 out>

TRAIN<m 100 100 data_in data_out>

print!("done")