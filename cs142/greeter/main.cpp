#include <vector>
#include "greeter.h"
#include "student.h"
using namespace std;

int main() {
  Greeter greeter("Hello");
  Student student("World");
  greeter.greet(student);
}
