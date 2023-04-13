#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <time.h>
#include <math.h>
#include <stdarg.h>

// -- ml config --
#define INPUT_SIZE 784
#define OUTPUT_SIZE 10
#define TRAINING_DATA_AMOUNT 12
#define EPOCHS 3000
#define BATCH_SIZE 4

// -- other config --
#define DATA_WIDTH 28
#define DATA_HEIGHT 28
#define PRINT_NUM 5
#define PRINTED_EXAMPLE 0
#define PRINTED_EXAMPLE_AMOUNT 12
#define SHUFFLE_N 100
#define _NO_PRINT

// -- debugging tools --
#define DEBUG(fmt, var) printf("%s: " fmt, #var, var)
#define SET_RED() printf("\e[31m")
#define SET_YELLOW() printf("\e[93m")
#define SET_GREEN() printf("\e[92m")
#define SET_RESET() printf("\e[0m")
#define DEBUG_LF(var)            \
    do                           \
    {                            \
        if (var < 0)             \
            SET_RED();           \
        if (var == 0)            \
            SET_YELLOW();        \
        if (var > 0)             \
            SET_GREEN();         \
        DEBUG("%+012.5lf", var); \
        SET_RESET();             \
    } while (0)

typedef struct
{
    int label;
    double img[INPUT_SIZE];
    double expected[OUTPUT_SIZE];
} image;

typedef struct
{
    double (*function)(double);
    double (*derivative)(double);
} activation;

typedef struct
{
    size_t in;
    size_t out;
    double *weights;
    double *biases;
    activation activation;
} layer;

typedef struct
{
    size_t layer_amount;
    layer *layers;
} model;

int alloc_counter = 0;

// Allocates 'size' bytes initialized to 0 and asserts that the allocation succeeded
// Memory is still freed with ass_free()
void *ass_calloc(size_t size)
{
    ++alloc_counter;
    void *ptr = calloc(size, 1);
    assert(ptr != NULL);
    return ptr;
}

// Allocates 'size' bytes and asserts that the allocation succeeded
// Memory is still freed with ass_free()
void *ass_malloc(size_t size)
{
    ++alloc_counter;
    void *ptr = malloc(size);
    assert(ptr != NULL);
    return ptr;
}

void ass_free(void *ptr)
{
    --alloc_counter;
    free(ptr);
}

void randomize_double_arr(double *arr, int size, double min, double max)
{
    for (int i = 0; i < size; i++)
    {
        arr[i] = min + (((double)rand()) / ((double)RAND_MAX)) * (max - min);
    }
}

model model_new(int amount, ...)
{
    model result;
    result.layer_amount = amount;
    result.layers = ass_malloc(sizeof(layer) * amount);
    va_list valist;
    va_start(valist, amount);

    for (size_t i = 0; i < amount; i++)
    {
        // result.layers[i] = ((layer*)((&amount)+1))[i];
        result.layers[i] = va_arg(valist, layer);
    }

    va_end(valist);

    return result;
}

int E_number = 69;

void E_square(int *r, int a) { *r = a * a; }

// char *arr_new(size_t amount, size_t elem_size, ...)
// {
//     char *result = ass_malloc(amount * elem_size);
//     ((size_t *)result)[-1] = amount;

//     va_list valist;
//     va_start(valist, amount * elem_size);

//     for (size_t i = 0; i < amount * elem_size; i++)
//     {
//         result[i] = va_arg(valist, char);
//     }

//     va_end(valist);
//     return result;
// }

