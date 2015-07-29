#ifndef GREETER_H
#define GREETER_H

#include <string>
#include "student.h"
using namespace std;

class Greeter {
 private:
  string greeting;

 public:
  Greeter();
  Greeter(string greeting);

  void greet(Student student);
  void setGreeting(string greeting);
};

#endif // GREETER_H
