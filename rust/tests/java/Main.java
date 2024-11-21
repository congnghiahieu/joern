// Generic class
class Box<T> {
  private T value;

  public void set(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }
}

// Main class
public class Main {
  public static void main(String[] args) {
    Box<Integer> integerBox = new Box<>();
    integerBox.set(10);
    Box<String> stringBox = new Box<>();
    integerBox.get();
  }
}