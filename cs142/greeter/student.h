#ifndef STUDENT_H
#define STUDENT_H

#include <string>
using namespace std;

class Student {
 private:
  string name;
 public:
  Student(string name);

  void setName(string name);
  string getName();
};

#endif // STUDENT_H
