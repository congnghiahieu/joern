// https://doc.rust-lang.org/reference/patterns.html#slice-patterns

fn fixed() {
    // Fixed size
    let arr = [1, 2, 3];
    match arr {
        [1, _, _] => "starts with one",
        [a, b, c] => "starts with something else",
    };
}

fn dynamic() {
    // Dynamic size
    let v = vec![1, 2, 3];
    match v[..] {
        [a, b] => { /* this arm will not apply because the length doesn't match */ }
        [a, b, c] => { /* this arm will apply */ }
        _ => { /* this wildcard is required, since the length is not known statically */ }
    };
}
