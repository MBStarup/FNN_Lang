dense(in out): {
    feedforward: (data: float[in]) -> float[out] {
        result: float[out]
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

    feedbackwards: (gradient: float[out] results: float[in] eta: float) -> float[in] {
        /* calculate gradient for prev layer */
        result: float[in]
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

    weights: float[out][in]
    biases: float[out]
}

sigmoid(in): {
    float[in] feedforward(data: float[in]) {
        result: float[in]
        for (i: 0 .. in) {
            result[i] = sigmoid(result[i])
        }
        result
    }
    
    feedbackwards: (gradient: float[in] results: float[in] eta: float) -> float[in] {
        result: float[in]
        for (i: 0 .. in) {
            result[in]: gradient[i] * (sigmoid(result[i]) * (1 - sigmoid(result[i])))
        }
        result
    }

    sigmoid: (x: float) -> float {
        1.0 / (1.0 + exp(-1 * x))
    }
}

l1: dense(784 128, sigmoid)
l3: dense(128 10, sigmoid)

m: model<><l1 l2 l3 l4><softmax>
for (e: 0 .. 1000) {
        train(m data 0.15)
}

save(m)
