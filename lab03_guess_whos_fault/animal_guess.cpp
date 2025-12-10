#include <iostream>
#include <cstdlib>
#include <ctime>
#include <limits>
#include <string>
#include <sstream>

class AnimalUtil {
public:
    enum Animal { DOG = 1, CAT, BIRD, FISH };

    static const char* toStr(Animal a) {

        switch (a) {
            case DOG:  return "Dog";
            case CAT:  return "Cat";
            case BIRD: return "Bird";
            case FISH: return "Fish";
            default:   return "Unknown";
        }
    }
};

const std::string staticWelcomeMessage = "Welcome to the Animal Guesser!";

int main() {
    std::srand(static_cast<unsigned>(std::time(nullptr)));

    std::cout << staticWelcomeMessage << "\n";

    std::cout << "Guess the Animal! (1: Dog, 2: Cat, 3: Bird, 4: Fish)\n";
    std::cout << "Enter 0 to quit.\n";

    AnimalUtil::Animal* mysteryAnimal = nullptr;
    // std::cout << "The animal is initialized to: " << AnimalUtil::toStr(*mysteryAnimal) << "\n";
    // std::cout << "The animal should initally be nothing: " << AnimalUtil::toStr(*mysteryAnimal) << "\n";
    
    // Error #3 - Figure it out.
    while (true) {
        mysteryAnimal =
            new AnimalUtil::Animal(static_cast<AnimalUtil::Animal>(1 + std::rand() % 4));

        std::string line;
        int guess;
        
        std::cout << "\nYour guess: ";
        
        // Read the line
        std::getline(std::cin, line);
        
        // Check for blank entry
        if (line.empty()) {
            std::cin.clear();
            std::cout << "Invalid input; try again.\n";
            delete mysteryAnimal;
            continue;
        }

        // If not an empty string, convert the input to a number
        std::stringstream ss(line);

        // Verify that the conversion is successful without extra characters
        // This check also assigns the conversion to our guess, allowing for
        // a bounds check immediately afterward
        if (!(ss >> guess && ss.eof()) | (guess < 0) | (guess > 4)) {
            std::cin.clear();
            std::cout << "Invalid input; try again.\n";
            delete mysteryAnimal;
            continue;
        }
        if (guess == 0) {
            std::cout << "Bye!\n";
            break;
        }

        if (*mysteryAnimal == static_cast<AnimalUtil::Animal>(guess)) {
            std::cout << "Correct! It was " << AnimalUtil::toStr(*mysteryAnimal) << "\n";
            delete mysteryAnimal;
        } else {
            std::cout << "Wrong! It was " << AnimalUtil::toStr(*mysteryAnimal) << "\n";
            delete mysteryAnimal;
        }

    }
    delete mysteryAnimal; 
    return 0;
}
