#include <vector>
#include "greeter.h"
#include "student.h"
using namespace std;

int main() {
  vector<Student*> v;
  v.push_back(new Student("Harry"));
  v.push_back(new Student("Karin"));
  v.push_back(new Student("Molly"));
  v.push_back(new Student("Michael"));

  Greeter greeter;

  for (unsigned int i = 0; i < v.size(); i++) {
    greeter.greet(v[i]);
  }

  greeter.setGreeting("Oi");

  for (unsigned int i = 0; i < v.size(); i++) {
    greeter.greet(v[i]);
  }
}
