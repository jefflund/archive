#include "draw.h"
#include "shapes.h"

Shape::Shape(int x, int y, int size, char brush, int color) {
  x_ = x;
  y_ = y;
  size_ = size;
  brush_ = brush;
  color_ = color;
}

Shape::~Shape() {}

Square::Square(int x, int y, int size, char brush, int color)
  : Shape(x, y, size, brush, color) {}

void Square::draw() {
  for (int x = x_; x < x_ + size_; x++) {
    for (int y = y_; y < y_ + size_; y++) {
      draw_at(x, y, brush_, color_);
    }
  }
}

Circle::Circle(int x, int y, int size, char brush, int color)
  : Shape(x, y, size, brush, color) {}

void Circle::draw() {
  for (int x = x_; x < x_ + size_; x++) {
    for (int y = y_; y < y_ + size_; y++) {
      if (inside_circle(x, y)) {
        draw_at(x, y, brush_, color_);
      }
    }
  }
}

bool Circle::inside_circle(int x, int y) {
  double radius = size_ / 2.0; // Radius is half the size

  double center_x = x_ + radius;
  double dx = (x - center_x);

  double center_y = y_ + radius;
  double dy = (y - center_y);

  // Squared form of the equation of circle.
  return dx * dx + dy * dy <= radius * radius;
}
