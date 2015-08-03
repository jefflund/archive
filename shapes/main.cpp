#include <vector>
#include <iostream>
#include "draw.h"
#include "shapes.h"
using namespace std;

int main() {
  vector<Shape*> v;
  v.push_back(new Circle(0, 0, 50, '#', Yellow));
  v.push_back(new Circle(15, 25, 20, '.', Black));
  v.push_back(new Rectangle(15, 25, 10, 20, '#', Yellow));
  v.push_back(new Rectangle(15, 30, 5, 20, ']', White));
  v.push_back(new Square(25, 43, 6, '=', Red));
  v.push_back(new Circle(25, 45, 6, '=', Red));
  v.push_back(new Circle(13, 12, 9, 'O', White));
  v.push_back(new Square(18, 16, 2, '@', Aqua));
  v.push_back(new Circle(33, 12, 9, 'O', White));
  v.push_back(new Square(38, 16, 2, '@', Aqua));

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
