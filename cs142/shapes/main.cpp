#include <vector>
#include <iostream>
#include "draw.h"
#include "shapes.h"
using namespace std;

int main() {
  // Create some brushes
  Brush* skin1 = new SimpleBrush('O', Yellow);
  Brush* skin2 = new SimpleBrush('O', Salmon);
  Brush* empty = new SimpleBrush(' ', Black);
  Brush* eye = new SimpleBrush('.', White);
  Brush* iris = new SimpleBrush('#', Aqua);

  vector<Brush*> strips;
  strips.push_back(skin1);
  strips.push_back(skin2);
  Brush* skin = new StripedBrush(strips);

  // Set up the scene.
  vector<Shape*> v;
  v.push_back(new Circle(0, 0, 41, skin));
  v.push_back(new Ellipse(5, 23, 31, 11, empty));
  v.push_back(new Rectangle(5, 23, 31, 4, skin));
  v.push_back(new Ellipse(7, 9, 7, 9, eye));
  v.push_back(new Square(11, 12, 2, iris));
  v.push_back(new Ellipse(27, 9, 7, 9, eye));
  v.push_back(new Square(31, 12, 2, iris));

  // Prepare the canvas for drawing.
  hide_cursor();
  clear();

  // Draw the scene.
  for (unsigned int i = 0; i < v.size(); i++) {
    v[i]->draw();
  }

  // Wait for user input while they enjoy the scene.
  cin.get();

  // Clear the canvas.
  clear();
  show_cursor();

  // Delete the scene.
  for (unsigned int i = 0; i < v.size(); i++) {
    delete v[i];
  }

  // Delete the brushes.
  delete skin1;
  delete skin2;
  delete empty;
  delete eye;
  delete iris;
  delete skin;

  return 0;
}
