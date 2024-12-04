fn main() {
    let mut optional = Some(0);

    while let Some(i) = optional {
        if i > 9 {
            optional = None;
        } else {
            optional = Some(i + 1);
        }
    }
}

// fn f<'a, 'b: 'a, 'c, 'd: 'c>(// x: &'a i32 , mut y: &'b i32, z: &'c i32
// )
// // where
// //     'a: 'b,
// {
// }

// fn f<'a, 'b, 'c: 'static>()
// where
//     'b: 'a + 'c,
// {
// }

// fn longest<'a>(x: &'a str, y: &'a str) -> &'a str {
//     // if x.len() > y.len() {
//     //     x
//     // } else {
//     //     y
//     // }
// }

// pub fn external<'a, C, T>(cx: &mut C, data: T) -> Handle<'a, Self>
// where
//     C: Context<'a>  ,
//     T: AsMut<[u8]> + Send + 'static,
// {
//     // ...
// }

// // Before fix
// pub fn iter<'a>(&'_ self) -> Iter<'a, K, V> {
//     // Iter {
//     //     ptr: unsafe { (*self.head).next },
//     // }
// }

// // After fix
// pub fn iter(&self) -> Iter<'_, K, V> {
//     // Iter {
//     //     ptr: unsafe { (*self.head).next },
//     // }
// }
