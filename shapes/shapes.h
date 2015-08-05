#ifndef SHAPES_H
#define SHAPES_H

// Base class for things which know how to paint at location.
class Brush {
 public:
  virtual ~Brush();
  virtual void paint(int x, int y) = 0;
};

// Paints with a single brush type and color.
class SimpleBrush : public Brush {
 private:
  char brush_;
  int color_;
 public:
  SimpleBrush(char brush, int color);
  virtual void paint(int x, int y);
};

// Abstract base class for drawabled shapes. Stores its own location and a size.
// To draw simply call shape->draw() and the shape will draw itself.
// The shape does *not* own the brush, and will not delete it.
class Shape {
 protected:
  int x_;
  int y_;
  int width_;
  int height_;
  Brush* brush_;
 public:
  // Constructs the shape with the given dimensions and draw properties.
  Shape(int x, int y, int width, int height, Brush* brush);
  // Virtual destructor just in case a subclass needs it.
  virtual ~Shape();
  // Draws the shape on the screen.
  void draw();
 protected:
  // Returns true if the given coordinates are inside the shape. Should only be
  // called for coordinates inside the bounding box of the shape.
  virtual bool inside(int x, int y) = 0;
};

// Rectangle is a quadralaterial with 4 right angles.
class Rectangle : public Shape {
 public:
  // Constructs a new Rectangle with the given Shape properties.
  Rectangle(int x, int y, int width, int height, Brush* brush);
 protected:
  // Returns true, since the Rectangle fills its entire bounding box.
  virtual bool inside(int x, int y);
};

// Square is a Rectangle with equal side lengths.
class Square : public Rectangle {
 public:
  // Constructs a Square with the given dimensions. Size is both the width and
  // the height of the Square.
  Square(int x, int y, int size, Brush* brush);
};

// Ellipse is a curve on a plane surrounding two focal points.
class Ellipse : public Shape {
 public:
  // Constructs an Ellipse with the given Shape properties.
  Ellipse(int x, int y, int width, int height, Brush* brush);
 protected:
  // Returns true if the point is inside the Ellipse.
  virtual bool inside(int x, int y);
};

// Circle is an Ellipse whose major and minor axes are equal in length.
class Circle : public Ellipse {
 public:
  // Constructs a Circle with the given properties. Size is both the width and
  // height of the Circle.
  Circle(int x, int y, int size, Brush* brush);
};

#endif // SHAPES_H
