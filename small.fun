


(input expected): loadcsv("mnist.csv")

(real complex): 10i+2j + 5i+0j

l_one: dense(784 64 sigmoid)
l_two: dense(32 10 sigmoid)
m: model<l_one dense(64 32 sigmoid) l_two>
train<m 3000 4 input expected>