#ifndef DRAW_H
#define DRAW_H

#include <iostream>
using namespace std;

extern const int Aqua;
extern const int Beige;
extern const int Black;
extern const int Blue;
extern const int Brown;
extern const int Chartreuse;
extern const int Coral;
extern const int Crimson;
extern const int Cyan;
extern const int Darkblue;
extern const int Darkgray;
extern const int Darkgreen;
extern const int Darkmagenta;
extern const int Darkorange;
extern const int Darkred;
extern const int Darkturquoise;
extern const int Darkviolet;
extern const int Darkpink;
extern const int Dimgray;
extern const int Forestgreen;
extern const int Fuchsia;
extern const int Gold;
extern const int Gray;
extern const int Green;
extern const int Hotpink;
extern const int Indigo;
extern const int Lavender;
extern const int Lawngreen;
extern const int Lightblue;
extern const int Lightcoral;
extern const int Lightcyan;
extern const int Lightgreen;
extern const int Lightgray;
extern const int Lightpink;
extern const int Lightsalmon;
extern const int Lime;
extern const int Limegreen;
extern const int Magenta;
extern const int Mediumblue;
extern const int Mediumorchid;
extern const int Midnightblue;
extern const int Olive;
extern const int Oliverab;
extern const int Orange;
extern const int Orangered;
extern const int Orchid;
extern const int Peachpuff;
extern const int Pink;
extern const int Plum;
extern const int Purple;
extern const int Red;
extern const int Salmon;
extern const int Seagreen;
extern const int Silver;
extern const int Skyblue;
extern const int Slategray;
extern const int Smoke;
extern const int Tan;
extern const int Teal;
extern const int Turquoise;
extern const int Violet;
extern const int White;
extern const int Whitesmoke;
extern const int Yellow;

void hide_cursor();
void show_cursor();

void clear();

template<typename T> void draw_at(int x, int y, T data) {
  cout << "\033[" << y << ";" << x << "H";
  cout << data;
}

template<typename T> void draw_at(int x, int y, T data, int color) {
  cout << "\033[" << y << ";" << x << "H";
  cout << "\033[38;5;" << color << "m";
  cout << data;
  cout << "\033[0m";
}

#endif // DRAW_H
