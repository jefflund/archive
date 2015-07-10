#include <iostream>
#include <cstdlib>
#include <ctime>
#include <string>
#include <climits>
#include <cmath>
using namespace std;

const int OPTION_ROLL = 1;
const int OPTION_STATS = 2;
const int OPTION_QUIT = 3;

int main() {
  srand(time(NULL));

  while (true) {
    // Allow user to select an option from the root menu
    cout << "Root Menu:" << endl;
    cout << "1 - roll some dice" << endl;
    cout << "2 - get stats on dice" << endl;
    cout << "3 - quit" << endl;
    cout << "Select an option: ";
    int option;
    cin >> option;

    // Inform the user if the option is out of bounds
    if (option < OPTION_ROLL || option > OPTION_QUIT) {
      cout << "Illegal option!" << endl << endl;
    }

    // User wants to make a single dice roll
    if (option == OPTION_ROLL) {
      // Get the user input as a string so we can parse the xdy notation
      cout << "Enter your roll in xdy notation: ";
      string input;
      cin >> input;

      // find the x and y in the xdy notation
      // if the d is missing, then the notation was incorrect
      int split = input.find("d");
      if (split < 0) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }
      string xpart = input.substr(0, split);
      string ypart = input.substr(split + 1);

      // make sure that the xpart is a non-negative integer
      bool valid = true;
      for (unsigned int i = 0; i < xpart.length(); i++) {
        if (!isdigit(xpart[i])) {
          valid = false;
        }
      }
      // if the xpart is invalid, continue so we get back to the root menu
      if (!valid) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // make sure that the ypart is a non-negative integer
      for (unsigned int i = 0; i < ypart.length(); i++) {
        if (!isdigit(ypart[i])) {
          valid = false;
        }
      }
      // if the ypart is invalid, continue so we get back to the root menu
      if (!valid) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // convert the xpart and ypart to ints, and verify the range
      int x = atoi(xpart.c_str());
      int y = atoi(ypart.c_str());
      if (x < 0 || y < 0) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // make the dice roll
      // we roll a y-sided die x times
      int roll = 0;
      for (int i = 0; i < x; i++) {
        roll += rand() % y + 1; // add 1 so range is [1, y], not [0, y - 1]
      }

      // output result to the user
      cout << "Result: " << roll << endl << endl;
    }

    // User wants to output stats about a dice roll
    if (option == OPTION_STATS) {
      // Get the user input as a string so we can parse the xdy notation
      cout << "Enter your roll in xdy notation: ";
      string input;
      cin >> input;

      // find the x and y in the xdy notation
      // if the d is missing, then the notation was incorrect
      int split = input.find("d");
      if (split < 0) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }
      string xpart = input.substr(0, split);
      string ypart = input.substr(split + 1);

      // make sure that the xpart is a non-negative integer
      bool valid = true;
      for (unsigned int i = 0; i < xpart.length(); i++) {
        if (!isdigit(xpart[i])) {
          valid = false;
        }
      }
      // if the xpart is invalid, continue so we get back to the root menu
      if (!valid) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // make sure that the ypart is a non-negative integer
      for (unsigned int i = 0; i < ypart.length(); i++) {
        if (!isdigit(ypart[i])) {
          valid = false;
        }
      }
      // if the ypart is invalid, continue so we get back to the root menu
      if (!valid) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // convert the xpart and ypart to ints, and verify the range
      int x = atoi(xpart.c_str());
      int y = atoi(ypart.c_str());
      if (x < 0 || y < 0) {
        cout << "Invalid dice format!" << endl << endl;
        continue;
      }

      // allow user to specify how many times the user should make the dice roll
      cout << "Enter the number of dice rolls: ";
      int rolls;
      cin >> rolls;

      // make sure rolls was correctly entered
      if (rolls <= 0) {
        cout << "Invalid number of rolls" << endl << endl;
        continue;
      }

      // we need these stats for the output
      int minRoll = INT_MAX;
      int maxRoll = INT_MIN;
      double runningMean = 0;
      double runningVariance = 0;

      for (int k = 1; k <= rolls; k++) {
        // make the dice roll - we roll a y-sided die x times
        int roll = 0;
        for (int i = 0; i < x; i++) {
          roll += rand() % y + 1; // add 1 so range is [1, y], not [0, y - 1]
        }

        // update min based on this roll
        if (roll < minRoll) {
          minRoll = roll;
        }

        // update max based on this roll
        if (roll > maxRoll) {
          maxRoll = roll;
        }

        // This update allows us to compute the mean and variance in places. See
        // https://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods
        double newMean = runningMean + (roll - runningMean) / k;
        runningVariance += (roll - runningMean) * (roll - newMean);
        runningMean = newMean;
      }

      // Output statistics
      cout << "Mean: " << runningMean << endl;
      cout << "Deviation : " << sqrt(runningVariance / (rolls - 1)) << endl;
      cout << "Max: " << maxRoll << endl;
      cout << "Min: " << minRoll << endl << endl;
    }

    // User quits, return 0 to indicate success
    if (option == 3) {
      cout << "Goodbye!" << endl;
      return 0;
    }
  }
}
