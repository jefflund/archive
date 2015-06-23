#include <iostream>
using namespace std;

int main() {
  cout << "Hello\nThis is an example of\nescape sequences.\n";
  cout << "\tI can indent lines with escape sequences.\n";
  cout << "I can also print \"quoted\" things with escape sequences\n";
  cout << "I can even print silly stuff like\n\t\t";
  cout << "(\u3065\uFF61\u25D5\u203F\u203F\u25D5\uFF61)\u3065";
  return 0;
}
