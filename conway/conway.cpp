#include <climits>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <unistd.h>
using namespace std;

const int MICROS_PER_SEC = 1e6;
const int BOARD_COLS = 100;
const int BOARD_ROWS = 30;
const int NUM_GENS = 100;

// init board randomly initializes the board, with each cell being alive with
// the given probability, and the boarder of the board set to dead.
void init_board(bool board[BOARD_COLS][BOARD_ROWS], double alive_prob) {
  // fill the middle of the board, with each cell alive with alive_prob
  for (int x = 0; x < BOARD_COLS; x++) {
    for (int y = 0; y < BOARD_ROWS; y++) {
      board[x][y] = (double)rand() / INT_MAX < alive_prob;
    }
  }
}

// sleeps the program for the given number of seconds, including
// fractional seconds.
void delay(double seconds) {
  usleep(seconds * MICROS_PER_SEC);
}

// computes x modulo y (not the same as remainder)
int mod(int x, int y) {
  int z = x % y;
  if (z < 0) {
    z += y;
  }
  return z;
}

// computes the number of living neighbors a cell has
int alive_neighbors(bool board[BOARD_COLS][BOARD_ROWS], int x, int y) {
  int neighbors = 0;
  // loop over every cell with in manhattan distance of 1.
  for (int dx = -1; dx <= 1; dx++) {
    for (int dy = -1; dy <= 1; dy++) {
      // we aren't our own neighbor
      if (dx == 0 && dy == 0) {
        continue;
      }

      // compute the offsets, with wrapping around the edges
      int adjx = mod(x + dx, BOARD_COLS);
      int adjy = mod(y + dy, BOARD_ROWS);

      // check if the adjacent cell is alive
      if (board[adjx][adjy]) {
        neighbors++;
      }
    }
  }

  return neighbors;
}

// alive returns true if the cell at x, y will be alive in the next generation.
bool alive(bool board[BOARD_COLS][BOARD_ROWS], int x, int y) {
  // See https://en.wikipedia.org/wiki/Conway's_Game_of_Life for information
  // on these rules.
  int neighbors = alive_neighbors(board, x, y);
  if (board[x][y]) {
    // if already alive, need 2 or 3 neighbors to avoid under/over population
    return neighbors == 2 || neighbors == 3;
  } else {
    // if dead, spawn if exactly 3 neighbors
    return neighbors == 3;
  }
}

// updates the board state to reflect the next generation.
void update_board(bool board[BOARD_COLS][BOARD_ROWS]) {
  // compute the alive states for the next generation without updating board
  // if we update board in place, then we mix the current and next generation
  // of states in this computation.
  bool scratch[BOARD_COLS][BOARD_ROWS];
  for (int x = 0; x < BOARD_COLS; x++) {
    for (int y = 0; y < BOARD_ROWS; y++) {
      scratch[x][y] = alive(board, x, y);
    }
  }

  // copy scratch into board so board contains the new generation
  for (int x = 0; x < BOARD_COLS; x++) {
    for (int y = 0; y < BOARD_ROWS; y++) {
      board[x][y] = scratch[x][y];
    }
  }
}

// prints out the state of the board
void print_board(bool board[BOARD_COLS][BOARD_ROWS]) {
  cout << "\033[H"; // move the cursor to the top left

  // y on the outer loop, so we print row by row, not by column
  for (int y = 0; y < BOARD_ROWS; y++) {
    // print each value of the row
    for (int x = 0; x < BOARD_COLS; x++) {
      cout << (board[x][y] ? "\u25CF" : "\u00B7");
    }
    cout << endl; // advance to the next row
  }
}

int main() {
  srand(time(NULL));

  bool board[BOARD_COLS][BOARD_ROWS];

  init_board(board, .2);
  print_board(board);

  while (true) {
    update_board(board);
    print_board(board);
    delay(.1);
  }

  return 0;
}
