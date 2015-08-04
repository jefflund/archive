#ifndef SHAPES_H
#define SHAPES_H

// Abstract base class for drawabled shapes. Stores its own location and a size.
// To draw simply call shape->draw() and the shape will draw itself.
class Shape {
 protected:
  int x_;
  int y_;
  int width_;
  int height_;
  char brush_;
  int color_;
 public:
  // Constructs the shape with the given dimensions and draw properties.
  Shape(int x, int y, int width, int height, char brush, int color);
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
  Rectangle(int x, int y, int width, int height, char brush, int color);
 protected:
  // Returns true, since the Rectangle fills its entire bounding box.
  virtual bool inside(int x, int y);
};

// Square is a Rectangle with equal side lengths.
class Square : public Rectangle {
 public:
  // Constructs a Square with the given dimensions. Size is both the width and
  // the height of the Square.
  Square(int x, int y, int size, char brush, int color);
};

// Ellipse is a curve on a plane surrounding two focal points.
class Ellipse : public Shape {
 public:
  // Constructs an Ellipse with the given Shape properties.
  Ellipse(int x, int y, int width, int height, char brush, int color);
 protected:
  // Returns true if the point is inside the Ellipse.
  virtual bool inside(int x, int y);
};

// Circle is an Ellipse whose major and minor axes are equal in length.
class Circle : public Ellipse {
 public:
  // Constructs a Circle with the given properties. Size is both the width and
  // height of the Circle.
  Circle(int x, int y, int size, char brush, int color);
};

#endif // SHAPES_H
