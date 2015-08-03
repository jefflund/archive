#include <iostream>
#include <cstdlib>
#include "draw.h"
using namespace std;

const int Aqua = 51;
const int Beige = 230;
const int Black = 16;
const int Blue = 21;
const int Brown = 130;
const int Chartreuse = 118;
const int Coral = 209;
const int Crimson = 161;
const int Cyan = 51;
const int Darkblue = 18;
const int Darkgray = 145;
const int Darkgreen = 22;
const int Darkmagenta = 90;
const int Darkorange = 208;
const int Darkred = 88;
const int Darkturquoise = 44;
const int Darkviolet = 92;
const int Darkpink = 198;
const int Dimgray = 59;
const int Forestgreen = 28;
const int Fuchsia = 201;
const int Gold = 220;
const int Gray = 244;
const int Green = 28;
const int Hotpink = 205;
const int Indigo = 54;
const int Lavender = 189;
const int Lawngreen = 82;
const int Lightblue = 152;
const int Lightcoral = 210;
const int Lightcyan = 195;
const int Lightgreen = 120;
const int Lightgray = 252;
const int Lightpink = 217;
const int Lightsalmon = 216;
const int Lime = 46;
const int Limegreen = 77;
const int Magenta = 201;
const int Mediumblue = 20;
const int Mediumorchid = 134;
const int Midnightblue = 17;
const int Olive = 100;
const int Oliverab = 64;
const int Orange = 214;
const int Orangered = 202;
const int Orchid = 170;
const int Peachpuff = 223;
const int Pink = 218;
const int Plum = 182;
const int Purple = 90;
const int Red = 196;
const int Salmon = 209;
const int Seagreen = 29;
const int Silver = 145;
const int Skyblue = 116;
const int Slategray = 66;
const int Smoke = 240;
const int Tan = 180;
const int Teal = 30;
const int Turquoise = 80;
const int Violet = 213;
const int White = 231;
const int Whitesmoke = 255;
const int Yellow = 226;

void hide_cursor() {
  system("tput civis");
}

void show_cursor() {
  system("tput cnorm");
}

void clear() {
  cout << "\033[2J";
}

