@print: (STR) -> (INT)
@print_int: (INT) -> (INT)
@print_flt: (FLT) -> (INT)

(flip): (a:(INT INT) b:INT) -> {
    (x y):a
    return (x (y b))
}
(get_tuple): (_:INT)->{print!("GOT TUPLE\n") return (1 (2 3))}

(a b c): (1 (2.2 "str"))
print!("a:") print_int!(a) print!("\n")
print!("b:") print_flt!(b) print!("\n")
print!("c:") print!(c) print!("\n")
print!("done!\n")