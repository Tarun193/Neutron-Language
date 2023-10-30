# Neutron Language

Welcome to the Neutron programming language! Neutron is a dummy language that utilizes a Recursive Descent Parser (RDP). This document provides an overview of the grammar rules and constructs of the Neutron language.

**Recursive Descent Parser (RDP):** A Recursive Descent Parser (RDP) works by recursively descending through the grammar rules of a language to analyze and process the syntax of the input text.

## Grammar Rules

Neutron's grammar is defined as follows:

### Expressions:

- **Expression:** expression → `lambda;`

- **Lambda:** `lambda → ("lambda" parameters: expression) | assignment;`

- **Assignment:** `assignment → IDENTIFIER "=" assignment | logic_or;`

- **Logical OR:** `logic_or → logic_and ( "or" logic_and)*;`
- **Logical AND:** `logic_and → equality ("and" equality)*;`
- **Equality:** `equality → comparison ( ( "!=" | "==" ) comparison )*;`
- **Comparison:** `comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*;`
- **Term:** `term → factor ( ( "-" | "+" ) factor )*;`

- **Factor:** `factor → unary ( ( "/" | "*" ) unary )*;`

- **Unary:** `unary → ( "!" | "-" ) unary | call;`

- **Call:** `call → primary ( "(" arguments? ")" )*;`

- **Lambda:** `lambda → "("parameters?")" : ExprStmt | PrintStmt;`

- **Arguments:** `arguments → expression ("," expression)*;`

- **Primary:** `primary → NUMBER | STRING | "true"
| "false" | "nil" '
| "(" expression ")" | IDENTIFIER`;

## Statements:

- **Program:** `program → declaration* EOF;`
- **Block:** `block → "{" declaration* "}";`
- **Declaration:** `declaration → funDecl | varDecl | statement;`
- **Function Declaration:** `funDecl → "fun" function;`
- **Function:** `function → IDENTIFIER "("parameters?")" block;`
- **Parameter:** `parameter → IDENTIFIER ("," IDENTIFIER)*;`
- **Variable Declaration:** `varDecl → "box" IDENTIFIER ( "=" expression )? ";";`
- **Statement:** `statement → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt | returnStmt;`
- **If Statement:** `ifStmt → "if" "(" expression ")" statement ( "else" statement )?;`
- **While Statement:** `whileStmt → "while" "(" expression ")" statement;`
- **For Statement:** `forStmt → "for" "(" (varDecl | exprStmt | ";") expression? ";" expression ")" statement;`
- **Return Statement:** `returnStmt → "return" expression? ";"`
- **Break Statement:** `breakStmt → "break;";`
- - **Continue Statement:** `continueStmt → "continue;";`

**Note:** Statements in Neutron always end with a semicolon. This is why there isn't a ';' after (varDecl | exprStmt ).
