#ifndef SUDOKU_H
#define SUDOKU_H

#include <iostream>
using namespace std;

class Sudoku {
 private:
  static const int BOARD_SIZE = 9;
  static const int PANEL_SIZE = 3;
  static const int NUM_PANELS = 3;
  static const int EMPTY = 0;
  static const int MIN_VALUE = 1;
  static const int MAX_VALUE = 9;

  int data[BOARD_SIZE][BOARD_SIZE];
 public:
  Sudoku();

  friend ostream& operator<<(ostream& os, const Sudoku& s);
  friend istream& operator>>(istream& is, Sudoku& s);

  bool valid();

  bool solve();

 private:
  bool valid_cell(int x, int y);
  bool valid_col(int x);
  bool valid_row(int y);
  bool valid_panel(int panel_x, int panel_y);

  bool find_empty(int& x, int& y);
};

#endif // SUDOKU_H
