#include <iostream>
#include <fstream>
using namespace std;

string read_str(istream& input) {
  string s;
  input >> s;
  return s;
}

int main() {
  // Reads and echos a string from cin:
  cout << "Please enter a string: ";
  cout << "You entered: " << read_str(cin) << endl;

  // Reads and echos a string from a file
  ifstream file;
  file.open("input.cpp");
  cout << "Read from a file: " << read_str(file) << endl;

  return 0;
}
