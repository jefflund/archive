#include <iostream>
#include "register.h"
using namespace std;

const int OPTION_CASH = 1;
const int OPTION_ITEMS = 2;
const int OPTION_CHECK = 3;
const int OPTION_BUY = 4;
const int OPTION_QUIT = 5;
const int OPTION_INVALID = -1;

int menu() {
  cout << "Choose an option:" << endl;
  cout << "1 - Display cash" << endl;
  cout << "2 - Display items" << endl;
  cout << "3 - Price check" << endl;
  cout << "4 - Make purchase" << endl;
  cout << "5 - Quit" << endl;
  cout << "Make your selection: ";

  int option;
  cin >> option;

  switch (option) {
    case OPTION_CASH:
    case OPTION_ITEMS:
    case OPTION_CHECK:
    case OPTION_BUY:
    case OPTION_QUIT:
      return option;
    default:
      if (cin.fail()) {
        cin.clear();
        cin.ignore(1000, '\n');
      }
      return OPTION_INVALID;
  }
}

int main() {
  Register till;

  while (true) {
    switch (menu()) {
    case OPTION_CASH:
      cout << "Cash: " << till.cash() << endl;
      break;
    case OPTION_ITEMS:
      till.print_items();
      break;
    case OPTION_CHECK:
      till.price_check();
      break;
    case OPTION_BUY:
      till.make_purchase();
      break;
    case OPTION_QUIT:
      cout << "Goodbye!" << endl;
      return 0;
    case OPTION_INVALID:
      cout << "Invalid selection!" << endl;
      break;
    }
  }
}
