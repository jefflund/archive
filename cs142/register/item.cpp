#include "item.h"
using namespace std;

Item::Item(string name, double price) {
  name_ = name;
  price_ = price;
}

string Item::name() {
  return name_;
}

double Item::price() {
  return price_;
}
