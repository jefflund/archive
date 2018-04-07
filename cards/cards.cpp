#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
#include <ctime>
using namespace std;

const unsigned int NUM_FACES = 13;
const string FACES[NUM_FACES] = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
const unsigned int NUM_SUITS = 4;
const string SUITS[NUM_SUITS] = {"\u2660", "\u2665", "\u2666", "\u2663"};

// returns a vector with all the face cards in sorted order
vector<string> sortedDeck() {
  vector<string> deck;
  for(unsigned int suit = 0; suit < NUM_SUITS; suit++) {
    for (unsigned int face = 0; face < NUM_FACES; face++) {
      deck.push_back(FACES[face] + SUITS[suit]);
    }
  }
  return deck;
}

// swap exchanges the values of the ith and jth values of the vector v.
// swap uses a vector reference, so the change is reflected in the vector being
// passed in. no bounds checking is performed.
void swap(vector<string>& v, int i, int j) {
  string tmp = v[i];
  v[i] = v[j];
  v[j] = tmp;
}

// returns a random number between min (inclusive) and max (exclusive).
unsigned int randrange(int min, int max) {
  return rand() % (max - min) + min;
}

// shuffle randomly permutes the given vector. the vector is a reference, so the
// change is reflected in the vector being passed in. the algorithm is taken
// from the modern version of the fisher-yates algorithm as given by wikipedia.
void shuffle(vector<string>& v) {
  for (unsigned int i = 0; i < v.size() - 2; i++) {
    unsigned int j = randrange(i, v.size());
    swap(v, i, j);
  }
}

int main() {
  srand(time(NULL));
  vector<string> deck = sortedDeck();
  shuffle(deck);
  for (unsigned int i = 0; i < deck.size(); i++) {
    cout << deck[i] << endl;
  }
}
