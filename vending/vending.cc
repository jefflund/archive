#include <iostream>
#include <iomanip>
using namespace std;

const int VALUE_OF_QUARTER_IN_PENNIES = 25;
const int VALUE_OF_DIMES_IN_PENNIES = 10;
const int VALUE_OF_NICKLES_IN_PENNIES = 5;
const int PENNIES_IN_DOLLAR = 100;

int main() {
  cout << fixed << setprecision(2);

  // Prompt user for amount paid
  cout << "Enter the amount paid: ";
  double amount_paid;
  cin >> amount_paid;

  // Prompt user for price of the item
  cout << "Enter the price of the item: ";
  double item_price;
  cin >> item_price;

  cout << "Enter the tax rate on the item: ";
  int tax_rate;
  cin >> tax_rate;

  // Compute and print item_price include tax
  item_price = item_price * (1 + tax_rate / 100.0);
  cout << "Price of the item with tax: " << item_price << endl;

  // Compute and print change, while converting change to pennies;
  double change = amount_paid - item_price;
  cout << "Change recieved: " << change << endl;
  double change_in_pennies = change * PENNIES_IN_DOLLAR;
  cout << setprecision(100);
  cout << change_in_pennies << endl;

  // Compute the number of quarters
  int quarters = change_in_pennies / VALUE_OF_QUARTER_IN_PENNIES;
  cout << "Quarters: " << quarters << endl;

  // Update the remaining change after dispension quarters
  if (quarters != 0) {
    change_in_pennies = change_in_pennies - (quarters * VALUE_OF_QUARTER_IN_PENNIES);
    //change_in_pennies = change_in_pennies % VALUE_OF_QUARTER_IN_PENNIES;
  }
  cout << change_in_pennies << endl;

  //int dimes = change_in_pennies / VALUE_OF_DIMES_IN_PENNIES;
  //cout << "Dimes: " << dimes << endl;

  //if (dimes != 0) {
    //change_in_pennies = change_in_pennies % dimes;
  //}
  //cout << change_in_pennies;

  return 0;
}
