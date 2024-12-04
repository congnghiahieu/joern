use std::mem;
use std::sync::Arc;
use std::sync::atomic::{AtomicPtr, Ordering};
use std::ops::Deref;


/// AtomicBox<T> is a safe wrapper around AtomicPtr<T>
/// You can safely swap values using the replace_with method
#[derive(Debug)]
pub struct AtomicBox<T: Sized>
{
    ptr: AtomicPtr<T>,
}

impl<T: Sized> AtomicBox<T> {
    /// Allocates a new AtomicBox containing the given value
    pub fn new(value: T) -> AtomicBox<T> {
        AtomicBox {
            ptr: AtomicPtr::new(AtomicBox::alloc_from(value)),
        }
    }

    #[inline]
    fn alloc_from(value: T) -> *mut T {
        let total: Arc<T> = Arc::new(value);

        Arc::into_raw(total) as *mut T
    }

    fn compare_and_swap(&self,
                        current: *mut T,
                        new: *mut T,
                        order: Ordering) -> *mut T {
        self.ptr.compare_and_swap(current, new, order)
    }

    fn take(&self) -> Arc<T> {
        loop {
            let curr = self.ptr.load(Ordering::Acquire);
            let null: *mut T = std::ptr::null_mut();

            if curr == null {
                continue;
            }

            if self.compare_and_swap(curr, null, Ordering::AcqRel) == curr {
                return unsafe { Arc::from_raw(curr) };
            }
        }
    }

    fn release(&self, ptr: *mut T) {
        assert!(ptr != 0xfffffffffffffff0 as *mut T);
        self.ptr.store(ptr, Ordering::Release);
    }

    pub fn get(&self) -> Arc<T> {
        let val = self.take();
        let copy = Arc::clone(&val);
        let ptr = Arc::into_raw(val) as *mut T;

        self.release(ptr);
        copy
    }

    /// Atomically replace the inner value with the result of applying the
    /// given closure to the current value
    pub fn replace_with<F>(&self, f: F)
        where F: Fn(Arc<T>) -> T
    {
        let val = self.take();
        let new_val = f(val);
        let ptr = Arc::into_raw(Arc::new(new_val)) as *mut T;
        self.release(ptr);
    }
}

impl<T: Sized + PartialEq> PartialEq for AtomicBox<T> {
    fn eq(&self, other: &AtomicBox<T>) -> bool {
        self == other
    }
}

impl<T: Sized> Drop for AtomicBox<T> {
    fn drop(&mut self) {
        unsafe {
            Arc::from_raw(self.ptr.load(Ordering::Acquire))
        };
    }
}

unsafe impl<T: Sized> Sync for AtomicBox<T> {}
unsafe impl<T: Sized> Send for AtomicBox<T> {}

#[cfg(test)]
mod tests {
    use std::sync::{Arc, Barrier};
    use std::thread;

    use super::AtomicBox;

    #[test]
    fn atomic_arc_new() {
        let b = AtomicBox::new(1024);

        assert_eq!(*b.get(), 1024);
    }

    #[test]
    fn atomic_arc_replace_with() {
        let value: i64 = 1024;
        let b = AtomicBox::new(value);

        b.replace_with(|x| *x  * 2);

        assert_eq!(*b.get(), value * 2);
    }

    #[test]
    fn atomic_arc_replace_with_ten_times() {
        let value = 1024;
        let b = AtomicBox::new(value);

        for _i in 0..10 {
            b.replace_with(|x| *x * 2);
        }

        assert_eq!(*b.get(), value * 2_i32.pow(10));
    }

    #[test]
    fn atomic_arc_replace_instance() {
        let b = Arc::new(AtomicBox::new(1024));
        let b1 = b.clone();

        b1.replace_with(|x| *x * 2);

        assert_eq!(*b.get(), 2048);
    }

    #[test]
    fn atomic_arc_threaded_leak_test() {
        let val = Arc::new(AtomicBox::new(10));
        let val_cpys: Vec<Arc<AtomicBox<i32>>> = (0..10)
            .map(|_| val.clone())
            .collect();
        let mut guards = Vec::new();

        for i in 0..10 {
            let val_cpy = val_cpys[i].clone();
            let guard = thread::spawn(move || {
                val_cpy.replace_with(|x| *x * 2);
            });

            guards.push(guard);
        }

        for g in guards {
            g.join().unwrap();
        }

        assert_eq!(*val.get(), 10 * 2_i32.pow(10));
    }

    #[test]
    fn atomic_arc_threaded_contention() {
        let abox = Arc::new(AtomicBox::new(0));
        let thread_num = 10;
        let mut guards = Vec::new();
        let barrier = Arc::new(Barrier::new(thread_num));

        for _i in 0..thread_num {
            let b = Arc::clone(&barrier);
            let cpy = abox.clone();
            guards.push(thread::spawn(move || {
                b.wait();
                for _j in 0..1000 {
                    cpy.replace_with(|x| *x + 100)
                }
            }));
        }

        for g in guards {
            g.join().unwrap();
        }

        assert_eq!(*abox.get(), thread_num * 1000 * 100);
    }

    #[test]
    fn atomic_arc_vector_container() {
        let values: Vec<i32> = (0..10).map(|x: i32| {
            x.pow(2)
        }).collect();
        let abox = Arc::new(AtomicBox::new(vec![]));
        let mut guards = Vec::new();

        for i in 0..10 {
            let cpy = abox.clone();
            let values: Vec<i32> = values.clone();

            guards.push(thread::spawn(move || {
                cpy.replace_with(|x| {
                    let mut nx = (*x).clone();
                    nx.push(values[i]);
                    nx
                })
            }));
        }

        for g in guards {
            g.join().unwrap();
        }

        assert_eq!(abox.get().len(), values.len());

        for i in values {
            assert_eq!(abox.get().contains(&i), true);
        }
    }
}
