// Before fix

mod before {
    pub fn iter<'a>(&'_ self) -> Iter<'a, K, V> {
        Iter {
            ptr: unsafe { (*self.head).next },
        }
    }
}

mod after {
    pub fn iter(&self) -> Iter<'_, K, V> {
        Iter {
            ptr: unsafe { (*self.head).next },
        }
    }
}
