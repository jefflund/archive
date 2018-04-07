#include <iostream>
#include <string>
#include <fstream>
#include "sudoku.h"
using namespace std;

const int SUCCESS = 0;
const int ERROR = 1;

int main() {
  Sudoku s;

  string input_file;
  cout << "Enter test file: ";
  cin >> input_file;

  ifstream input(input_file.c_str());
  input >> s;

  if (!s.valid()) {
    cout << "Invalid puzzle!" << endl;
    return ERROR;
  }

  cout << "Puzzle: " << endl << s << endl;

  if (!s.solve()) {
    cout << "Could not solve puzzle!" << endl;
    return ERROR;
  } else {
    cout << "Solution: " << endl << s << endl;
  }

  return SUCCESS;
}
