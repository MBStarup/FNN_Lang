dense(in out): {
    feedforward: (data: num[in]) -> num[out]: {
        result: num[out]
        for (o: 0 .. out)
        {
            accum: 0.0
            for (i: 0 .. in)
            {
                accum: accum + weights[o][i] * data[i]
            }
            result[o]: accum + biases[o]
        }
    }

    feedbackwards: (gradient: num[out] results: num[in] eta: num) -> num[in]: {
        /* calculate gradient for prev layer */
        result: num[in]
        for (i: 0 .. in)
        {
            result[i]: 0.0
            for (o: 0 .. out)
            {
                result[i]: result[i] + gradient_in[o] * weights[o][i]
            }
        }

        /* adjust  weights and biases based on gradient */
        for (o: 0 .. out)
        {
            for (i: 0 .. in)
            {
                weights[o][i]: weights[o][i] - (eta * gradient[o] * results[i])
            }
            biases[o]: biases[o] - eta * gradient[o]
        }
        result
    }

    weights: num[out][in]
    biases: num[out]
}

sigmoid(in): {
    feedforward: (data: num[in]) -> num[in]: {
        result: num[in]
        for (i: 0 .. in) {
            result[i] = sigmoid(result[i])
        }
        result
    }
    
    feedbackwards: (gradient: num[in] results: num[in] eta: num) -> num[in]: {
        result: num[in]
        for (i: 0 .. in) {
            result[in]: gradient[i] * (sigmoid(result[i]) * (1 - sigmoid(result[i])))
        }
        result
    }

    sigmoid: (x: num) -> num {
        1.0 / (1.0 + exp(-1 * x))
    }
}

l1: dense(784 128)
l2: sigmoid(128)
l3: dense(128 10)
l4: sigmoid(10)

m: model<><l1 l2 l3 l4><softmax>
for (e: 0 .. 1000) {
        train(m data 0.15)
}

save(m)
