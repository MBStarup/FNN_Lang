@print_int: (INT) -> (INT)
@print: (STR) -> (INT)

print!("stt")

(get_func): ( a:INT ) -> {
    return ( b:INT ) -> {return b*2}
}

(_for): (i:INT f:(INT) -> (INT)) -> { WHILE i { f!(i) (i): (i - 1) } return 1 }

_for!(69 (i:INT) -> { print_int!(i) print!("\n") return 1 } )

print_int!(get_func!(0)!(2))