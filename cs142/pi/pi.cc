#include <iostream>
#include <cstdlib>
#include <climits>
using namespace std;

const int N = 1000000;

int main() {
  srand(time(NULL));

  int inside = 0;
  for (int i = 0; i < N; i++) {
    double x = (double)rand() / INT_MAX;
    double y = (double)rand() / INT_MAX;
    if (x * x + y * y < 1) {
      inside++;
    }
  }
  double pi = 4.0 * inside / N;

  cout << pi << endl;
  return 0;
}
