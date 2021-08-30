# compiler

> The task of this project was to create a basic compiler using Java

## Description

1) The compiler should generate code which implements all assignablevalues as integers, as follows:

- **int** values
- **booolean values
- **array** values: like Java, Mapl uses reference semantics for arrays; implement as an integer which is a memory address within the heap where the array data resides.

2) The basic compiler should support method declaration, method calls, or arrays.

3) Add support for method declaration and method cal. Variables now must be implemented as MEM exprssions using offsets from TEMP FP.

4) Add support for arrays. Generated code will need to call the pre-defined **_malloc** method to allocate heap memory for array creation.

5) _Short-circuit_ and generate code for th Boolean **and** operator which implements short-circuit semantics.

6) **Array checks:** Generate code which detects out-of-bounds errors during execution of array creations, updates and look-ups; the code should output a meaningful error message then halt execution.

## Usage

### Run

Using your favourite IDE, make sure you are in `Backend.jar` in the `src` directory and add the following

```
 javac mapl/Compile.java
 java mapl.Compile ../examples/methods/counter.mapl
 java ir.Compile ../examples/methods/counter.ir
 java tac.Exec ../examples/methods/counter.tac 5
```
