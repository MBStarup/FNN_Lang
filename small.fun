@train: (MDL INT INT [[FLT]] [[FLT]]) -> (INT)
@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@print: (STR) -> (INT)

(in): 784
(out): 10

(data): load_csv!("mnist_train.csv" out in 2000 50)

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

(m): MODEL<sigmoid sigmoid_derivative><in 32 out>

train!(m 100 4 data)

print!("done")