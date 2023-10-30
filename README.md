# Neutron Language

Welcome to the Neutron programming language! Neutron is a dummy language that utilizes a Recursive Descent Parser (RDP). This document provides an overview of the grammar rules and constructs of the Neutron language. I am creating this language by reading a book called [crafting interpreters.](https://www.craftinginterpreters.com/) by @munificent.

**Recursive Descent Parser (RDP):** A Recursive Descent Parser (RDP) works by recursively descending through the grammar rules of a language to analyze and process the syntax of the input text.

## Feature implemented by me

- **Lambda Expressions:** Neutron supports lambda expressions, which are similar to Python, one of my favorite programming languages.

- **Break Statements:** Implemented as part of an exercise.

- **Continue Statements:** Added as part of an exercise.

- **For Loop:** Introduced through exercises from the book.

## Operators

1. **Unary operators** ((Left-to-Right Associative):
     - `!` (logical NOT)
     - `-` (negation)

2. **Term Operators** (Left-to-Right Associative):

   - `*` (multiplication)
   - `/` (division)

3. **Comparison Operators** (Left-to-Right Associative):

   - `<` (less than)
   - `<=` (less than or equal)
   - `>` (greater than)
   - `>=` (greater than or equal)

4. **Equality Operators** (Left-to-Right Associative):

   - `==` (equal)
   - `!=` (not equal)

5. **Logical AND Operator** (Left-to-Right Associative):

   - `and`

6. **Logical OR Operator** (Left-to-Right Associative):

   - `or`

7. **Assignment Operator** (Right-to-Left Associative):
   - `=` (assignment)

These operators are ordered from highest precedence (evaluated first) to lowest precedence (evaluated last) when parsing expressions in Neutron. You should use this order to correctly parse and evaluate expressions and statements according to the grammar rules you provided.

### Example

```
box x = 10;
box y = 5;

// Arithmetic Expressions
box sum = x + y;
box difference = x - y;
box product = x * y;
box quotient = x / y;

// Comparison Expressions
box isGreater = x > y;
box isLessOrEqual = x <= y;

// Logical Expressions
box isTrue = true;
box isFalse = false;
box andResult = isTrue and isFalse;
box orResult = isTrue or isFalse;

// Equality Expressions
box isEqual = x == y;
box isNotEqual = x != y;

// Unary Expressions
box isNotTrue = !isTrue;
box negativeX = -x;

// Complex Expressions
box result = (x + y * 2) > (3 * x - y);

// Function Call
fun add(a, b) {
  return a + b;
}

box addResult = add(x, y);

box sumFun = lambda a, b: a + b;

box sumResult = sumFun(x, y);
```

## Statements

1. **Return Statement**:

   - Used to return a value from a function.
   - Example: `return result;`

2. **Break Statement**:

   - Used to exit a loop prematurely.
   - Example: `break;`

3. **Continue Statement**:

   - Used to skip the rest of the current iteration and continue to the next in a loop.
   - Example: `continue;`

4. **Function Declaration** (`fun`):

   - Used to define a new function.
   - Example:
     ```neutron
     fun add(a, b) {
       return a + b;
     }
     ```

5. **Variable Declaration** (`box`):

   - Used to declare and initialize variables.
   - Example:
     ```neutron
     box x = 10;
     ```

6. **If Statement** (`if`):

   - Used for conditional execution.
   - Example:
     ```neutron
     if (condition) {
       // code to execute if condition is true
     } else {
       // code to execute if condition is false
     }
     ```

7. **While Statement** (`while`):

   - Used to create a loop that executes a block of code as long as a condition is true.
   - Example:
     ```neutron
     while (condition) {
       // code to execute while condition is true
     }
     ```

8. **For Statement** (`for`):

   - Used to create a loop with initialization, condition, and increment expressions.
   - Example:
     ```neutron
     for (box i = 0; i < 5; i = i + 1) {
       // code to execute in the loop
     }
     ```

9. **Expression Statement** (`exprStmt`):

   - Used to execute an expression as a statement.
   - Example:
     ```neutron
     x = x + 1;
     ```

10. **Print Statement** (`print`):

    - Used to print values to the console.
    - Example:
      ```neutron
      print("Hello, world!");
      ```

11. **Block Statement** (`block`):
    - Used to create a block of code, typically within control flow statements.
    - Example:
      ```neutron
      {
        // code inside the block
      }
      ```

You can use these statements as appropriate in your Neutron programs to control the flow and perform various tasks.
