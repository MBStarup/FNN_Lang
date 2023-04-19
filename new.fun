@print: (STR) -> (INT)
@print_int: (INT) -> (INT)
@print_flt: (FLT) -> (INT)
@print_lyr: (LYR) -> (INT)
@exit: (INT) -> (INT)
@new_dense: (INT INT (FLT)->(FLT) (FLT)->(FLT)) -> (LYR)
@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))
@train: (MDL INT INT ([[FLT]] [[FLT]])) -> (INT)

(a_sigmoid): (a:FLT) -> {1 return .2}
(d_sigmoid): (a:FLT) -> {1 return .2}

(layer): new_dense(784 128 (a:FLT) -> {1 return .2} (a:FLT) -> {1 return .2})
print_lyr(layer)

(data): load_csv("../../c/c_ml/mnist_train.csv", 784, 10, 1000, 50)

(model): MODEL<layer new_dense(128 10 a_sigmoid d_sigmoid)>
train(model 100 4 data)
print("done")
