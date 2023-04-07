#include <stdio.h>
#include "c_ml_base.c"
int main(int argc, char* argv[]){ activation sigmoid_activationfunction;
sigmoid_activationfunction.function = sigmoid;
sigmoid_activationfunction.derivative = derivative_of_sigmoid;
void (*load_csv)(double * *,double * **,char *,int,int,int,int) = &E_load_csv;;int number = E_number;;void (*square)(int*,int) = &E_square;;int a = ({int TEMP0;(*square)(&TEMP0,(69420));TEMP0;;});
;double * *train_input;double * *train_expected;{void **TEMP = ({double * *TEMP0;double * *TEMP1;(*load_csv)(&TEMP0,&TEMP1,("train_mnist.csv"),(10),(784),(20000),(5));(void*[]){&TEMP0,&TEMP1};});train_input = (*((double * **)(TEMP[0])));train_expected = (*((double * **)(TEMP[1])));};double * *test_input;double * *test_expected;{void **TEMP = ({double * *TEMP0;double * *TEMP1;(*load_csv)(&TEMP0,&TEMP1,("test_mnist.csv"),(10),(784),(500),(5));(void*[]){&TEMP0,&TEMP1};});test_input = (*((double * **)(TEMP[0])));test_expected = (*((double * **)(TEMP[1])));};layer l_one = (layer_new(((784)),((64)),sigmoid_activationfunction));
;layer l_two = (layer_new(((32)),((10)),sigmoid_activationfunction));
;model m = (model_new(3,(l_one),(layer_new(((64)),((32)),sigmoid_activationfunction)),(l_two)));
;(train_model((m),(3000),(4), training_data_input, training_expected_output, TRAINING_DATA_AMOUNT));return 0;}