#include <vector>
#include <iostream>
#include "draw.h"
#include "shapes.h"
using namespace std;

int main() {
  vector<Shape*> v;
  v.push_back(new Circle(3, 2, 30, '#', Red));
  v.push_back(new Square(5, 4, 8, '@', Aqua));

  hide_cursor();
  clear();

  for (unsigned int i = 0; i < v.size(); i++) {
    v[i]->draw();
  }

  cin.get();
  show_cursor();


  for (unsigned int i = 0; i < v.size(); i++) {
    delete v[i];
    v[i] = NULL;
  }

  return 0;
}
