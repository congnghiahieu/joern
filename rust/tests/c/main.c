// Marco definitions

#include <stdio.h>

#define PI 3.1415
#define SQUARE(x) ((x) * (x))
#define FOLLOWERS 1000
#define INSTAGRAM FOLLOWERS
#define PRINT_NUMBERS \
  printf("1\n");    \
  printf("2\n");    \
  printf("3\n");

int main() {
  float radius = 5.0;
  float area = PI * radius * radius;
  printf("Area of circle: %.2f\n", area);

  int num = 4;
  printf("Square of %d is %d\n", num, SQUARE(num));

  printf("Instagram followers: %d\n", INSTAGRAM);

  return 0;
}

// Variable declaration and initialization
int a = 10;
float b = 5.5;
char c = 'A';

// Array declaration and initialization
int arr[3] = {1, 2, 3};

// Pointer declaration and usage
int *p = &a;
printf("Value of a using pointer: %d\n", *p);

// If-else statement
if (a > 5) {
  printf("a is greater than 5\n");
} else {
  printf("a is not greater than 5\n");
}

// For loop
for (int i = 0; i < 3; i++) {
  printf("arr[%d] = %d\n", i, arr[i]);
}

// While loop
int count = 0;
while (count < 3) {
  printf("count = %d\n", count);
  count++;
}

// Do-while loop
count = 0;
do {
  printf("count in do-while = %d\n", count);
  count++;
} while (count < 3);

// Switch-case statement
switch (a) {
  case 5:
    printf("a is 5\n");
    break;
  case 10:
    printf("a is 10\n");
    break;
  default:
    printf("a is neither 5 nor 10\n");
}

// Function declaration and call
int add(int x, int y) {
  return x + y;
}
printf("Sum of 3 and 4 is %d\n", add(3, 4));

// Struct declaration and usage
struct Point {
  int x;
  int y;
};
struct Point p1 = {10, 20};
printf("Point p1: (%d, %d)\n", p1.x, p1.y);

// Typedef usage
typedef struct {
  int x;
  int y;
} Point2;
Point2 p2 = {30, 40};
printf("Point p2: (%d, %d)\n", p2.x, p2.y);

// Enum declaration and usage
enum Weekday { MON, TUE, WED, THU, FRI, SAT, SUN };
enum Weekday today = WED;
printf("Today is %d\n", today);