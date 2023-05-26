#include "c_ml_base.c"

int main(int argc, char const *argv[])
{
    srand(420);

    model_T m = model_new((2), layer_new(INPUT_SIZE, 128, &sigmoid, &derivative_of_sigmoid), layer_new(128, OUTPUT_SIZE, &sigmoid, &derivative_of_sigmoid));
    DEBUG("%d\n", m.layer_amount);

    double **actual_results = ass_malloc(sizeof(double *) * (m.layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = &(actual_results[1]);                                       // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
    // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]
    for (int layer = 0; layer < m.layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * m.layers[layer].out);
    }

    char *data = E_load_csv("./mnist_train.csv", 10, 784, 100, 7);
    double **training_data_input = *((double ***)(&(data[0])));
    double **training_expected_output = *((double ***)(&(data[sizeof(double **)])));
    train_model(m, 0.15, 1, training_data_input, training_expected_output);

    // print examples to look at
    for (int printed_example = 0; printed_example < 1; printed_example++)
    {
        printf("Using model on data nr. (%d):\n", printed_example);
        print_image_data(training_data_input[printed_example]); // print the example image

        // forward propegate
        results[-1] = training_data_input[printed_example];
        for (int layer = 0; layer < m.layer_amount; layer++)
        {
            {
                layer_apply(m.layers[layer], results[layer - 1], results[layer]);
                for (int output = 0; output < m.layers[layer].out; output++)
                {
                    results[layer][output] = m.layers[layer].activation(results[layer][output]);
                }
            }
        }

        printf("Results data nr. (%d):\n", printed_example);
        print_double_arr(m.layers[m.layer_amount - 1].out, m.layers[m.layer_amount - 1].out, results[m.layer_amount - 1]);
        printf("\n____________________________________\n");
    }

    // clean up result buffers
    // results[-1] doesn't need to be cleaned, as it's just a pointer to part of the data[] array
    for (int result = 0; result < m.layer_amount; result++)
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

    ass_free(data);

    DEBUG("%d\n", alloc_counter);
    return 0;
}
