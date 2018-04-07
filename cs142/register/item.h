#ifndef ITEM_H
#define ITEM_H

#include <string>
using namespace std;

class Item {
 private:
  string name_;
  double price_;
 public:
  Item(string name, double price);

  string name();
  double price();
};

#endif // ITEM_H
