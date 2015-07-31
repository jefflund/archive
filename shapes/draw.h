#ifndef DRAW_H
#define DRAW_H

#include <iostream>
using namespace std;

void hide_cursor();
void show_cursor();

void clear();

template<typename T> void draw_at(int x, int y, T data) {
  cout << "\033[" << y << ";" << x << "H" << data;
}

#endif // DRAW_H
