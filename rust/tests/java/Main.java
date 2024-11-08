public class Main {
  public static void main(String[] args) {
    // Variable declaration and initialization
    int number = 10;
    String text = "Hello, World!";

    // Conditional statement
    if (number > 5) {
      System.out.println("Number is greater than 5");
    } else {
      System.out.println("Number is 5 or less");
    }

    // Loop statement
    for (int i = 0; i < 5; i++) {
      System.out.println("Loop iteration: " + i);
    }

    // Method call
    printMessage("This is a method call");

    // Array declaration and initialization
    int[] numbers = {1, 2, 3, 4, 5};
    for (int num : numbers) {
      System.out.println("Array element: " + num);
    }

    // Class instantiation
    Person person = new Person("John", 25);
    person.displayInfo();
  }

  // Method definition
  public static void printMessage(String message) {
    System.out.println(message);
  }
}

// Class definition
class Person {
  // Fields
  private String name;
  private int age;

  // Constructor
  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }

  // Method
  public void displayInfo() {
    System.out.println("Name: " + name + ", Age: " + age);
  }
}