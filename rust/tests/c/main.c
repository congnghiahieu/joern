#include <stdio.h>

// Object-like macro
#define PI 3.14159

// Function-like macro
#define SQUARE(x) ((x) * (x))

// Conditional compilation
#ifdef DEBUG
  #define LOG(msg) printf("DEBUG: %s\n", msg)
#else
  #define LOG(msg)
#endif

// Stringizing operator
#define TO_STRING(x) #x

// Token-pasting operator
#define CONCAT(a, b) a##b

int main() {
  // Using object-like macro
  printf("Value of PI: %f\n", PI);

  // Using function-like macro
  int num = 5;
  printf("Square of %d: %d\n", num, SQUARE(num));

  // Using conditional compilation
  LOG("This is a debug message");

  // Using stringizing operator
  printf("Stringized version of PI: %s\n", TO_STRING(PI));

  // Using token-pasting operator
  int xy = 100;
  printf("Value of xy: %d\n", CONCAT(x, y));

  return 0;
}