// Shuffles an array by repeadedly picking two random indexes and swapping them arr_length * SHUFFLE_N times
// ------------------------------
// arr_length: the amount of elements in the array, accepted values: {1 .. SIZE_MAX}
// elem_size: the size of each element in bytes, accepted values: {1 .. SIZE_MAX}
// arr: the array to shuffle
void shuffle_arr(size_t arr_length, size_t elem_size, void *arr)
{
    typedef unsigned char byte;
    assert(sizeof(byte) == 1);

    assert(arr_length > 0); // Cannot shuffle arrays of length zero
    assert(elem_size > 0);  // Cannot shuffle arrays with zero size elements
    assert(arr != NULL);    // Cannot shuffle NULL

    byte *array = (byte *)arr;

    byte *temp = ass_malloc(elem_size); // A temp variable to store a value while we shuffle

    for (int i = 0; i < arr_length * SHUFFLE_N; i++)
    {
        // pick two random indicies in the arr
        size_t a = (size_t)(((double)rand() / (double)RAND_MAX) * (arr_length)); // Shouldn't this be "... * (arr_length - 1)"? Although when I do that it seems to never shuffle the last one so...
        size_t b = (size_t)(((double)rand() / (double)RAND_MAX) * (arr_length)); // Shouldn't this be "... * (arr_length - 1)"? Although when I do that it seems to never shuffle the last one so...
        // if (a == PRINTED_EXAMPLE || b == PRINTED_EXAMPLE)
        //     printf("Shufflin %d and %d\n", a, b);
        memcpy(temp, array + (a * elem_size), elem_size);                    // temp = arr[a]
        memcpy(array + (a * elem_size), array + (b * elem_size), elem_size); // arr[a] = arr[b]
        memcpy(array + (b * elem_size), temp, elem_size);                    // arr[b] = temp
    }

    ass_free(temp);
}

layer layer_new(int in, int out, activation activation)
{
    layer res;

    res.in = in;
    res.out = out;

    res.weights = ass_malloc(sizeof(double) * in * out);
    randomize_double_arr(res.weights, in * out, 0, 1);

    res.biases = ass_malloc(sizeof(double) * out);
    randomize_double_arr(res.biases, out, 0, 1);

    res.activation = activation;

    return res;
}

void layer_del(layer l)
{
    ass_free(l.biases);
    ass_free(l.weights);
}

// Calculates the weigted sum, does not apply any activation function
// ----------------------------
// Assumes the size of inputs matches the size of l.in
// if greater, will cut off remaning inputs
// if smaller will potentially segfault
// Assumes the size of outputs matches the size of l.out
// writes results to outputs
void layer_apply(layer l, double *inputs, double *outputs)
{
    for (int i_out = 0; i_out < l.out; i_out++)
    {
        double accum = 0;
        for (int i_in = 0; i_in < l.in; i_in++)
        {
            accum += l.weights[i_out * l.in + i_in] * (inputs[i_in]);
        }
        outputs[i_out] = accum + l.biases[i_out];
    }
}

image parse_line(char *line)
{
    image result;
    char *token = strtok(line, ",");
    result.label = atoi(token);
    for (int i = 0; i < OUTPUT_SIZE; i++)
    {
        result.expected[i] = 0;
    }

    result.expected[result.label] = 1;

    for (int i = 0; i < INPUT_SIZE; i++)
    {
        token = strtok(NULL, ",");
        result.img[i] = ((double)atoi(token)) / 255;
    }
    return result;
}

void print_image_data(double *data)
{
    SET_GREEN();
    // printf("Label: %d\n", d.label);
    for (int i = 0; i < DATA_WIDTH; i++)
    {
        for (int j = 0; j < DATA_HEIGHT; j++)
        {
            data[i * DATA_WIDTH + j] > 0 ? printf("  ") : printf("[]");
        }
        printf("\n");
    }
    SET_RESET();
}

void print_double_arr(size_t print_width, size_t size, double *arr)
{
    for (int i = 0; i < size; i++)
    {
        printf("%+012.5lf, ", arr[i]);
        if (i % print_width == (print_width - 1) && i + 1 < size)
        {
            printf("\n");
        }
    }
}

void softmax(int size, double *inputs, double *outputs)
{
    double *e_arr = ass_malloc(sizeof(double) * size);
    double accum = 0;

    for (int i = 0; i < size; i++)
    {
        e_arr[i] = exp(inputs[i]);
        accum += e_arr[i];
    }

    for (int i = 0; i < size; i++)
    {
        outputs[i] = e_arr[i] / accum;
    }
    ass_free(e_arr);
}

