#include <iostream>
#include "vector.h"
using namespace std;

int main() {
  // Demo some vector functionality.
  Vector a(3, 4);
  Vector b(5, 3);
  cout << a * 4 << endl;
  cout << a << endl;
  a += Vector(5, 6);
  a *= 2;
  cout << a << endl;
  cout << a + b << endl;
  cout << -b << endl;
  return 0;
}
