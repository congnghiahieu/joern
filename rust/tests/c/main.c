#include <stdio.h>

int main() {
  int x, y;
  printf("Enter value for x: ");
  scanf("%d", &x);
  printf("Enter value for y: ");
  scanf("%d", &y);

  if (x > y) {
    return x;
  } else {
    return y;
  }
}