@print_int: (INT) -> (INT)

(add): ( a:INT b:INT ) -> {
    (b): a + b
    return b
}

print_int(add(1 2))