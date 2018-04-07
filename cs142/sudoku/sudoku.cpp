#include <iostream>
#include "sudoku.h"
using namespace std;

Sudoku::Sudoku() {
  for (int y = 0; y < BOARD_SIZE; y++) {
    for (int x = 0; x < BOARD_SIZE; x++) {
      data[y][x] = EMPTY;
    }
  }
}

ostream& operator<<(ostream& os, const Sudoku& s) {
  for (int y = 0; y < Sudoku::BOARD_SIZE; y++) {
    for (int x = 0; x < Sudoku::BOARD_SIZE; x++) {
      if (s.data[y][x] == Sudoku::EMPTY) {
        cout << "-";
      } else {
        cout << s.data[y][x];
      }
      if (x < Sudoku::BOARD_SIZE - 1) {
        cout << " ";
      }
    }
    cout << endl;
  }
  return os;
}

istream& operator>>(istream& is, Sudoku& s) {
  for (int y = 0; y < Sudoku::BOARD_SIZE; y++) {
    for (int x = 0; x < Sudoku::BOARD_SIZE; x++) {
      is >> s.data[y][x];
      if (is.fail()) {
        is.clear();
        is.get();
      }
    }
  }
  return is;
}

bool Sudoku::valid() {
  // Check all cells - do this first so other checks use invalid cell values
  for (int y = 0; y < BOARD_SIZE; y++) {
    for (int x = 0; x < BOARD_SIZE; x++) {
      if (!valid_cell(x, y)) {
        return false;
      }
    }
  }

  // Check all cols and rows
  for (int i = 0; i < BOARD_SIZE; i++) {
    if (!valid_col(i) || !valid_row(i)) {
      return false;
    }
  }

  // Check all panels
  for (int panel_y = 0; panel_y < NUM_PANELS; panel_y++) {
    for (int panel_x = 0; panel_x < NUM_PANELS; panel_x++) {
      if (!valid_panel(panel_x, panel_y)) {
        return false;
      }
    }
  }

  // All checks passed!
  return true;
}

bool Sudoku::valid_cell(int x, int y) {
  int cell = data[y][x];
  return cell == EMPTY || (MIN_VALUE <= cell && cell <= MAX_VALUE);
}

bool Sudoku::valid_col(int x) {
  bool seen[BOARD_SIZE] = {};
  for (int y = 0; y < BOARD_SIZE; y++) {
    int cell = data[y][x];
    if (cell == EMPTY) {
      continue;
    }
    cell -= MIN_VALUE; // offset by MIN_VALUE so cell can index seen

    if (seen[cell]) {
      return false;
    }
    seen[cell] = true;
  }

  return true;
}

bool Sudoku::valid_row(int y) {
  bool seen[BOARD_SIZE] = {};
  for (int x = 0; x < BOARD_SIZE; x++) {
    int cell = data[y][x];
    if (cell == EMPTY) {
      continue;
    }
    cell -= MIN_VALUE; // offset by MIN_VALUE so cell can index seen

    if (seen[cell]) {
      return false;
    }
    seen[cell] = true;
  }

  return true;
}

bool Sudoku::valid_panel(int panel_x, int panel_y) {
  int off_x = panel_x * PANEL_SIZE;
  int off_y = panel_y * PANEL_SIZE;

  bool seen[BOARD_SIZE] = {};
  for (int y = 0; y < PANEL_SIZE; y++) {
    for (int x = 0; x < PANEL_SIZE; x++) {
      int cell = data[y+off_y][x+off_x];
      if (cell == EMPTY) {
        continue;
      }
      cell -= MIN_VALUE; // offset by MIN_VALUE so cell can index seen

      if (seen[cell]) {
        return false;
      }
      seen[cell] = true;
    }
  }

  return true;
}

bool Sudoku::find_empty(int& x, int& y) {
  for (y = 0; y < BOARD_SIZE; y++) {
    for (x = 0; x < BOARD_SIZE; x++) {
      if (data[y][x] == EMPTY) {
        return true;
      }
    }
  }

  return false;
}

bool Sudoku::solve() {
  int x, y;
  if (!find_empty(x, y) && valid()) {
    return true;
  }

  // Try each value in the empty cell
  for (int i = MIN_VALUE; i <= MAX_VALUE; i++) {
    data[y][x] = i;
    if (valid() && solve()) {
      return true;
    }
  }

  // Undo any changes, this solution is invalid
  data[y][x] = EMPTY;
  return false;
}
