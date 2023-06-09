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
// -- other config --
#define DATA_WIDTH 28
#define DATA_HEIGHT 28
#define SHUFFLE_N 100

// -- debugging tools --
#ifdef DEBUG_LOG
#define DEBUG(fmt, var) printf("%s: " fmt, #var, var)
#else
#define DEBUG(fmt, var) ;
#endif

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
} image_T;

typedef struct
{
    int in;
    int out;
    double *weights;
    double *biases;
    double (*activation)(double);
    double (*activation_derivative)(double);
} layer_T;

typedef struct
{
    int layer_amount;
    layer_T *layers;
} model_T;

int alloc_counter = 0;

// Allocates 'size' bytes initialized to 0 and asserts that the allocation succeeded
// Memory is still freed with ass_free()
void *ass_calloc(size_t size)
{
    ++alloc_counter;
    void *ptr = calloc(size, 1);
    assert(ptr != NULL);

#ifdef MEM_PRINT
    printf("calloc: %llu @ %llu\n", size, ptr);
#endif

    return ptr;
}

// Allocates 'size' bytes and asserts that the allocation succeeded
// Memory is still freed with ass_free()
void *ass_malloc(size_t size)
{
    ++alloc_counter;
    void *ptr = malloc(size);
    assert(ptr != NULL);

#ifdef MEM_PRINT
    printf("malloc: %llu @ %llu\n", size, ptr);
#endif

    return ptr;
}

void ass_free(void *ptr)
{
    --alloc_counter;
    free(ptr);

#ifdef MEM_PRINT
    printf("free: %llu\n", ptr);
#endif
}

void *ass_malloc_fnn_arr(int elem_size, int count)
{
    assert(elem_size > 0); // array elements must have positive size
    int *arr = ass_malloc(sizeof(int) + elem_size * count);
    arr[0] = count;
    return (&arr[1]);
}

void ass_free_fnn_arr(void *arr)
{
    ass_free(&(((int *)arr)[-1]));
}

void randomize_double_arr(double *arr, int size, double min, double max)
{
    for (int i = 0; i < size; i++)
    {
        arr[i] = min + (((double)rand()) / ((double)RAND_MAX)) * (max - min);
    }
}

model_T model_new(int amount, ...)
{
    model_T result;
    result.layer_amount = amount;
    result.layers = ass_malloc(sizeof(layer_T) * amount);
    va_list valist;
    va_start(valist, amount);

    for (int i = 0; i < amount; i++)
    {
        result.layers[i] = va_arg(valist, layer_T);
    }

    va_end(valist);

    return result;
}


// Shuffles an array by repeadedly picking two random indexes and swapping them arr_length * SHUFFLE_N times
// ------------------------------
// arr_length: the amount of elements in the array, accepted values: {1 .. SIZE_MAX}
// elem_size: the size of each element in bytes, accepted values: {1 .. SIZE_MAX}
// arr: the array to shuffle
void shuffle_arr(int arr_length, int elem_size, void *arr)
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
        int a = (int)(((double)rand() / (double)RAND_MAX) * (arr_length - 1)); 
        int b = (int)(((double)rand() / (double)RAND_MAX) * (arr_length - 1));
        memcpy(temp, array + (a * elem_size), elem_size);                    // temp = arr[a]
        memcpy(array + (a * elem_size), array + (b * elem_size), elem_size); // arr[a] = arr[b]
        memcpy(array + (b * elem_size), temp, elem_size);                    // arr[b] = temp
    }

    ass_free(temp);
}

layer_T layer_new(int in, int out, double (*a)(double), double (*a_derivative)(double))
{
    layer_T res;

    res.in = in;
    DEBUG("%d\n", res.in);
    res.out = out;
    DEBUG("%d\n", res.out);

    res.weights = ass_malloc(sizeof(double) * in * out);
    randomize_double_arr(res.weights, in * out, 0, 1);

    res.biases = ass_malloc(sizeof(double) * out);
    randomize_double_arr(res.biases, out, 0, 1);

    res.activation = a;
    res.activation_derivative = a_derivative;

    return res;
}

void layer_del(layer_T l)
{
    ass_free(l.biases);
    ass_free(l.weights);
}

void model_del(model_T m)
{
    for (int l = 0; l < m.layer_amount; l++)
    {
        layer_del(m.layers[l]);
    }
    ass_free(m.layers);
}

layer_T layer_copy(layer_T l)
{
    layer_T copy;
    copy.activation = l.activation;
    copy.activation_derivative = l.activation_derivative;
    copy.out = l.out;
    copy.in = l.in;
    copy.biases = ass_malloc(sizeof(double) * l.out);
    memcpy(copy.biases, l.biases, sizeof(double) * l.out);
    copy.weights = ass_malloc(sizeof(double) * l.out * l.in);
    memcpy(copy.weights, l.weights, sizeof(double) * l.out * l.in);
    return copy;
}

