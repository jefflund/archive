#include "draw.h"
#include "shapes.h"

Shape::Shape(int x, int y, int width, int height, char brush, int color) {
  x_ = x;
  y_ = y;
  width_ = width;
  height_ = height;
  brush_ = brush;
  color_ = color;
}

Shape::~Shape() {}

void Shape::draw() {
  for (int x = x_; x < x_ + width_; x++) {
    for (int y = y_; y < y_ + height_; y++) {
      if (inside(x, y)) {
        draw_at(x, y, brush_, color_);
      }
    }
  }
}

Rectangle::Rectangle(int x, int y, int width, int height, char brush, int color)
  : Shape(x, y, width, height, brush, color) {}

bool Rectangle::inside(int x, int y) {
  return true;
}

Square::Square(int x, int y, int size, char brush, int color)
  : Rectangle(x, y, size, size, brush, color) {}

Ellipse::Ellipse(int x, int y, int width, int height, char brush, int color)
  : Shape(x, y, width, height, brush, color) {}

bool Ellipse::inside(int x, int y) {
  double rad_x = width_ / 2.0; // radius in x axis is half width
  double rad_y = height_ / 2.0; // radius in y axis is half height

  double dx = x - (x_ + rad_x); // dist in x axis is x - center in x
  double dy = y - (y_ + rad_y); // dist in y axis is y - center in y

  // Use equation of an ellipse to calculate if (dx, dy) inside the bounds
  return (dx * dx) / (rad_x * rad_x) + (dy * dy) / (rad_y * rad_y) <= 1;
}

Circle::Circle(int x, int y, int size, char brush, int color)
  : Ellipse(x, y, size, size, brush, color) {}
