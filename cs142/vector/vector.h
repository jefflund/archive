// Operator overloading is tricky! Here are some good references for remembering
// which method signiture to use:
// http://en.cppreference.com/w/cpp/language/operators
// https://msdn.microsoft.com/en-us/library/5tk49fh2.aspx
// http://courses.cms.caltech.edu/cs11/material/cpp/donnie/cpp-ops.html

#ifndef VECTOR_H
#define VECTOR_H

#include <iostream>
using namespace std;

// Represents a 2-dimensional Euclidean vector.
class Vector {
 private:
  double x_;
  double y_;
 public:
  // Simple constructors
  Vector();
  Vector(double x, double y);

  // Getters
  double x() const;
  double y() const;
  double magnitude() const;
  double direction() const;

  // Allow Vector be shifted into an ostream such as cout!
  // Without this, I can't do things like:
  // cout << Vector(3, 4) << endl;
  // With it, not only can I shift into cout, but I can customize how a Vector
  // should print!
  friend ostream& operator<<(ostream& os, const Vector& v);

  // Equality operators
  bool operator==(const Vector& other) const;
  bool operator!=(const Vector& other) const;

  // Negation
  Vector operator-() const;

  // Vector addition
  Vector& operator+=(const Vector& other);
  Vector& operator-=(const Vector& other);
  const Vector operator+(const Vector& other);
  const Vector operator-(const Vector& other);

  // Vector scaling
  Vector& operator*=(const double& other);
  Vector& operator/=(const double& other);
  const Vector operator*(const double& other);
  const Vector operator/(const double& other);
};

#endif // VECTOR_H
