void FUNC0(int *res, int a)
{
    *res = a * a;
}

void FUNC1(int *res, int a, void (*func)(int *, int))
{
    (*func)(res, a);
    (*func)(res, *res);
}

int main(int argc, char const *argv[])
{
    void (*square)(int *, int) = &FUNC0;
    void (*apply_twice)(int *, int, void (*)(int *, int)) = &FUNC1;
    int b = 3;
    char *asd = "asda";
    float fg = 2.3;
    void (*math)(int *, int) = square;
    void (*math2)(int *, float *, int); // func that looks like this (int) -> (int float)
    void (*math3)(int *, float *, int); // func that looks like this (int) -> ((int float))

    // func (int ((int int int) (int int int))) -> (int)
    void (*math)(int *, int *, int *, int *, int *, int *, int *, int) = square;

    int a_T0;
    int a_T1;
    {
        int TEMP0;
        int TEMP1;
        make_tuple(&TEMP0, &TEMP1, 1, 2);
        int a_T0 = TEMP0;
        int a_T1 = TEMP1;
    }

    int b;
    {
        int TEMP2;
        cool(&TEMP2, a_T0, a_T1);
        b = TEMP2;
    }

    int a;
    {
        int TEMP4;
        apply_twice(&TEMP4, b, square);
        a = TEMP4;
    }

    int c;
    {
        int TEMP5;
        TEMP5 = (2 + 4 + 5);
        c = TEMP5;
    }

    void (*math)(int *, int);
    {
        void (*TEMP4)(int *, int);
        func_that_returns_func(&TEMP4);
        void (*math)(int *, int) = TEMP4;
    }

    int *r;
    {
        int *TEMP9;
        TEMP9 = malloc(sizeof(int), 7); // TODO: free
        func(&TEMP9, 7);
        r = TEMP9;
    }
    // This is not allowed lmao

    printf("%d\n", a);

    return 0;

    //(x y): make_tuple(10 20)
    int x;
    int y;
    {
        int TEMP0;
        int TEMP1;
        make_tuple(&TEMP0, &TEMP1, 10, 20);
        x = TEMP0;
        y = TEMP1;
    }

    int a_T1;
    int a_T2;
    {
        int TEMP0;
        int TEMP1;
        make_tuple(&TEMP0, &TEMP1, 10, 20);
        a_T1 = TEMP0;
        a_T2 = TEMP1;
    }

    int a;
    {
        int TEMP0;
        TEMP0 = 2 + 4;
        a = TEMP0;
    }

    // a: square(square(2));
    int a = ;
    int FINAL0;
    {
        int TEMP0;
        square(&TEMP0, 2);
        {
            int TEMP1;
            square(&TEMP1, TEMP0);
            FINAL0 = TEMP1;
        }
    }
    FINAL0;
}

/* paste for print
;
    double **actual_results = ass_malloc(sizeof(double *) * (n.layer_amount + 1)); // the actual stack allocated array for the results of one training example (including the input data)
    double **results = &(actual_results[1]);                                       // offset the indexing of results by one, basically creating a "-1" index, this way the indexing still matches the layers[]
    // results[-1] doesn't need a new allocated buffer, since it's just gonna be pointing to already allocated memory in data[]
    for (int layer = 0; layer < n.layer_amount; layer++)
    {
        results[layer] = ass_malloc(sizeof(double) * n.layers[layer].out);
    }
    // print examples to look at
    for (int printed_example = 0; printed_example < 5; printed_example++)
    {
        printf("Using model on data nr. (%d):\n", printed_example);
        print_image_data(data_in[printed_example]); // print the example image

        // forward propegate
        results[-1] = data_in[printed_example];
        for (int layer = 0; layer < n.layer_amount; layer++)
        {
            {
                layer_apply(n.layers[layer], results[layer - 1], results[layer]);
                for (int output = 0; output < n.layers[layer].out; output++)
                {
                    n.layers[layer].activation(&results[layer][output], results[layer][output]);
                }
            }
        }

        // softmax((layers[layer_amount - 1].out, results[layer_amount - 1], results[layer_amount - 1]);

        printf("Results data nr. (%d):\n", printed_example);
        print_double_arr(n.layers[n.layer_amount - 1].out, n.layers[n.layer_amount - 1].out, results[n.layer_amount - 1]);
        printf("\n____________________________________\n");
    }
    // clean up result buffers
    // results[-1] doesn't need to be cleaned, as it's just a pointer to part of the data[] array
    for (int result = 0; result < n.layer_amount; result++)
    {
        ass_free(results[result]);
    }
    ass_free(actual_results);




*/

/*

    void (*sig)(double *, double) = &E_sigmoid;
    void (*sig_d)(double *, double) = &E_derivative_of_sigmoid;
    double input = 0.69;
    double result;
    (*sig)(&result, input);
    DEBUG("%lf\n", result);
    (*sigmoid)(&result, input);
    DEBUG("%lf\n", result);
    exit(0);

*/