#include <stdio.h>
#include "c_ml_base.c"
#define TRAINING_DATA_AMOUNT 12
int main(int argc, char *argv[])
{
    double *training_data_input[TRAINING_DATA_AMOUNT];
    double *training_expected_output[TRAINING_DATA_AMOUNT];
    load_csv(training_expected_output, OUTPUT_SIZE, training_data_input, INPUT_SIZE, TRAINING_DATA_AMOUNT, "../c_ml/mnist_train.csv", 5);
    activation sigmoid_activationfunction;
    sigmoid_activationfunction.function = sigmoid;
    sigmoid_activationfunction.derivative = derivative_of_sigmoid;
    layer l_one = (layer_new(((784)), ((64)), sigmoid_activationfunction));
    layer l_two = (layer_new(((32)), ((10)), sigmoid_activationfunction));
    model m = (model_new(3, (l_one), (layer_new(((64)), ((32)), sigmoid_activationfunction)), (l_two)));
    (train_model((m), (3000), (4), training_data_input, training_expected_output, TRAINING_DATA_AMOUNT));
    return 0;
}