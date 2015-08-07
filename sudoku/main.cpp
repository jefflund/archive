#include <iostream>
#include <string>
#include <fstream>
#include "sudoku.h"
using namespace std;

int main() {
  Sudoku s;

  string input_file;
  cout << "Enter test file: ";
  cin >> input_file;

  ifstream input(input_file.c_str());
  input >> s;

  if (!s.valid()) {
    cout << "Invalid puzzle!" << endl;
    return 1;
  }

  cout << "Puzzle: " << endl << s << endl;

  cout << "Solution:" << endl;
  s.solve();
  cout << s << endl;

  return 0;
}
