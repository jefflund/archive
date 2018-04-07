#include <iostream>
#include "register.h"
using namespace std;

const double INITIAL_CASH = 100;
const int NOT_FOUND = -1;

Register::Register() {
  cash_ = INITIAL_CASH;

  items_.push_back(Item("milk", 3.14));
  items_.push_back(Item("cheese", 2.31));
  items_.push_back(Item("bread", 1.40));
  items_.push_back(Item("fritos", 4.51));
}

double Register::cash() {
  return cash_;
}

void Register::print_items() {
  cout << "Items:" << endl;
  for (unsigned int i = 0; i < items_.size(); i++) {
    cout << items_[i].name() << endl;
  }
}

int find(vector<Item> items, string name) {
  for (unsigned int i = 0; i < items.size(); i++) {
    if (items[i].name() == name) {
      return i;
    }
  }
  return NOT_FOUND;
}

int Register::find_item(string prompt) {
  cout << prompt;
  string name;
  cin >> name;
  return find(items_, name);
}

void Register::price_check() {
  int index = find_item("Check the price of what item? ");

  if (index == NOT_FOUND) {
    cout << "Item not found!" << endl;
  } else {
    cout << "Price: " << items_[index].price() << endl;
  }
}

void Register::make_purchase() {
  int index = find_item("Purchace what item? ");

  if (index == NOT_FOUND) {
    cout << "Could not complete purchace!" << endl;
  } else {
    cash_ += items_[index].price();
    cout << "Purchace of " << items_[index].name() << " complete!" << endl;
  }
}
