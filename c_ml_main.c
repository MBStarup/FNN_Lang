#include "c_ml_base.c"
#define TRAINING_DATA_AMOUNT 1
#define BATCH_SIZE TRAINING_DATA_AMOUNT
#define PRINTED_EXAMPLE_AMOUNT 1

int main(int argc, char const *argv[])
{
    srand(420);

    layer_T layers[] = {layer_new(INPUT_SIZE, 128, &E_sigmoid, &E_derivative_of_sigmoid), layer_new(128, OUTPUT_SIZE, &E_sigmoid, &E_derivative_of_sigmoid)};
    const int layer_amount = sizeof(layers) / sizeof(layers[0]);
    DEBUG("%d\n", layer_amount);

    double **actual_results = ass_malloc(sizeof(double *) * (layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = &(actual_results[1]);                                     // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
    // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]
    for (int layer = 0; layer < layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * layers[layer].out);
    }

    double **training_data_input;      // = ass_malloc_fnn_arr(sizeof(double *), TRAINING_DATA_AMOUNT);
    double **training_expected_output; // = ass_malloc_fnn_arr(sizeof(double *), TRAINING_DATA_AMOUNT);

    E_load_csv(&training_expected_output, &training_data_input, "./mnist_train.csv", OUTPUT_SIZE, INPUT_SIZE, TRAINING_DATA_AMOUNT, 50);
    model_T m = model_new(layer_amount, layers[0], layers[1]);
    train_model(m, EPOCHS, BATCH_SIZE, training_data_input, training_expected_output);

    // print examples to look at
    for (int printed_example = PRINTED_EXAMPLE; printed_example < PRINTED_EXAMPLE_AMOUNT; printed_example++)
    {
        printf("Using model on data nr. (%d):\n", printed_example);
        print_image_data(training_data_input[printed_example]); // print the example image

        // forward propegate
        results[-1] = training_data_input[printed_example];
        for (int layer = 0; layer < layer_amount; layer++)
        {
            {
                layer_apply(layers[layer], results[layer - 1], results[layer]);
                for (int output = 0; output < layers[layer].out; output++)
                {
                    layers[layer].activation(&results[layer][output], results[layer][output]);
                }
            }
        }

        // softmax((layers[layer_amount - 1].out, results[layer_amount - 1], results[layer_amount - 1]);

        printf("Results data nr. (%d):\n", printed_example);
        print_double_arr(layers[layer_amount - 1].out, layers[layer_amount - 1].out, results[layer_amount - 1]);
        printf("\n____________________________________\n");
    }

    // clean up result buffers
    // results[-1] doesn't need to be cleaned, as it's just a pointer to part of the data[] array
    for (int result = 0; result < layer_amount; result++)
    {
        ass_free(results[result]);
    }
    ass_free(actual_results);

    model_del(m);

    for (int i = 0; i < ((int *)training_data_input)[-1]; i++)
    {
        ass_free_fnn_arr(training_data_input[i]);
    }
    ass_free_fnn_arr(training_data_input);

    for (int i = 0; i < ((int *)training_expected_output)[-1]; i++)
    {
        ass_free_fnn_arr(training_expected_output[i]);
    }
    ass_free_fnn_arr(training_expected_output);

    DEBUG("%d\n", alloc_counter);
    return 0;
}
