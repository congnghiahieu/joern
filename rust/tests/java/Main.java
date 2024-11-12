public class Main {
  public static void main(String[] args) {
    int x = 10;

    // Conventional if-elseif-else
    if (x > 10) {
      System.out.println("x is greater than 10");
    } else if (x == 10) {
      System.out.println("x is equal to 10");
    } else {
      System.out.println("x is less than 10");
    }

    // Conventional for loop
    for (int i = 0; i < 5; i++) {
      System.out.println("For loop iteration: " + i);
    }

    // Conventional while loop
    int y = 0;
    while (y < 5) {
      System.out.println("While loop iteration: " + y);
      y++;
    }
  }
}