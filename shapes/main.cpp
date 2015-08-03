#include <iostream>
#include "draw.h"
using namespace std;

int main() {
  hide_cursor();
  clear();
  draw_at(8, 10, "Hello World!");
  draw_at(3, 6, "Hello World!", Red);
  cin.get();
  show_cursor();
  return 0;
}
