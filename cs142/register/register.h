#ifndef REGISTER_H
#define REGISTER_H

#include <vector>
#include "item.h"
using namespace std;

class Register {
 private:
  double cash_;
  vector<Item> items_;
 public:
  Register();

  double cash();

  void print_items();
  void price_check();

  void make_purchase();
 private:
  int find_item(string prompt);
};

#endif // REGISTER_H
