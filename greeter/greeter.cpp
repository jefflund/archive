#include <iostream>
#include "greeter.h"
using namespace std;

Greeter::Greeter() : greeting("hello") {}

Greeter::Greeter(string greeting) : greeting(greeting) {}

void Greeter::greet(Student* student) {
  cout << greeting << " " << student->getName() << endl;
}

void Greeter::setGreeting(string greeting) {
  this->greeting = greeting;
}
