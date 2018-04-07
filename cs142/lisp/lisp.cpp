// Note that this is a very silly example of pointers. You will not be doing
// anything like this until CS 235. That said, this is an example of something
// you could absolutley not do with out something like pointers.

#include <iostream>
using namespace std;

struct element {
  int value;
  element* next; // this *MUST* be an element pointer, or else element would take infinite space to store!
};

// creates a new element. the name is historical to lisp...dont ask.
element* cons(int value, element* next) {
  element* e = new element;
  (*e).value = value; // alternatively: e->value = value;
  (*e).next = next;
  return e;
}

// prints out a list of elements, one value per line.
void print(element* e) {
  while (e != NULL) {
    cout << (*e).value << endl;
    e = (*e).next;
  }
}

// deletes an entire list using recursion.
void deleteList(element* e) {
  if (e != NULL) {
    deleteList((*e).next);
    delete e;
  }
}

int main() {
  element* list1 = cons(1, cons(2, cons(3, cons(4, NULL))));
  element* list2 = cons(42, list1);

  cout << "List 1" << endl;
  print(list1);

  cout << "List 2" << endl;
  print(list2);

  deleteList(list2);
  // dont call deleteList(list1), as this would double delete list1 elements!
}
