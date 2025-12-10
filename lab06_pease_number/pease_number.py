"""
Implement in Python in a pure functional style:
- Only use pure functions
- No loops
- No mutation or side-effects outside of main
Only exception is reading/writing to lookup table.
- All calculations must be memoized
 Only calculate any one value once, ever.
 Find previous calculations in lookup table.
- No AI to generate code.

OBJECTIVE:
1. User prompted to input a Month, Day, and a Year
2. Pease number is calculated and displayed to the screen
3. Go to step 1.

Your Birthday (YB) = [MM DD YYYY]

Fibo Birthday Constant (FBC) = [Fibo(YB[0]) Fibo(YB[1])]

Collatz Fibo-Birthday (CFB) 
                         = [Collatz(FBC[0]) Collatz(FBC[1]) Collatz(YB[2])]

Pease constant = CFB[0] + CFB[1] + CFB[2]

Example Birthday (April 10) YB = [4 10 1982]

Fibo Birthday Constant (FBC) = [Fibo(YB[0]) Fibo(YB[1])] = [3 55] 

Collatz Fibo-Birthday (CFB) = [Collatz(FBC[0]) Collatz(FBC[1])] = [7 112 99]

Pease constant = CFB[0] + CFB[1] + CFB[2] 
                      = 7 + 112 + 99
                      = 218
"""

fibonnaci_table = dict()
collatz_table = dict()


def fibonnaci_sequence(num: int) -> int:
    """
    Using an input number num, returns the nth number in the Fibonnaci sequence.
    First, checks the fibonnaci_table for a previously calculated value.
    If not found, calculates it recursively and stores the result in that table. 
    """

    # lookup first
    if num in fibonnaci_table:
        return fibonnaci_table[num]
    
    #base cases
    if num <= 0:
        result = 0
    elif num == 1:
        result = 1
    else:
        # recursive call
        result = fibonnaci_sequence(num - 1) + fibonnaci_sequence(num - 2)
    
    # save the result
    fibonnaci_table[num] = result
    return result


def collatz_sequence(num: int) -> int:
    """
    Using an input number num, returns the number of steps to reach 1 in the Collatz sequence.
    First, checks the collatz_table for a previously calculated value.
    If not found, calculates it recursively and stores the result in that table. 
    """

    # lookup first
    if num in collatz_table:
        return collatz_table[num]
    
    # base case
    if num == 1:
        result = 0
    elif num % 2 == 0:
        # even case
        result = 1 + collatz_sequence(num // 2)
    else:
        # odd case
        result = 1 + collatz_sequence(3 * num + 1)
    
    # save the result
    collatz_table[num] = result
    return result


def pease_number(month: int, day: int, year: int) -> int:
    """
    Given a month, day, and year, calculates the Pease number.
    """

    # Calculate Fibo Birthday Constant
    fbc_month = fibonnaci_sequence(month)
    fbc_day = fibonnaci_sequence(day)
    
    # Calculate Collatz Fibo-Birthday
    cfb_month = collatz_sequence(fbc_month)
    cfb_day = collatz_sequence(fbc_day)
    cfb_year = collatz_sequence(year)
    
    # Calculate Pease number
    return cfb_month + cfb_day + cfb_year


def main():
    print("Use Ctrl+C to exit.")

    while True:
        try:
            month = int(input("Enter a month (1-12): "))
            if (month < 1) or (month > 12):
                print("Month must be between 1 and 12.\n")
                continue

            day = int(input("Enter a day (1-31): "))
            if (month == 2) and (day > 29):
                print("February cannot have more than 29 days.\n")
                continue

            if (day < 1) or (day > 31):
                print("Day must be between 1 and 31.\n")
                continue

            year = int(input("Enter a year (e.g., 1776): "))
            if year < 1:
                print("Year must be a positive integer.\n")
                continue

            result = pease_number(month, day, year)
            print(f"Your Pease Number Is: {result}\n")

        except ValueError:
            print("Invalid input. Please enter numeric values only.\n")

        except:
            print("\nexiting...\n")
            break


if __name__ == "__main__":
    main()