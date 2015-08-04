#ifndef SHAPES_H
#define SHAPES_H

class Shape {
 protected:
  int x_;
  int y_;
  int width_;
  int height_;
  char brush_;
  int color_;
 public:
  Shape(int x, int y, int width, int height, char brush, int color);
  virtual ~Shape();
  void draw();
 protected:
  virtual bool inside(int x, int y) = 0;
};

class Rectangle : public Shape {
 public:
  Rectangle(int x, int y, int width, int height, char brush, int color);
 protected:
  virtual bool inside(int x, int y);
};

class Square : public Rectangle {
 public:
  Square(int x, int y, int size, char brush, int color);
};

class Ellipse : public Shape {
 public:
  Ellipse(int x, int y, int width, int height, char brush, int color);
 protected:
  virtual bool inside(int x, int y);
};

class Circle : public Ellipse {
 public:
  Circle(int x, int y, int size, char brush, int color);
};

#endif // SHAPES_H
