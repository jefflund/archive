#include <climits>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <unistd.h>
using namespace std;

const int MICROS_PER_SEC = 1e6;
const int BOARD_COLS = 200;
const int BOARD_ROWS = 50;
const int NUM_GENS = 100;

// init board randomly initializes the board, with each cell being alive with
// the given probability, and the boarder of the board set to dead.
void init_board(bool board[BOARD_COLS][BOARD_ROWS], double alive_prob) {
  // fill the middle of the board, with each cell alive with alive_prob
  for (int x = 1; x < BOARD_COLS - 1; x++) {
    for (int y = 1; y < BOARD_ROWS - 1; y++) {
      board[x][y] = (double)rand() / INT_MAX < alive_prob;
    }
  }

  // fill the board with dead cells
  for (int x = 0; x < BOARD_COLS; x++) {
    board[x][0] = false;
    board[x][BOARD_ROWS - 1] = false;
  }
  for (int y = 0; y < BOARD_ROWS; y++) {
    board[0][y] = false;
    board[BOARD_COLS - 1][y] = false;
  }
}

// sleeps the program for the given number of seconds, including
// fractional seconds.
void delay(double seconds) {
  usleep(seconds * MICROS_PER_SEC);
}

// alive returns true if the cell at x, y will be alive in the next generation.
bool alive(bool board[BOARD_COLS][BOARD_ROWS], int x, int y) {
  // count the number of alive neighbors the cell has
  int neighbors = 0;
  for (int dx = -1; dx <= 1; dx++) {
    for (int dy = -1; dy <= 1; dy++) {
      // if the cell is alive, and its not ourself
      if (board[x+dx][y+dy] && (dx != 0 || dy != 0)) {
        neighbors++;
      }
    }
  }

  // See https://en.wikipedia.org/wiki/Conway's_Game_of_Life for information
  // on these rules.
  if (board[x][y]) {
    return neighbors == 2 || neighbors == 3;
  } else {
    return neighbors == 3;
  }
}

// updates the board state to reflect the next generation.
void update_board(bool board[BOARD_COLS][BOARD_ROWS]) {
  // compute the alive states for the next generation without updating board
  // if we update board in place, then we mix the current and next generation
  // of states in this computation.
  bool scratch[BOARD_COLS][BOARD_ROWS];
  for (int x = 1; x < BOARD_COLS - 1; x++) {
    for (int y = 1; y < BOARD_ROWS - 1; y++) {
      scratch[x][y] = alive(board, x, y);
    }
  }

  // copy scratch into board so board contains the new generation
  for (int x = 1; x < BOARD_COLS - 1; x++) {
    for (int y = 1; y < BOARD_ROWS - 1; y++) {
      board[x][y] = scratch[x][y];
    }
  }
}

// prints out the state of the board
void print_board(bool board[BOARD_COLS][BOARD_ROWS]) {
  cout << "\033[H"; // move the cursor to the top left

  // y on the outer loop, so we print row by row
  for (int y = 0; y < BOARD_ROWS; y++) {
    // print each value of the row
    for (int x = 0; x < BOARD_COLS; x++) {
      cout << (board[x][y] ? "O" : "\u00B7");
    }
    cout << endl; // advance to the next row
  }

  cout << endl;
}

int main() {
  srand(time(NULL));

  bool board[BOARD_COLS][BOARD_ROWS];

  init_board(board, .2);
  print_board(board);

  for (int i = 0; i < NUM_GENS; i++) {
    update_board(board);
    print_board(board);
    delay(.1);
  }

  return 0;
}
