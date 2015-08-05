#include "draw.h"
#include "shapes.h"

// We have no dynamically allocated data, so destructor is a no-op.
Brush::~Brush() {}

// Sets all the fields with the given parameters.
SimpleBrush::SimpleBrush(char brush, int color) {
  brush_ = brush;
  color_ = color;
}

// Paint at the given location using the brush and color.
void SimpleBrush::paint(int x, int y) {
  draw_at(x, y, brush_, color_);
}

// Sets all the fields from the given parameters.
Shape::Shape(int x, int y, int width, int height, Brush* brush) {
  x_ = x;
  y_ = y;
  width_ = width;
  height_ = height;
  brush_ = brush;
}

// We have no dynamically allocated data, so destructor is a no-op.
Shape::~Shape() {}

// Loop over the entire bounding box, and draw each tile if inside the Shape.
void Shape::draw() {
  for (int x = x_; x < x_ + width_; x++) {
    for (int y = y_; y < y_ + height_; y++) {
      if (inside(x, y)) {
        brush_->paint(x, y);
      }
    }
  }
}

// Pass all the parameters to the Shape constructor.
Rectangle::Rectangle(int x, int y, int width, int height, Brush* brush)
  : Shape(x, y, width, height, brush) {}

bool Rectangle::inside(int x, int y) {
  return true;
}

// Pass all the parameters to the Rectangle constructor, with size passed as
// both the width and the height.
Square::Square(int x, int y, int size, Brush* brush)
  : Rectangle(x, y, size, size, brush) {}

// Pass all the parameters to the Shape constructor.
Ellipse::Ellipse(int x, int y, int width, int height, Brush* brush)
  : Shape(x, y, width, height, brush) {}

// Translates the Ellipse to 0,0 by calculating the distance of x, y from the
// center of this Ellipse, and then uses the equation of an ellipse to determine
// if x, y is inside the Ellipse.
bool Ellipse::inside(int x, int y) {
  double rad_x = width_ / 2.0; // radius in x axis is half width
  double rad_y = height_ / 2.0; // radius in y axis is half height

  double dx = x - (x_ + rad_x); // dist in x axis is x - center in x
  double dy = y - (y_ + rad_y); // dist in y axis is y - center in y

  // Use equation of an ellipse to calculate if (dx, dy) inside the bounds of
  // the ellipse centered at (0, 0).
  return (dx * dx) / (rad_x * rad_x) + (dy * dy) / (rad_y * rad_y) <= 1;
}

// Pass all the parameters to the Circle constructor, with size passed as
// both the width and the height.
Circle::Circle(int x, int y, int size, Brush* brush)
  : Ellipse(x, y, size, size, brush) {}
