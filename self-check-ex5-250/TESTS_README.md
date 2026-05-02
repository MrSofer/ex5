# L Compiler Test Suite (Exercise 5) - 250 Tests

## Overview

This document describes the full test suite for the L compiler (Exercise 5), comprising 250 tests. The tests cover all compiler phases -- lexing, parsing, semantic analysis, register allocation, and MIPS code generation -- as well as runtime behavior under SPIM. Tests range from basic programs (printing primes, sorting) to stress tests for register allocation, saturation arithmetic edge cases, runtime error detection, and full integration programs that exercise every major language feature simultaneously.

Each test consists of an L source file in `tests/` and a corresponding expected output file in `expected_output/`. The expected output matches SPIM output format (including the standard SPIM header) unless the test is expected to fail at a compiler phase, in which case the expected output contains an error token such as `ERROR`, `ERROR(3)`, `ERROR(4)`, `ERROR(8)`, or `Register Allocation Failed`.

---

## Table of Contents

1. [Tests 01-26: Original / Foundational Tests](#tests-01-26-original--foundational-tests)
2. [Tests 27-56: Edge Cases](#tests-27-56-edge-cases)
3. [Tests 57-61: Saturation and Literal Edge Cases](#tests-57-61-saturation-and-literal-edge-cases)
4. [Tests 62-65: More Arithmetic](#tests-62-65-more-arithmetic)
5. [Tests 66-70: String Operations](#tests-66-70-string-operations)
6. [Tests 71-75: Array Operations](#tests-71-75-array-operations)
7. [Tests 76-80: Class Operations](#tests-76-80-class-operations)
8. [Tests 81-85: Control Flow](#tests-81-85-control-flow)
9. [Tests 86-88: Evaluation Order](#tests-86-88-evaluation-order)
10. [Tests 89-91: Global Variables](#tests-89-91-global-variables)
11. [Tests 92-96: Runtime Errors](#tests-92-96-runtime-errors)
12. [Tests 97-100: Compiler Error Cases](#tests-97-100-compiler-error-cases)
13. [Tests 101-112: Miscellaneous](#tests-101-112-miscellaneous)
14. [Tests 113-120: Arithmetic Edge Cases](#tests-113-120-arithmetic-edge-cases)
15. [Tests 121-130: String Operations (Extended)](#tests-121-130-string-operations-extended)
16. [Tests 131-140: Array Operations (Extended)](#tests-131-140-array-operations-extended)
17. [Tests 141-150: Class Features (Extended)](#tests-141-150-class-features-extended)
18. [Tests 151-160: Control Flow (Extended)](#tests-151-160-control-flow-extended)
19. [Tests 161-170: Function Features](#tests-161-170-function-features)
20. [Tests 171-180: Error Cases (Extended)](#tests-171-180-error-cases-extended)
21. [Tests 181-190: Global Variable Edge Cases](#tests-181-190-global-variable-edge-cases)
22. [Tests 191-200: Register Allocation Stress](#tests-191-200-register-allocation-stress)
23. [Tests 201-210: Evaluation Order (Extended)](#tests-201-210-evaluation-order-extended)
24. [Tests 211-220: Runtime Error Edge Cases](#tests-211-220-runtime-error-edge-cases)
25. [Tests 221-230: Complex Programs / Algorithms](#tests-221-230-complex-programs--algorithms)
26. [Tests 231-240: Inheritance and Polymorphism](#tests-231-240-inheritance-and-polymorphism)
27. [Tests 241-250: Full Integration](#tests-241-250-full-integration)
28. [Error and Failure Tests Summary](#error-and-failure-tests-summary)
29. [Runtime Error Tests Summary](#runtime-error-tests-summary)
30. [Notes on the L Language and Testing](#notes-on-the-l-language-and-testing)

---

## Tests 01-26: Original / Foundational Tests

These are the foundational tests covering core language features: basic I/O, sorting algorithms, data structures, classes, strings, arrays, operator precedence, recursion, overflow/saturation, register pressure, global variables, inheritance, and runtime errors.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 01 | Print_Primes | Prints prime numbers up to 100 | 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97 |
| 02 | Bubble_Sort | Bubble sort implementation | -600 -580 -400 12 30 34 70 |
| 03 | Merge_Lists | Merge two sorted lists | 12 34 50 70 92 96 97 99 |
| 04 | Matrices | Matrix operations | 12 |
| 05 | Classes | Basic class features | 8400 8400 |
| 06 | Strings | String operations | Havingsaidthat |
| 07 | Arrays | Array operations | SonWALKFatherSWIM |
| 08 | Access_Violation | Array out of bounds | Access Violation |
| 09 | Access_Violation | Another access violation case | Invalid Pointer Dereference |
| 10 | Tree | Binary tree operations | 1729 |
| 11 | Precedence | Operator precedence | 7 4 1 3 -1 |
| 12 | Fib | Fibonacci | 34 |
| 13 | Overflow | Integer overflow/saturation | 32767 |
| 14 | Many_Local_Variables | Many local variables (register pressure) | 528 |
| 15 | Many_Data_Members | Many class data members | 47 |
| 16 | Classes | More class features | Access Violation |
| 17 | Global_Variables | Global variable init and access | 160 |
| 18 | (unnamed) | Function call returning constant | 27 |
| 19 | (unnamed) | Nested function calls | 27 |
| 20 | (unnamed) | Class instantiation and method call | what |
| 21 | (unnamed) | Class inheritance with method override | no |
| 22 | (unnamed) | Recursive function | 2 |
| 23 | (unnamed) | Multiple inheritance levels with overrides | noyesgo666 |
| 24 | (unnamed) | Division by zero in method | whatIllegal Division By Zero |
| 25 | (unnamed) | Large negative number arithmetic | -32768 |
| 26 | (unnamed) | 18-parameter function (too many for register allocation) | Register Allocation Failed |

---

## Tests 27-56: Edge Cases

These tests target edge cases in recursion, data structures, saturation, evaluation order, control flow, nil handling, reference aliasing, and register allocation pressure.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 27 | Deep_Recursion | Ackermann function | 1 4 7 29 |
| 28 | Chained_Assignments | `a := b := c := d := 42` (invalid L syntax) | 42 42 42 42 |
| 29 | Array_of_Arrays | 2D arrays with nested indexing | 1 2 3 4 5 6 7 8 9 |
| 30 | Method_Returns_Object | Linked list creation via method returns | 10 20 30 |
| 31 | Complex_Saturation | -32768 literal (lexer rejects 32768) | ERROR |
| 32 | Nested_Method_Calls | Method calls as arguments | 11 21 7 |
| 33 | Empty_Array | `new int[0]` zero-length array | 42 |
| 34 | Single_Element_Array_Bounds | arr[1] on size-1 array triggers Access Violation | 42 Access Violation |
| 35 | Multiple_Classes_Interaction | Objects containing objects | 250 |
| 36 | Division_Edge_Cases | Division with negative numbers, floor division | 0 3 5 10 32767 1 0 9 |
| 37 | Side_Effects_In_Expressions | Global mutations during eval, left-to-right order | 45 30 |
| 38 | Deep_Inheritance_Chain | 4-level inheritance A->B->C->D | 1 2 3 4 10 |
| 39 | Tricky_While_Conditions | while(i) where i counts to 0 | 5 4 3 2 1 100 |
| 40 | Nested_If_While | Triple-nested control structures | 11 22 33 |
| 41 | Array_Class_Mix | Arrays stored in classes | 70 |
| 42 | Zero_Arithmetic | All operations with zero operands | 0 0 0 0 0 0 |
| 43 | Negative_Numbers_Operations | Arithmetic with negative operands | -50 -150 -200 -50 100 -8 -5 |
| 44 | String_Empty_Concatenation | Empty string edge cases | HelloHello1 1 |
| 45 | Chained_Array_Access | values[indices[i]] | 20 50 80 |
| 46 | Global_Array_Init | Global array initialized in main | 5 10 15 |
| 47 | Complex_Global_Init | Chain of dependent global inits | 2 5 10 5 2 |
| 48 | Nil_Comparisons | nil = nil, object = nil, etc. | 1 1 0 1 0 |
| 49 | Fibonacci_Iterative | Iterative Fibonacci with temps | 0 1 5 55 610 |
| 50 | Array_Copy_Edge_Cases | Reference aliasing | 10 20 30 1 |
| 51 | Return_In_Loop | Early return from while loop | 0 1 4 7 9 -1 |
| 52 | Class_Self_Reference | Circular linked list | 10 20 30 10 |
| 53 | Many_Parameters | 8-parameter function | 36 120 |
| 54 | Mixed_Access_Violations | Valid accesses then out-of-bounds | 10 20 Access Violation |
| 55 | Expression_Precedence_Complex | Multi-operator precedence | 9 8 9 15 25 24 4 |
| 56 | Large_Array_Operations | 100-element array sum | 4950 |

---

## Tests 57-61: Saturation and Literal Edge Cases

These tests focus on saturation arithmetic triggered by computation (not by literal values), chained saturation, multiplication overflow, division edge cases, and exact boundary values.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 57 | Saturation_Via_Computation | Overflow via computation (0 - 32767 - 1) | -32768 32767 |
| 58 | Saturation_Chain | Chained saturating operations | 32767 32767 -32768 -32768 |
| 59 | Multiplication_Overflow | Multiplication overflow scenarios | 32767 32767 0 32767 -32768 32767 |
| 60 | Division_Saturation | Division edge cases with saturation | 32767 -32767 3 -3 3 0 |
| 61 | Saturation_Boundary_Exact | Exact boundary values 32767 and -32768 | 32767 32766 -32767 -32768 32767 -32768 |

---

## Tests 62-65: More Arithmetic

Additional arithmetic tests covering underflow, saturation recovery, floor division, and negative division.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 62 | Subtraction_Underflow | Subtraction causing underflow | -32768 -32768 -32768 |
| 63 | Saturate_Then_Continue | Saturate then recover | 32767 32766 32767 32767 |
| 64 | Floor_Division | Floor division behavior | 3 -3 0 0 1 |
| 65 | Negative_Division | Division with negatives | -5 -10 3 -2 3 |

---

## Tests 66-70: String Operations

Tests for string content equality, multi-concatenation, equality after concatenation, multiple print calls, and empty string edge cases.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 66 | String_Equality | String content equality | 1 0 1 |
| 67 | String_Concat_Multi | Multiple string concatenations | HelloBigWorldHelloHelloWorld |
| 68 | String_Equality_After_Concat | Compare concatenated strings | 1 |
| 69 | PrintString_Multiple | Multiple PrintString calls | onetwothree |
| 70 | Empty_String_Operations | Empty string edge cases | 1 hi |

---

## Tests 71-75: Array Operations

Tests for array reference equality, last-element access, negative index handling, index-at-length boundary, and single-element array operations.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 71 | Array_Equality | Array reference equality | 1 0 |
| 72 | Array_Last_Element | Access last element | 10 50 |
| 73 | Array_Negative_Index | Negative index triggers Access Violation | Access Violation |
| 74 | Array_Index_At_Length | Index equal to length triggers Access Violation | Access Violation |
| 75 | Array_Size_One | Single element array operations | 42 |

---

## Tests 76-80: Class Operations

Tests for reference equality, nil field access (Invalid Pointer Dereference), inherited fields, three-level virtual dispatch, and methods with multiple parameters.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 76 | Class_Equality | Reference equality for objects | 1 0 0 |
| 77 | Nil_Field_Access | Field access on nil triggers Invalid Pointer Dereference | Invalid Pointer Dereference |
| 78 | Inheritance_Field | Accessing inherited fields | 10 20 |
| 79 | Virtual_Dispatch_Three_Levels | 3-level virtual dispatch | 1 2 3 |
| 80 | Class_Method_With_Params | Methods with multiple parameters | 150 120 120 |

---

## Tests 81-85: Control Flow

Tests for truthy non-zero values, zero-iteration loops, cascading if/else, nested while with early return, and countdown loops.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 81 | If_NonZero_Truthy | Non-zero values as true | 1 2 3 |
| 82 | While_Zero_Iterations | While with false condition | 99 |
| 83 | If_Else_Chain | Cascading if/else | -1 0 1 |
| 84 | Nested_While_Break_Return | Nested while with early return | 4 11 1 |
| 85 | While_Countdown | Countdown loop | 5 4 3 2 1 |

---

## Tests 86-88: Evaluation Order

Tests verifying left-to-right evaluation order for binary operations and function arguments, based on the PDF specification.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 86 | Eval_Order_PDF_Example | PDF Figure 1 example | 32766 |
| 87 | Eval_Order_Binary | Binary operation order | 11 10 |
| 88 | Eval_Order_Args_Three | Three-argument evaluation order | 6 3 |

---

## Tests 89-91: Global Variables

Tests for global initialization order, global class instances, and global arrays.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 89 | Global_Init_Order | Global initialization order | 5 15 30 |
| 90 | Global_Class_Instance | Global class instance | 3 4 |
| 91 | Global_Array | Global array variable | 10 20 30 |

---

## Tests 92-96: Runtime Errors

Tests that verify runtime error detection: division by zero, method call on nil, array access on nil, and output followed by a runtime error.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 92 | Div_Zero_In_Expression | Division by zero in expression | Illegal Division By Zero |
| 93 | Null_Method_Call | Method call on nil object | Invalid Pointer Dereference |
| 94 | Array_Nil_Access | Access on nil array | Invalid Pointer Dereference |
| 95 | Div_Zero_After_Output | Output then division by zero | 42 Illegal Division By Zero |
| 96 | Null_Deref_After_Output | Output then null dereference | beforeInvalid Pointer Dereference |

---

## Tests 97-100: Compiler Error Cases

Tests that expect compiler-phase errors: lexer errors, semantic errors, and register allocation failure.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 97 | Lexer_Error_Bad_Token | Invalid token | ERROR |
| 98 | Semantic_Type_Mismatch | Type mismatch | ERROR(3) |
| 99 | Semantic_Undeclared | Undeclared variable | ERROR(3) |
| 100 | RegAlloc_Fail | Too many variables for registers | Register Allocation Failed |

---

## Tests 101-112: Miscellaneous

A varied collection covering void returns, factorial, arrays of objects, linked lists, default field values, nested classes, multiple return paths, GCD, power functions, string building, complex inheritance, and a comprehensive integration test.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 101 | Void_Function_Return | Void function with return | 5 10 |
| 102 | Recursive_Factorial | Factorial via recursion | 1 120 5040 |
| 103 | Array_Of_Objects | Array holding objects | 10 20 30 |
| 104 | Linked_List | Linked list traversal | 1 2 3 |
| 105 | Class_Init_Defaults | Default field values | 42 100 hi |
| 106 | Nested_Classes | Classes containing class instances | 77 |
| 107 | Multiple_Returns | Multiple return paths | 5 3 0 |
| 108 | GCD | Greatest common divisor | 6 25 1 |
| 109 | Power_Function | Power via recursion | 1024 243 1 |
| 110 | String_Builder | Building strings via concatenation | abababxxxxx |
| 111 | Complex_Inheritance | Complex inheritance hierarchy | square25 rect21 |
| 112 | Ultimate | Comprehensive test: 4 animal classes, inheritance, virtual dispatch, arrays, linked list, recursion, saturation, string ops, nil checks | meowwooftweetFelix21 2 13325 32767 -32768 1 0 meowwoof1 0 4441 17 225 |

---

## Tests 113-120: Arithmetic Edge Cases

Focused tests for overflow chains, multiplication overflow cascades, division truncation toward zero, saturation recovery, mixed arithmetic expressions, chained subtractions, division by negative numbers, and arithmetic with function return values.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 113 | Overflow_Chain | Repeated addition pushing past saturation | 32767 32767 32767 -32768 -32768 |
| 114 | Multiply_Overflow_Cascade | Multiplication overflow: 200*200, 32767*2, etc. | 32767 32767 32767 32767 -32768 32761 |
| 115 | Division_Truncation | Division truncation toward zero: 7/2, 1/3, 100/7 | 3 0 14 16383 -16384 1 0 -3 2 3 |
| 116 | Saturation_Recovery | Saturate to max/min, then recover with opposite operations | 32767 32567 32767 -32768 -32568 -32768 |
| 117 | Mixed_Arith_Expressions | Function a*b+c with overflow cases | 17 10100 1 0 -1 12 |
| 118 | Subtraction_Chains | Chained subtractions including negation of -32768 | -100 -100 200 -400 0 -32768 32767 32767 -32768 |
| 119 | Division_By_Negative | Division by negative numbers | -10 10 -32767 32767 33 10 -1 1 |
| 120 | Arith_With_Function_Results | double, triple, square functions with nesting | 10 15 25 32000 32767 32767 25 36 18 |

---

## Tests 121-130: String Operations (Extended)

Extended string tests covering equality with various lengths, chained concatenation with empty strings, comparing concatenated strings to literals, interleaved PrintString/PrintInt, strings as function parameters, global string variables, string fields in classes, arrays of strings, virtual dispatch returning strings, and empty string edge cases.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 121 | String_Equality_Various | Same content, different lengths, empty strings | 1 0 0 0 1 0 1 |
| 122 | String_Concat_Chain | Chained concatenation a+b+c with empty strings | HelloHelloHelloHello |
| 123 | String_Equality_After_Concat | Comparing concatenated to literal strings | 1 1 1 1 |
| 124 | PrintString_Interleaved | Interleaved PrintString and PrintInt | a1 b2 c3 end |
| 125 | String_As_Parameter | Strings as function parameters | HelloAliceHiBobJohnDoeWelcomeJohnDoe |
| 126 | String_Global_Vars | Global string variables with concatenation | helloworldhelloworldgoodbyeworld |
| 127 | String_In_Class | String field in class with getter/setter | hibyebyebyebye |
| 128 | String_Array | Array of strings with iteration | alphabetagammadeltaepsilon1 0 alphabeta |
| 129 | String_Return_From_Method | Virtual dispatch returning strings | unknownsilentcatmeowdogwoofdogwoof |
| 130 | Empty_String_Edge | Empty string equality and concatenation | 1 0 1 1 1 1 xx |

---

## Tests 131-140: Array Operations (Extended)

Extended array tests covering matrix multiplication, element-by-element copy, exact bounds access violation, negative index access violation, 3D arrays, swap/reverse, sum/average, aliasing, arrays of objects, and nil element access.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 131 | Matrix_Multiply | 2x2 matrix multiplication via arrays | 19 22 43 50 |
| 132 | Array_Copy | Element-by-element copy, verify independence | 10 20 30 40 50 10 999 |
| 133 | Array_Bounds_Exact | Access at index = length triggers Access Violation | 100 200 300 Access Violation |
| 134 | Array_Negative_Index | Negative index triggers Access Violation | Access Violation |
| 135 | ThreeD_Array | 3D array via nested typedefs | 36 |
| 136 | Array_Swap_Elements | Swap function to reverse array | 10 20 30 40 |
| 137 | Array_Sum_Average | Sum and average (integer division) | 210 35 |
| 138 | Array_Alias | Two vars pointing to same array | 1 99 99 2 |
| 139 | Array_Of_Objects | Array of Point objects, find closest to origin | 1 1 1 |
| 140 | Array_Nil_Element_Access | Field access on nil array element | 42 Invalid Pointer Dereference |

---

## Tests 141-150: Class Features (Extended)

Extended class tests covering 5-level inheritance, selective method override, field defaults through inheritance, method chaining via return values, reference equality and nil comparison, three-level nested classes, polymorphic arrays, counter with this-reference, graph data structure, and overridden getters in grandchildren.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 141 | Deep_Inheritance_Five | 5-level inheritance with virtual dispatch | 15 15 10 10 15 |
| 142 | Override_Selective | Selective method override across levels | 1 20 300 1 20 300 1 20 300 |
| 143 | Field_Defaults_Inheritance | Inherited field defaults (Vehicle/Car/Truck) | 4 0 generic4 4 0 generic1000 3 6 |
| 144 | Method_Chaining_Via_Return | Pair class with nested addPairs calls | 4 6 14 26 8 12 |
| 145 | Class_Equality_And_Nil | Reference equality, nil comparison, aliasing | 0 1 0 0 1 0 99 |
| 146 | Class_As_Field | Three-level nested classes (Deep/Outer/Inner) | 42 42 42 42 |
| 147 | Polymorphic_Array | Shape[] with Circle, Rect, Triangle | 0 75 21 12 108 |
| 148 | This_In_Method | Counter class with inc/dec/addN methods | 3 2 12 13 13 1 |
| 149 | Multiple_Classes_Graph | Graph with Edge and Vertex classes | 0 2 1 10 2 20 1 1 2 5 2 0 |
| 150 | Inherited_Field_Override | Overridden getter in GrandChild | 60 20 150 200 200 120 200 |

---

## Tests 151-160: Control Flow (Extended)

Extended control flow tests covering nested while computing triangular sums, cascading if-return classifiers, early return via i*i > target, complex conditions, converging variable loops, void functions with early return, multiple accumulators, nested countAbove, zero-iteration while, and the Collatz sequence.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 151 | Nested_While_Triangle | Nested while computing triangular sums | 1 3 6 10 15 |
| 152 | If_Else_Cascade | Cascading if-return classifier | -1 0 1 2 3 3 |
| 153 | Early_Return | Find smallest i where i*i > target | 1 2 4 11 32 |
| 154 | Complex_Condition | Complex conditions: (a<b)=0, (a+b)=c | 1 3 4 5 6 |
| 155 | While_With_Multiple_Updates | Two variables converging in while | 60 60 32767 15 |
| 156 | If_Return_Void | Void function with early return | 5 10 1 |
| 157 | Loop_Accumulator | Sum, factorial, alternating sign accumulators | 28 5040 -5 |
| 158 | Nested_If_While | countAbove: count elements above threshold | 4 8 0 5 |
| 159 | While_Zero_Iterations | while(0) and while(1) with return | 100 0 42 |
| 160 | Collatz_Sequence | Collatz from 27, count steps | 111 1 |

---

## Tests 161-170: Function Features

Tests for recursive sum, utility functions (isEven, abs, max, min), many parameters, void functions with side effects, recursive Fibonacci, nested function calls as arguments, recursive power, Tower of Hanoi, recursive linked list methods, and global init via function calls.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 161 | Recursive_Sum | Recursive sum 1 to n | 0 1 55 5050 32640 |
| 162 | Mutual_Recursion_Classes | isEven, abs, max, min utility functions | 1 0 1 0 5 5 0 7 10 3 2 |
| 163 | Many_Parameters | Functions with 8 parameters | 36 3600 300 |
| 164 | Void_Functions_Side_Effects | Void functions modifying global counter | 3 10 0 100 |
| 165 | Recursive_Fibonacci | Classic recursive fib for n=0,1,2,5,10,15 | 0 1 1 5 55 610 |
| 166 | Function_Calls_As_Args | Nested calls: add(inc(5), dbl(3)), dbl(dbl(dbl(1))) | 12 3 8 13 10 |
| 167 | Recursive_Power | Recursive power with various bases/exponents | 1 2 1024 243 16384 32767 125 |
| 168 | Tower_Of_Hanoi | Hanoi move counter for n=1,3,5,10 | 1 7 31 1023 |
| 169 | Method_With_Recursion | Recursive linked list length and sum | 3 60 2 50 1 30 |
| 170 | Global_Init_With_Functions | Globals initialized by function calls | 21 123 42 186 |

---

## Tests 171-180: Error Cases (Extended)

Tests for compiler-phase errors: lexer errors (oversized integer literal, underscore in identifier, space in string), semantic errors (undefined function, type mismatch, undeclared class, wrong argument type, nonexistent method, return type mismatch, duplicate variable).

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 171 | Lexer_Bad_Integer | Integer literal 99999 exceeds 32767 | ERROR |
| 172 | Lexer_Underscore_Id | Identifier with underscore | ERROR |
| 173 | Lexer_Bad_String | String with space character | ERROR |
| 174 | Semantic_Undefined_Func | Call to undeclared function | ERROR(3) |
| 175 | Semantic_Type_Mismatch_Assign | Assigning int to string | ERROR(4) |
| 176 | Semantic_Undeclared_Class | Using undeclared class | ERROR(3) |
| 177 | Semantic_Wrong_Arg_Type | String arg where int expected | ERROR(8) |
| 178 | Semantic_No_Such_Method | Calling non-existent method | ERROR(8) |
| 179 | Semantic_Return_Type | Returning string from int function | ERROR(3) |
| 180 | Semantic_Duplicate_Var | Duplicate variable in same scope | ERROR(4) |

---

## Tests 181-190: Global Variable Edge Cases

Tests for global variable initialization by function calls, chained dependencies, array creation during init, class instance creation, multiple global objects with mutation, side effects during init, saturation during init, string concatenation during init, arrays of objects as globals, and nested function call expressions in globals.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 181 | Global_Func_Init | Global variable initialized by function call | 15 45 |
| 182 | Global_Chain_Init | Chain of global init dependencies (x->y->z->w) | 1 2 4 7 |
| 183 | Global_Array_Fill | Global array created at init, filled by function | 0 1 4 9 16 |
| 184 | Global_Object_Init | Global class instance with default fields | 100 200 default320 240 custom |
| 185 | Global_Multiple_Objs | Two global objects with mutation | 20 10 30 40 |
| 186 | Global_Func_Side_Effect | Global init calls nextId() modifying counter | 1 2 3 3 |
| 187 | Global_Saturation_Init | Global init triggers saturation (32000+32000) | 32000 32767 |
| 188 | Global_String_Concat | Global string concatenation at init | helloworldhelloworld |
| 189 | Global_Array_Of_Objs | Global array of class instances | sword100 shield200 potion300 600 |
| 190 | Global_Nested_Expr | Globals with nested function call expressions | 3 7 17 65 1153 |

---

## Tests 191-200: Register Allocation Stress

Tests designed to stress the register allocator with many local variables, long computation chains, nested calls with many arguments, functions with 10 parameters, deeply nested arithmetic expressions, multiple concurrent local arrays, recursive functions with many locals per frame, multiple return paths through nested ifs, interleaved function calls, and many temporary result variables.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 191 | Many_Locals_Sum | 20 local variables summed | 210 |
| 192 | Complex_Expr_Chain | 10 chained local computations | 70 10 213 |
| 193 | Nested_Calls_Many_Args | Nested function calls as arguments | 140 136 |
| 194 | Many_Params_Func | Functions with 10 parameters | 55 55 |
| 195 | Deep_Nesting_Expr | Deeply nested arithmetic | 102 90 |
| 196 | Many_Local_Arrays | 4 local arrays used concurrently | 60 |
| 197 | Recursive_Many_Locals | Recursive function with 4 locals per frame | 65 5 27 |
| 198 | Multi_Return_Paths | 5 params, 6 return paths through nested ifs | 19 18 21 21 7 |
| 199 | Interleaved_Calls | Interleaved doubleIt, tripleIt, addTwo, square calls | 15 153 261 25 30 |
| 200 | Lots_Of_Temps | 8 result variables from complex expressions | 15 19 26 60 105 54 1120 1165 |

---

## Tests 201-210: Evaluation Order (Extended)

Tests verifying left-to-right evaluation order for addition, multiplication, three-argument function calls, subtraction, division, array index side effects, equality with side effects, method call arguments, nested binary expressions with precedence, and sequential assignment right-hand sides.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 201 | Eval_Order_Add | Left-to-right in sideA() + sideB() | 8 6 |
| 202 | Eval_Order_Mul | Left-to-right in incG() * doubleG() | 72 12 |
| 203 | Eval_Order_Three_Args | 3-arg evaluation: process(bump(1), bump(10), bump(100)) | 123 111 |
| 204 | Eval_Order_Subtraction | Left-to-right in decG() - negG() | 14 -7 |
| 205 | Eval_Order_Division | Left-to-right in halfG() / addTen() | 0 60 |
| 206 | Eval_Order_Array_Idx | Side effects in array index: arr[next()] + arr[next()] | 500 2 |
| 207 | Eval_Order_Equality | Side effects in equality: setG(5) = setG(10) | 0 10 |
| 208 | Eval_Order_Method_Args | Eval order for method call arguments | 6 3 |
| 209 | Eval_Order_Nested_Binary | Eval with precedence: tick() + tick() * tick() | 7 3 |
| 210 | Eval_Order_Assign_Rhs | Sequential getAndBump() calls | 0 1 2 3 4 5 |

---

## Tests 211-220: Runtime Error Edge Cases

Tests for runtime errors occurring in various contexts: division by zero inside if blocks, mid-loop, from function return values; null pointer dereference on field write, through chained nil references; negative array index; exact bounds violation; nil array access; division by zero inside methods; and null dereference through uninitialized array elements.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 211 | Div_Zero_In_If | Division by zero inside if block | Illegal Division By Zero |
| 212 | Div_Zero_In_While | Division by zero mid-loop | 5 50 4 100 3 Illegal Division By Zero |
| 213 | Div_Zero_Func_Return | Division by zero from function return value | startIllegal Division By Zero |
| 214 | Null_Field_Write | Null pointer dereference on field write | beforeInvalid Pointer Dereference |
| 215 | Null_Method_Deep | Null through chain (a.next is nil) | 1 Invalid Pointer Dereference |
| 216 | Array_Neg_Index | Negative array index | 10 Access Violation |
| 217 | Array_Exact_Bound | Access at exactly array length | 10 20 30 Access Violation |
| 218 | Null_Array_Access | Element access on nil array | aboutInvalid Pointer Dereference |
| 219 | Div_Zero_In_Method | Division by zero inside class method | 5 Illegal Division By Zero |
| 220 | Null_In_Array_Elem | Null dereference through uninitialized array element | 42 Invalid Pointer Dereference |

---

## Tests 221-230: Complex Programs / Algorithms

Tests implementing complete algorithms: selection sort, insertion sort, stack (push/pop/peek), queue via linked list, Fibonacci array, matrix multiplication, iterative power with saturation, sieve of Eratosthenes, binary search, and GCD/LCM.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 221 | Selection_Sort | Selection sort on 8 elements | 1 11 12 22 25 45 64 90 |
| 222 | Insertion_Sort | Insertion sort on 6 elements | Access Violation |
| 223 | Stack_Class | Stack with push, pop, peek, isEmpty | 30 30 20 0 10 1 |
| 224 | Queue_Linked_List | Queue with enqueue, dequeue, isEmpty | 10 20 30 40 50 1 |
| 225 | Fibonacci_Array | First 20 Fibonacci numbers in array | 0 1 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1597 2584 4181 |
| 226 | Matrix_Multiply | 2x2 matrix multiplication using flat arrays | 19 22 43 50 |
| 227 | Power_Iterative | Iterative power with saturation | 1 2 1024 243 125 1 32767 |
| 228 | Sieve_Of_Primes | Sieve of Eratosthenes for primes under 50 | 2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 |
| 229 | Binary_Search | Binary search on sorted array | 5 0 9 -1 3 |
| 230 | GCD_LCM | GCD (Euclidean) and LCM | 4 25 1 12 12 21 24 |

---

## Tests 231-240: Inheritance and Polymorphism

Tests for three-level virtual dispatch, selective override, field inheritance across hierarchies, polymorphic arrays of shapes, inherited methods with overridden helpers, five-level chains with base reference reassignment, expression trees (Expr/Literal/Add/Mul), upcasting and aliasing, method override with field access in subclass, and multiple subtypes with findMaxPriority.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 231 | Three_Level_Override | 3-level virtual dispatch (Base/Mid/Leaf) | 10 15 18 basemidleaf |
| 232 | Partial_Override | Some methods inherited, some overridden | 4 bark30 4 meow0 |
| 233 | Field_Inheritance | Field inheritance across Vehicle/Car/Truck | 4 1500 sedan4 5 6 1000 hauler5000 3 |
| 234 | Polymorphic_Array | Shape array with Circle, Rectangle, Triangle | 75 28 30 0 133 |
| 235 | Method_Calls_Inherited | Inherited getCount() with overridden increment() | 1 2 2 2 4 4 3 6 6 |
| 236 | Deep_Chain_Five | 5-level chain with base reference reassignment | 10000 four100 two1 zero |
| 237 | Override_With_Field | Expression tree (Expr/Literal/Add/Mul) with eval() | 35 7 3 |
| 238 | Upcast_Assign | Upcasting, aliasing, field modification through base | 30 30 30 20 99 119 |
| 239 | Method_Returns_Self_Type | Override with field access in subclass | 42 first110 super |
| 240 | Multiple_Subtypes | 3 subtypes in array with findMaxPriority | greeting1 icon2 submit3 3 |

---

## Tests 241-250: Full Integration

End-to-end integration tests that combine multiple language features into realistic programs: a zoo system with polymorphic arrays, bank accounts with inheritance and interest, full linked list operations, string array manipulation, binary search tree, saturation in complex scenarios, global objects with method calls during init, a matrix class wrapping a flat array, Collatz conjecture with complex branching, and a player/scoreboard system combining arrays, globals, strings, and saturation.

| # | Name | Description | Expected Output |
|---|------|-------------|-----------------|
| 241 | Zoo_System | Zoo with Mammal/Reptile, polymorphic arrays, counting | lion5 1 dog1 0 croc10 1 gecko2 0 2 |
| 242 | Bank_Account | Bank accounts with inheritance, transfer, interest | 1000 500 700 800 35 735 -1 800 |
| 243 | Linked_List_Ops | Full linked list: prepend, length, sum, reverse, print | 4 50 20 15 10 5 5 10 15 20 |
| 244 | String_Array_Ops | String array with concatenation and equality | alphabetagammadeltaepsilonalphabetagammadeltaepsilon0 1 |
| 245 | Recursive_Tree | BST: insert, inorder traversal, sum | 20 30 40 50 60 70 80 350 |
| 246 | Saturation_Complex | Saturation in functions, assignments, boundaries | 32767 32767 32767 -32768 -32768 -32768 32767 -32768 32767 -32768 |
| 247 | Global_Obj_Methods | Global object with methods called during init | 1 2 2 Bob3 Carol |
| 248 | Nested_Array_Class | Matrix class wrapping flat array with get/set/trace | 1 5 9 15 4 3 |
| 249 | Complex_Control_Flow | Collatz conjecture with complex branching | -1 0 1 10 10 0 55 0 5 3 0 |
| 250 | Full_Integration | Player/Scoreboard, arrays, globals, strings, saturation | alpha1 92 78 85 bravo2 100 65 84 charlie3 70 70 70 100 32767 255 |

---

## Error and Failure Tests Summary

The following tests are expected to produce a compiler-phase error (not SPIM output). They should be caught before code generation or during register allocation.

### Lexer Errors (Expected: ERROR)

| # | Name | Reason |
|---|------|--------|
| 31 | Complex_Saturation | -32768 literal (lexer rejects the token 32768) |
| 97 | Lexer_Error_Bad_Token | Invalid token |
| 171 | Lexer_Bad_Integer | Integer literal 99999 exceeds 32767 |
| 172 | Lexer_Underscore_Id | Underscore in identifier (not valid in L) |
| 173 | Lexer_Bad_String | String containing a space character |

### Semantic Errors (Expected: ERROR with error code)

| # | Name | Expected | Reason |
|---|------|----------|--------|
| 28 | Chained_Assignments | ERROR(8) | Chained assignment is invalid L syntax |
| 98 | Semantic_Type_Mismatch | ERROR(3) | Type mismatch |
| 99 | Semantic_Undeclared | ERROR(3) | Undeclared variable |
| 174 | Semantic_Undefined_Func | ERROR(3) | Call to undeclared function |
| 175 | Semantic_Type_Mismatch_Assign | ERROR(4) | Assigning int to string variable |
| 176 | Semantic_Undeclared_Class | ERROR(3) | Using an undeclared class type |
| 177 | Semantic_Wrong_Arg_Type | ERROR(8) | String argument where int expected |
| 178 | Semantic_No_Such_Method | ERROR(8) | Calling a method that does not exist |
| 179 | Semantic_Return_Type | ERROR(3) | Returning string from int function |
| 180 | Semantic_Duplicate_Var | ERROR(4) | Duplicate variable declaration in same scope |

### Register Allocation Failures (Expected: Register Allocation Failed)

| # | Name | Reason |
|---|------|--------|
| 26 | (unnamed) | 18-parameter function exceeds available registers |
| 100 | RegAlloc_Fail | Too many live variables for register allocation |

---

## Runtime Error Tests Summary

The following tests produce SPIM output that includes a runtime error message. The program may print some output before the error is triggered. The error message is part of the expected output.

### Access Violation (Array Out of Bounds)

| # | Name | Trigger |
|---|------|---------|
| 08 | Access_Violation | Array out of bounds |
| 09 | Access_Violation | Another access violation case |
| 34 | Single_Element_Array_Bounds | arr[1] on a size-1 array |
| 54 | Mixed_Access_Violations | Valid accesses followed by out-of-bounds |
| 73 | Array_Negative_Index | Negative array index |
| 74 | Array_Index_At_Length | Index equal to array length |
| 133 | Array_Bounds_Exact | Access at index = length |
| 134 | Array_Negative_Index | Negative index |
| 216 | Array_Neg_Index | Negative array index |
| 217 | Array_Exact_Bound | Access at exactly array length |

### Division by Zero

| # | Name | Trigger |
|---|------|---------|
| 24 | (unnamed) | Division by zero in method |
| 92 | Div_Zero_In_Expression | Division by zero in an expression |
| 95 | Div_Zero_After_Output | Output printed, then division by zero |
| 211 | Div_Zero_In_If | Division by zero inside if block |
| 212 | Div_Zero_In_While | Division by zero mid-loop |
| 213 | Div_Zero_Func_Return | Division by zero from function return value |
| 219 | Div_Zero_In_Method | Division by zero inside class method |

### Invalid Pointer Dereference (Null/Nil)

| # | Name | Trigger |
|---|------|---------|
| 77 | Nil_Field_Access | Field access on nil object |
| 93 | Null_Method_Call | Method call on nil object |
| 94 | Array_Nil_Access | Access on nil array |
| 96 | Null_Deref_After_Output | Output printed, then null dereference |
| 140 | Array_Nil_Element_Access | Field access on nil array element |
| 214 | Null_Field_Write | Null pointer dereference on field write |
| 215 | Null_Method_Deep | Null reference through chained field (a.next is nil) |
| 218 | Null_Array_Access | Element access on nil array |
| 220 | Null_In_Array_Elem | Null dereference through uninitialized array element |

---

## Notes on the L Language and Testing

### L Language Constraints

- **Integer range**: All integers are 16-bit signed, ranging from -32768 to 32767.
- **Saturation arithmetic**: Arithmetic operations that overflow or underflow saturate (clamp) to 32767 or -32768 respectively, rather than wrapping around.
- **Integer literals**: The lexer accepts integer literals in the range 0 to 32767. The value -32768 can only be produced by computation (e.g., 0 - 32767 - 1), not as a literal.
- **Division**: Integer division truncates toward zero (e.g., 7/2 = 3, -7/2 = -3).
- **Booleans**: 0 is false, any non-zero value is true. There is no dedicated boolean type.
- **Strings**: Compared by content equality (not reference). Concatenation with `+`.
- **Arrays**: Compared by reference equality. Out-of-bounds access triggers an Access Violation runtime error.
- **Classes**: Compared by reference equality. Field access or method call on nil triggers an Invalid Pointer Dereference runtime error.
- **Evaluation order**: Left-to-right for binary operators and function/method arguments.
- **Register allocation**: The compiler uses a fixed number of registers. Programs that require more simultaneously live variables than available registers will fail with "Register Allocation Failed".

### SPIM Output Format

All expected output files that represent successful execution include the standard SPIM header. Runtime errors (Access Violation, Division by Zero, Invalid Pointer Dereference) are printed as part of the SPIM output and cause immediate program termination.

### File Organization

- **Test source files**: `tests/TEST_<NN>_<Name>.txt`
- **Expected output files**: `expected_output/TEST_<NN>_<Name>_Expected_Output.txt`

### Running Tests

Tests are executed by compiling each L source file through all compiler phases (lexing, parsing, semantic analysis, IR generation, register allocation, MIPS code generation) and then running the resulting MIPS assembly through SPIM. The actual SPIM output is compared against the expected output file. For error tests, the compiler output is compared against the expected error string.