// return (x > 0) ? x : 0;
double relu(double x)
{
    return (x > 0) ? x : 0;
}

// return x > 0;
double derivative_of_relu(double x)
{
    return x > 0;
}

// return 1.0 / (1.0 + exp(-1 * x));
double sigmoid(double x)
{
    return 1.0 / (1.0 + exp(-1 * x));
}

// return sigmoid(x) * (1 - sigmoid(x));
double derivative_of_sigmoid(double x)
{
    return sigmoid(x) * (1 - sigmoid(x));
}

void _train_model(model model, int epochs, int batch_size, double **input_data, double **expected_output, int data_amount)
{

    int layer_amount = model.layer_amount;
    layer *layers = model.layers;

    double *actual_results = ass_malloc(sizeof(double *) * (layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = &(actual_results[1]);                                    // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
    // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]

    for (int layer = 0; layer < layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * layers[layer].out);
    }

    int *index = ass_malloc(sizeof(int) * data_amount);
    for (size_t i = 0; i < data_amount; i++)
    {
        index[i] = i;
    }

    const size_t batch_amount = data_amount / batch_size;
    assert(batch_amount * batch_size == data_amount); // DATA_AMOUNT should be divisble by batch_size
    for (int epoch = 0; epoch < epochs; epoch++)
    {

        shuffle_arr(data_amount, sizeof(index[0]), index); // Shuffle array to use as index LMAO

        for (int batch = 0; batch < batch_amount; batch++)
        {

            for (int training = 0; training < batch_size; training++)
            {

                // forward propegate
                results[-1] = input_data[index[batch * batch_size + training]]; // the "output" of the input "layer" is just the input data
                for (int layer = 0; layer < layer_amount; layer++)
                {

                    layer_apply(layers[layer], results[layer - 1], results[layer]); // apply the dense layer
                    for (int output = 0; output < layers[layer].out; output++)      // apply the activation
                    {

                        results[layer][output] = layers[layer].activation.function(results[layer][output]);
                    }
                }

                // setup for backpropagation
                double *dcost_dout = ass_calloc(sizeof(double) * layers[layer_amount - 1].out);

                // compute derivative of error with respect to network's output
                // ie. for the 'euclidian distance' cost function, (output  - expected)^2, this would be 2(output - expected) ∝ (output - expected)
                for (int out = 0; out < layers[layer_amount - 1].out; out++)
                {

                    dcost_dout[out] = (results[layer_amount - 1][out] - expected_output[index[batch * batch_size + training]][out]);
                }

                // Backpropagate
                double eta = 0.15;
                double *next_dcost_dout;
                for (int layer = layer_amount - 1; layer >= 0; layer--)
                {

                    /*
                     * side note:
                     * we're being kinda wastefull here to help generalize, since we're allocating a big array for the dcost_dout of the input values,
                     * values for it, just to throw them out since that isn't a real layer. Definetly a possible place to optimize
                     * if we're fine with introducing more hard coded "edge cases" such as the first and last loop
                     */
                    next_dcost_dout = ass_calloc(sizeof(double) * layers[layer].in); // alloc new array according to the previous layers (next in the backpropagation, since we're propagating backwards) output, aka this layers input

                    for (int out = 0; out < layers[layer].out; out++)
                    {

                        double dout_dz = layers[layer].activation.derivative(results[layer][out]); //! <- only real diff I can see is that in the example that works, this uses the "Out" value after activation instead of the "z" value before activation, so why does 3B1B say it's the derivative of the activation of z???
                        for (int input = 0; input < layers[layer].in; input++)
                        {

                            double dz_dw = results[layer - 1][input];
                            next_dcost_dout[input] += layers[layer].weights[out * layers[layer].in + input] * dcost_dout[out] * dout_dz; // uses old weight, so has to come before adjustment
                            layers[layer].weights[out * layers[layer].in + input] -= eta * dcost_dout[out] * dout_dz * dz_dw;            // adjust weight
                        }
                        layers[layer].biases[out] -= eta * dcost_dout[out] * dout_dz; // adjust bias
                    }

                    ass_free(dcost_dout);
                    dcost_dout = next_dcost_dout; // reassign next_dcost_dout to dcost_dout before going to prev_layer
                }

                ass_free(next_dcost_dout);
            }
        }
    }

    // clean up result buffers
    // results[-1] doesn't need to be cleaned, as it's just a pointer to part of the data[] array
    for (int result = 0; result < layer_amount; result++)
    {
        ass_free(results[result]);
    }
}

void train_model(model model, int epochs, int batch_size, double **input_data, double **expected_output)
{
    int size = ((size_t *)expected_output)[-1];
    _train_model(model, epochs, batch_size, input_data, expected_output, size);
}

// Expects the datqa to be formatted as lines in a csv, where the first elem is the correct index in the output categories, and the rest is the input data.
// largest_elem_size is the amount of chars in hte largest single element in the data, without the comma. Used for buffer allocation.
void E_load_csv(double ***expected_outputs, double ***input_data, char *filepath, int output_size, int input_size, int data_amount, int largest_elem_size)
{
    *expected_outputs = ass_malloc(sizeof(size_t) + sizeof(double *) * data_amount);
    *input_data = ass_malloc(sizeof(size_t) + sizeof(double *) * data_amount);
    ((size_t *)(*expected_outputs))[-1] = data_amount;
    ((size_t *)(*input_data))[-1] = data_amount;

    printf("Opening file: %s\n", filepath);
    FILE *fptr = fopen(filepath, "r");

    int file_buffer_size = (largest_elem_size + 1) * (input_size + 1); // make the buffer just big enough to hold a single line of well formatted data: input size (plus one for the label) times the size of the largest elem, plus 1 for comma, like "xxx,"
    char *file_buffer = ass_malloc(sizeof(char) * file_buffer_size);

    // the first line (the one that explains the layout).
    fgets(file_buffer, file_buffer_size, fptr);

    printf("\n");
    for (int i = 0; i < data_amount; i++)
    {
        (*expected_outputs)[i] = ((int *)ass_malloc(sizeof(size_t) + sizeof(double) * output_size))[1]; // TODO: free
        (*input_data)[i] = ((int *)ass_malloc(sizeof(size_t) + sizeof(double) * input_size))[1];        // TODO: free
        ((size_t *)((*expected_outputs)[i]))[-1] = output_size;
        ((size_t *)((*input_data)[i]))[-1] = input_size;

        char *line = fgets(file_buffer, file_buffer_size, fptr);
        assert(line != NULL); // Ran out of lines when reading training data, make sure data_amount <= the amount of lines of atual data in the csv
        {
            char *token = strtok(line, ",");
            int label = atoi(token);
            for (int j = 0; j < output_size; j++)
            {
                (*expected_outputs)[i][j] = 0;
            }

            (*expected_outputs)[i][label] = 1;

            for (int j = 0; j < input_size; j++)
            {
                token = strtok(NULL, ",");
                (*input_data)[i][j] = atof(token);
            }
        }
    }
    fclose(fptr);
    ass_free(file_buffer);
}

void E_print(int *r, char *str)
{
    *r = 0;
    printf("%s", str);
}
void E_print_int(int *r, int i)
{
    *r = 0;
    printf("%d", i);
}
void E_print_flt(int *r, double f)
{
    *r = 0;
    printf("%lf", f);
}

void E_exit(int *r, int a)
{
    *r = a;
    exit(a);
}

void E_getarr(int **r, int a)
{
    size_t *arr = ass_malloc(sizeof(size_t) + a);
    arr[0] = a;
    (*r) = &(arr[1]);
    for (size_t i = 0; i < a; i++)
    {
        (*r)[i] = 69 + i;
    }
}