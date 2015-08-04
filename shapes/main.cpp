#include <vector>
#include <iostream>
#include "draw.h"
#include "shapes.h"
using namespace std;

int main() {
  vector<Shape*> v;
  v.push_back(new Circle(0, 0, 41, 'O', Yellow));
  v.push_back(new Ellipse(5, 23, 31, 11, ' ', Black));
  v.push_back(new Rectangle(5, 23, 31, 4, 'O', Yellow));
  v.push_back(new Ellipse(7, 9, 7, 9, '.', White));
  v.push_back(new Square(11, 12, 2, '#', Aqua));
  v.push_back(new Ellipse(27, 9, 7, 9, '.', White));
  v.push_back(new Square(31, 12, 2, '#', Aqua));

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
