#include <iostream>
using namespace std;

class Foo {
 public:
  Foo();
  virtual ~Foo();
};

Foo::Foo() {
  cout << "constructed" << endl;
}

Foo::~Foo() {
  cout << "delorted" << endl;
}

void foobius() {
  Foo foo;
}

int main() {
  foobius();
  Foo* foo = new Foo();
  delete foo;

  return 0;
}
