#include <iostream>
#include "draw.h"
using namespace std;

int main() {
  hide_cursor();
  clear();
  draw_at(3, 6, "Hello World!");
  cin.get();
  show_cursor();
  return 0;
}
