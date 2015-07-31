#include <iostream>
#include <cstdlib>
using namespace std;

void hide_cursor() {
  system("tput civis");
}

void show_cursor() {
  system("tput cnorm");
}

void clear() {
  cout << "\033[2J";
}

