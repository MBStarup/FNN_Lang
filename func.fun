@print_int: (INT) -> (INT)

(get_func): ( a:INT ) -> {
    1
    return ( b:INT ) -> {1 return b*2}
}

print_int(get_func(0)(2))