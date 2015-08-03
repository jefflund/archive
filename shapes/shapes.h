#ifndef SHAPES_H
#define SHAPES_H

class Shape {
 protected:
  int x_;
  int y_;
  int size_;
  char brush_;
  int color_;
 public:
  Shape(int x, int y, int size, char brush, int color);
  virtual ~Shape();
  virtual void draw() = 0;
};

class Square : public Shape {
 public:
  Square(int x, int y, int size, char brush, int color);
  virtual void draw();
};

class Rectangle : public Square {
 private:
  int width_;
 public:
  Rectangle(int x, int y, int height, int width, char brush, int color);
  virtual void draw();
};

class Circle : public Shape {
 public:
  Circle(int x, int y, int size, char brush, int color);
  virtual void draw();
 private:
  bool inside_circle(int x, int y);
};

#endif // SHAPES_H
