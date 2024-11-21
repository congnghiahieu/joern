fn cal_sum(n: i32) {
    let mut sum = 0;
    let mut i = 1;
    while i <= n {
        sum += i;
        i += 1;
    }
    return sum;
}
