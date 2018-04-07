#include "student.h"
using namespace std;

Student::Student(string name) : name(name) {}

void Student::setName(string name) {
  this->name = name;
}

string Student::getName() {
  return name;
}
