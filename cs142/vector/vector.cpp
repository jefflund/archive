#include <cmath>
#include "vector.h"
using namespace std;

Vector::Vector() {
  x_ = 0;
  y_ = 0;
}

Vector::Vector(double x, double y) {
  x_ = x;
  y_ = y;
}

double Vector::x() const {
  return x_;
}

double Vector::y() const {
  return y_;
}

double Vector::magnitude() const {
  return sqrt(x_ * x_ + y_ * y_);
}

double Vector::direction() const {
  return atan2(y_, x_);
}

ostream& operator<<(ostream& os, const Vector& v) {
  os << "<" << v.x() << ", " << v.y() << ">";
  return os;
}

bool Vector::operator==(const Vector& other) const {
  return x_ == other.x_ && y_ == other.y_;
}

bool Vector::operator!=(const Vector& other) const {
  return !(*this == other);
}

Vector Vector::operator-() const {
  return Vector(-x_, -y_);
}

Vector& Vector::operator+=(const Vector& other) {
  x_ += other.x_;
  y_ += other.y_;
  return *this;
}

Vector& Vector::operator-=(const Vector& other) {
  return (*this += -other);
}

const Vector Vector::operator+(const Vector& other) {
  return Vector(*this) += other;
}

const Vector Vector::operator-(const Vector& other) {
  return Vector(*this) -= other;
}

Vector& Vector::operator*=(const double& other) {
  x_ *= other;
  y_ *= other;
  return *this;
}

Vector& Vector::operator/=(const double& other) {
  x_ /= other;
  y_ /= other;
  return *this;
}

const Vector Vector::operator*(const double& other) {
  return Vector(*this) *= other;
}

const Vector Vector::operator/(const double& other) {
  return Vector(*this) /= other;
}
