// // Ví dụ đoạn mã nguồn cho cú pháp if let.
// fn main() {
//     let number: Option<i32> = None;

//     if let Some(i) = number {
//         println!("Matched number {:?}!", i);
//     } else {
//         // ...
//     }
// }

// // Ví dụ đoạn mã nguồn cho cú pháp while let.
// fn main() {
//     let mut optional = Some(0);

//     while let Some(i) = optional {
//         if i > 9 {
//             optional = None;
//         } else {
//             optional = Some(i + 1);
//         }
//     }
// }

// // Ví dụ đoạn mã nguồn cho cú pháp match.
// enum Color {
//     Red,
//     Blue(u32, u32, u32),
//     Green { red: u32, green: u32, blue: u32 },
// }

// fn main() {
//     let color = Color::Blue(0, 0, 255);

//     match color {
//         Color::Red => println!("The color is Red!"),
//         Color::Blue(r, g, b) => println!("R: {}, G: {}, B: {}!", r, g, b),
//         Color::Green { red, green, blue } => {
//             println!("Red: {}, Green: {}, Blue: {}!", red, green, blue)
//         }
//     }
// }

// // Ví dụ đoạn mã nguồn cho cú pháp lifetime.
// fn longest<'a>(x: &'a str, y: &'a str) -> &'a str {
//     if x.len() > y.len() {
//         x
//     } else {
//         y
//     }
// }

// // Ví dụ đoạn mã nguồn cho cú pháp lifetime kết hợp cú pháp where.
// fn f<'a, 'b, 'c, T>(x: &'a i32, mut y: &'b i32, z: &'c T)
// where
//     'b: 'a,
//     'c: 'b,
//     T: 'c,
// {
//     // ...
// }

// // Ví dụ đoạn mã nguồn cho RUSTSEC-2021-0086.
// fn main() {
//     let mut buf: Vec<u8> = Vec::with_capacity(N);
//     unsafe { buf.set_len(N) };
//     // After fix
//     let mut buf: Vec<u8> = vec![0; N];
// }

// // Ví dụ đoạn mã nguồn cho RUSTSEC-2022-0028.
// // Before fix
// pub fn external<'a, T>(data: T) -> Handle<'a, Self>
// where
//     T: AsMut<[u8]> + Send,
// {
//     // ...
// }

// // After fix
// pub fn external<'a, T>(data: T) -> Handle<'a, Self>
// where
//     T: AsMut<[u8]> + Send + 'static,
// {
//     // ...
// }

// // Ví dụ đoạn mã nguồn cho RUSTSEC-2020-0044.
// // Before fix
// mod before {
//     unsafe impl<P> Send for Atom<P> where P: IntoRawPtr + FromRawPtr {}
//     unsafe impl<P> Sync for Atom<P> where P: IntoRawPtr + FromRawPtr {}
// }

// // After fix
// mod after {
//     unsafe impl<P> Send for Atom<P> where P: IntoRawPtr + FromRawPtr + Send {}
//     unsafe impl<P> Sync for Atom<P> where P: IntoRawPtr + FromRawPtr + Sync {}
// }

// Ví dụ đoạn mã nguồn cho RUSTSEC-2021-0130.
// Before fix
mod before {
    pub fn iter<'a>(&'_ self) -> Iter<'a, K, V> {
        Iter {
            ptr: unsafe { (*self.head).next },
        }
    }
}
// After fix
mod after {
    pub fn iter(&self) -> Iter<'_, K, V> {
        Iter {
            ptr: unsafe { (*self.head).next },
        }
    }
}
