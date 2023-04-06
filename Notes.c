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
}