model_T model_copy(model_T m)
{
    model_T copy;
    copy.layer_amount = m.layer_amount;
    copy.layers = ass_malloc(sizeof(layer_T) * m.layer_amount);
    for (size_t i = 0; i < m.layer_amount; i++)
    {
        copy.layers[i] = layer_copy(m.layers[i]);
    }
    return copy;
}

// Calculates the weigted sum, does not apply any activation function
// ----------------------------
// Assumes the size of inputs matches the size of l.in
// if greater, will cut off remaning inputs
// if smaller will potentially segfault
// Assumes the size of outputs matches the size of l.out
// writes results to outputs
void layer_apply(layer_T l, double *inputs, double *outputs)
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

image_T parse_line(char *line)
{
    image_T result;
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

void print_double_arr(int print_width, int size, double *arr)
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

void _train_model(model_T model, double learning_rate, int epochs, double **input_data, double **expected_output, int data_amount)
{
    int layer_amount = model.layer_amount;
    layer_T *layers = model.layers;

    double *actual_results = ass_malloc(sizeof(double *) * (layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = (double **)(&(actual_results[1]));                       // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
                                                                                // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]

    DEBUG("%d\n", layer_amount);

    for (int layer = 0; layer < layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * layers[layer].out);
    }

    int *index = ass_malloc(sizeof(int) * data_amount);
    for (int i = 0; i < data_amount; i++)
    {
        index[i] = i;
    }

    DEBUG("%lf\n", learning_rate);
    DEBUG("%d\n", data_amount);
    for (int epoch = 0; epoch < epochs; epoch++)
    {

        // shuffle_arr(data_amount, sizeof(index[0]), index); // Shuffle array to use as index LMAO
        for (int training = 0; training < data_amount; training++)
        {

            // forward propegate
            results[-1] = input_data[index[training]]; // the "output" of the input "layer" is just the input data
            for (int layer = 0; layer < layer_amount; layer++)
            {

                layer_apply(layers[layer], results[layer - 1], results[layer]); // apply the dense layer
                for (int output = 0; output < layers[layer].out; output++)      // apply the activation
                {

                    results[layer][output] = layers[layer].activation(results[layer][output]);
                }
            }

            // setup for backpropagation
            double *dcost_dout = ass_calloc(sizeof(double) * layers[layer_amount - 1].out);

            // compute derivative of error with respect to network's output
            // ie. for the 'euclidian distance' cost function, (output  - expected)^2, this would be 2(output - expected) ∝ (output - expected)
            for (int out = 0; out < layers[layer_amount - 1].out; out++)
            {

                dcost_dout[out] = (results[layer_amount - 1][out] - expected_output[index[training]][out]);
            }

            // Backpropagate
            double *next_dcost_dout;
            for (int layer = layer_amount - 1; layer >= 0; layer--)
            {
                next_dcost_dout = ass_calloc(sizeof(double) * layers[layer].in); // alloc new array according to the previous layers (next in the backpropagation, since we're propagating backwards) output, aka this layers input

                for (int out = 0; out < layers[layer].out; out++)
                {
                    double dout_dz;
                    dout_dz = layers[layer].activation_derivative(results[layer][out]);
                    for (int input = 0; input < layers[layer].in; input++)
                    {

                        double dz_dw = results[layer - 1][input];
                        next_dcost_dout[input] += layers[layer].weights[out * layers[layer].in + input] * dcost_dout[out] * dout_dz; // uses old weight, so has to come before adjustment
                        layers[layer].weights[out * layers[layer].in + input] -= learning_rate * dcost_dout[out] * dout_dz * dz_dw;  // adjust weight
                    }
                    layers[layer].biases[out] -= learning_rate * dcost_dout[out] * dout_dz; // adjust bias
                }

                ass_free(dcost_dout);
                dcost_dout = next_dcost_dout; // reassign next_dcost_dout to dcost_dout before going to prev_layer
            }
            ass_free(next_dcost_dout);
        }
    }

    // clean up result buffers
    // results[-1] doesn't need to be cleaned, as it's just a pointer to part of the data[] array
    for (int result = 0; result < layer_amount; result++)
    {
        ass_free(results[result]);
    }
    ass_free(actual_results);
    ass_free(index);
}

void train_model(model_T model, double learning_rate, int epochs, double **input_data, double **expected_output)
{
    int size = ((int *)expected_output)[-1];

    _train_model(model, learning_rate, epochs, input_data, expected_output, size);
}

double naive_avg(double *vals, int count)
{
    double sum = 0;
    for (int i = 0; i < count; i++)
    {
        sum += vals[i];
    }
    return sum / ((double)count);
}

double _test_model(model_T model, double **input_data, double **expected_output, int data_amount)
{
    int layer_amount = model.layer_amount;
    layer_T *layers = model.layers;

    double *actual_results = ass_malloc(sizeof(double *) * (layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = (double **)(&(actual_results[1]));                       // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
                                                                                // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]

    DEBUG("%d\n", layer_amount);

    for (int layer = 0; layer < layer_amount; layer++)
    {
#ifdef DEBUG_LOG
        printf("l_%d: %d -> %d\n", layer, layers[layer].in, layers[layer].out);
#endif
        results[layer] = ass_malloc(sizeof(double) * layers[layer].out);
    }

    double *costs = ass_calloc(sizeof(double) * data_amount);
    double avg_scalar = ((double)1) / ((double)layers[layer_amount - 1].out);

    for (int test = 0; test < data_amount; test++)
    {

        // forward propegate
        results[-1] = input_data[test]; // the "output" of the input "layer" is just the input data
        for (int layer = 0; layer < layer_amount; layer++)
        {

            layer_apply(layers[layer], results[layer - 1], results[layer]); // apply the dense layer
            for (int output = 0; output < layers[layer].out; output++)      // apply the activation
            {

                results[layer][output] = layers[layer].activation(results[layer][output]);
            }
        }

        for (int out = 0; out < layers[layer_amount - 1].out; out++)
        {
            costs[test] += pow((results[layer_amount - 1][out] - expected_output[test][out]), 2);
        }
        costs[test] *= avg_scalar;
    }

    double result = naive_avg(costs, data_amount);
    ass_free(costs);
    for (int layer = 0; layer < layer_amount; layer++)
    {
        ass_free(results[layer]);
    }
    ass_free(actual_results);

    return result;
}

double test_model(model_T mdl, double **in, double **out)
{
    int size = ((int *)out)[-1];

    return _test_model(mdl, in, out, size);
}

// Expects the datqa to be formatted as lines in a csv, where the first elem is the correct index in the output categories, and the rest is the input data.
// largest_elem_size is the amount of chars in hte largest single element in the data, without the comma. Used for buffer allocation.
char *E_load_csv(char *filepath, int output_size, int input_size, int data_amount, int largest_elem_size)
{
    double **expected_outputs = ass_malloc_fnn_arr(sizeof(double *), data_amount);
    double **input_data = ass_malloc_fnn_arr(sizeof(double *), data_amount);

    DEBUG("%s\n", filepath);
    DEBUG("%d\n", data_amount);
    FILE *fptr = fopen(filepath, "r");
    int file_buffer_size = (largest_elem_size + 1) * (input_size + 1); // make the buffer just big enough to hold a single line of well formatted data: input size (plus one for the label) times the size of the largest elem, plus 1 for comma, like "xxx,"
    char *file_buffer = ass_malloc(sizeof(char) * file_buffer_size);

    // the first line (the one that explains the layout).
    fgets(file_buffer, file_buffer_size, fptr);
    for (int i = 0; i < data_amount; i++)
    {
        expected_outputs[i] = ass_malloc_fnn_arr(sizeof(double), output_size);
        input_data[i] = ass_malloc_fnn_arr(sizeof(double), input_size);

        char *line = fgets(file_buffer, file_buffer_size, fptr);
        assert(line != NULL); // Ran out of lines when reading training data, make sure data_amount <= the amount of lines of atual data in the csv
        {
            char *token = strtok(line, ",");
            int label = atoi(token);
            for (int j = 0; j < output_size; j++)
            {
                (expected_outputs)[i][j] = 0;
            }

            (expected_outputs)[i][label] = 1;

            for (int j = 0; j < input_size; j++)
            {
                token = strtok(NULL, ",");
                (input_data)[i][j] = atof(token);
            }
        }
    }
    fclose(fptr);
    ass_free(file_buffer);

    char *res = ass_malloc(sizeof(double **) * 2);
    (*((double ***)(&(res[0])))) = input_data;
    (*((double ***)(&(res[sizeof(double **)])))) = expected_outputs;
    return res;
}

int E_print(char *str)
{
    printf("%s", str);
    return 0;
}

int E_print_int(int i)
{
    printf("%d", i);
    return 0;
}

int E_print_flt(double f)
{
    printf("%lf", f);
    return 0;
}

int E_exit(int a)
{
    exit(a);
    return 0;
}

int E_tstwithprint(model_T m, double *image)
{
    double **actual_results = ass_malloc(sizeof(double *) * (m.layer_amount + 1));
    double **results = &(actual_results[1]);
    for (int layer = 0; layer < m.layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * m.layers[layer].out);
    }
    print_image_data(image);
    results[-1] = image;
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
    printf("Results\n");
    print_double_arr(m.layers[m.layer_amount - 1].out, m.layers[m.layer_amount - 1].out, results[m.layer_amount - 1]);
    printf("\n____________________________________\n");
    for (int result = 0; result < m.layer_amount; result++)
    {
        ass_free(results[result]);
    }
    ass_free(actual_results);
    return 0;
}
