#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>
#include <climits>
#include <cmath>
using namespace std;

const int OPTION_ILLEGAL = -1;
const int OPTION_ROLL = 1;
const int OPTION_STATS = 2;
const int OPTION_QUIT = 3;

// rootMenu displays the menu to the user, and returns the option they selected
int rootMenu() {
  // display the root menu
  cout << "Root Menu:" << endl;
  cout << "1 - roll some dice" << endl;
  cout << "2 - get stats on dice" << endl;
  cout << "3 - quit" << endl;
  cout << "Select an option: ";

  // retrieve the user input
  int option;
  cin >> option;

  // return valid options, or indicate menu failure
  switch (option) {
    case OPTION_ROLL:
    case OPTION_STATS:
    case OPTION_QUIT:
      return option;
    default:
      // clean up cin so the next menu doesn't fail too
      cin.clear();
      cin.ignore(1000, '\n');

      return OPTION_ILLEGAL;
  }
}

// getUserRollXdY prompts to the user to enter a dice roll in xdy format and
// places the x and y into the output references. It returns true upon success
// and false upon failure.
bool getUserRollXdY(int& outx, int& outy) {
  // Get the user input as a string so we can parse the xdy notation
  cout << "Enter your roll in xdy notation: ";
  string input;
  cin >> input;

  // split rollXdY at the character d to find the x and y parts of the roll
  int split = input.find("d");
  if (split < 0) {
    return false; // indicates failure in the input
  }
  string xpart = input.substr(0, split);
  string ypart = input.substr(split + 1); // + 1 to skip the 'd'

  // ensure that the xpart is valid (contains only digits)
  for (unsigned int i = 0; i < xpart.length(); i++) {
    if (!isdigit(xpart[i])) {
      return false; // indicates failure in the input
    }
  }
  // ensure that the ypart is valid (contains only digits)
  for (unsigned int i = 0; i < ypart.length(); i++) {
    if (!isdigit(ypart[i])) {
      return false; // indicates failure in the input
    }
  }

  // parse the x and y and store them in the output int references
  outx = atoi(xpart.c_str());
  outy = atoi(ypart.c_str());

  // indicates successful parsing
  return true;
}

// getUserNumRolls prompts the user to specify the number of rolls they want and 
// places the result in the output reference. It returns true upon success and 
// false upon failure.
bool getUserNumRolls(int& outNum) {
  cout << "Enter the number of dice rolls: ";
  cin >> outNum;

  // if outNum is invalid, including 0 for alphabetic input, clear cin and
  // return false to indicate an input error
  if (outNum <= 0) {
    cin.clear();
    cin.ignore(1000, '\n');
    return false;
  }

  // indicates successful input
  return true;
}

// simulateRoll makes an xdy dice roll, where x die with y sides are used.
int simulateRoll(int x, int y) {
    int roll = 0;
    for (int i = 0; i < x; i++) {
      roll += rand() % y + 1; // add 1 so range is [1, y], not [0, y - 1]
    }
    return roll;
}

// performUserRoll the user to enter in an xdy roll, and displays the results
void performUserRoll() {
  int x, y;
  if (!getUserRollXdY(x, y)) {
    cout << "Invalid dice format!" << endl << endl;
  } else {
    cout << "Result: " << simulateRoll(x, y) << endl << endl;
  }
}

// getRollStats prompts the user to enter an xdy roll and the number of times
// to simulate that roll, then displays stats about the results of the rolls
void getRollStats() {
  int x, y;
  if (!getUserRollXdY(x, y)) {
    cout << "Invalid dice format!" << endl << endl;
    return;
  }
  int numRolls;
  if (!getUserNumRolls(numRolls)) {
    cout << "Invalid number of rolls" << endl << endl;
    return;
  }

  // we need these stats for the output
  double runningMean = 0;
  double runningVariance = 0;
  int minRoll = INT_MAX;
  int maxRoll = INT_MIN;

  // simulate all of the rolls while accumulating the stats
  for (int k = 1; k <= numRolls; k++) {
    int roll = simulateRoll(x, y);

    // This update allows us to compute the mean and variance in place. See
    // https://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods
    double newMean = runningMean + (roll - runningMean) / k;
    runningVariance += (roll - runningMean) * (roll - newMean);
    runningMean = newMean;

    // Update the min and max rolls
    minRoll = minRoll < roll ? minRoll : roll;
    maxRoll = maxRoll > roll ? maxRoll : roll;
  }

  // Output statistics
  cout << "Mean: " << runningMean << endl;
  cout << "Deviation : " << sqrt(runningVariance / (numRolls - 1)) << endl;
  cout << "Max: " << maxRoll << endl;
  cout << "Min: " << minRoll << endl << endl;
}

int main() {
  srand(time(NULL));

  while (true) {
    switch (rootMenu()) {
      case OPTION_ROLL:
        performUserRoll();
        break;
      case OPTION_STATS:
        getRollStats();
        break;
      case OPTION_QUIT:
        cout << "Goodbye!" << endl;
        return 0;
      default:
        cout << "Illegal option!" << endl << endl;
    }
  }
}
