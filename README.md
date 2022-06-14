# GROUP: comp2022-1b

| NAME | NR | GRADE | CONTRIBUTION |
| --- | --- | --- | --- |
| Henrique Ribeiro Nunes | 201906852 | 20 | 25% |
| José António Dantas Macedo | 201705226 | 20 | 25% |
| Margarida Assis Ferreira | 201905046 | 20 | 25% |
| Patrícia do Carmo Nunes Oliveira | 201905427 | 20 | 25% |

GLOBAL Grade of the project: 20

# SUMMARY:

This project aimed to build a working compiler for the programming language “Java-minus-minus”.

It takes **.jmm files** as input and performs **syntactical** and **semantic analysis**, which is followed by the **generation of OLLIR** (OO-based Low-Level Intermediate Representation) code.

The compiler generates valid **JVM** (Java Virtual Machine) instructions in the Jasmin format correspondent to OLLIR code generated, that can be optimized, which are then translated into Java bytecodes by the Jasmin assembler.

## Command Line Arguments

```bash
./comp2022-1b [-r=<num>] [-o] [-d] -i=<input_file.jmm>
```

The program accepts 4 arguments where 3 of them are optional:

* `-i` option specifies the input file with the extension **.jmm** to be compiled;
* `-o` option directs the compiler to perform code optimizations (default *false*);
* `-r` option directs the compiler to use only till a specific number of registers (*-1*: one register for each var (default); *0*: minimum;);
* `-d` option enables additional output related to the compilation process, to help programmers understand the compiler's operation (default *false*).

# SEMANTIC ANALYSIS:

This compiler stage is responsible for validating the contents of the AST, verifying if the program is according to the definitions of the programming language, by ensuring and enforcing semantic rules and accordingly reporting the semantic errors associated with useful messages to the user.

Our tool implements the following semantic rules:

* Fields in static methods:
    1. Class non-static fields can't be used in a static method;
* Check undefined vars:
    1. The local variables must be initialized before being used;
* Check imports errors:
    1. Method caller not imported;
    2. Return type of method not imported;
    3. Type of variable not imported;
    4. Superclass was not imported;
    5. New object class not imported;
* Type checking:
    1. Expression in a condition must return a boolean;
    2. The type of the assignee must be compatible with the assigned;
    3. New Int Array size must be an expression of type integer;
    4. Array access index must be an expression of type integer;
    5. Array access must be done over an array;
    6. The operands in a comparison expression must be integers;
    7. The operands in a logic expression must be booleans;
    8. The operands in an arithmetic expression must be integers;
    9. Arrays cannot be used in arithmetic operations;
    10. Variable used must be declared;
    11. Built-in types must have no methods;
    12. Length is exclusively a property of an array;
* Function calls:
    1. Types of function arguments and parameters must compatible;
    2. The number of arguments must match the number of parameters;
    4. The method must exist in the caller correspondent class;
    5. Object "this" can't be used in a static method;
    6. Class' non-static methods can't be called like static methods;
* Return checking:
    1. Return statement expression must match the method return type;
* Duplicates:
    1. A method can't have parameters with the same name;
    2. A method can't have duplicated local variables;
    3. A class can't have duplicated fields;
    4. A class can't have methods with the same name (no overloading);

# CODE GENERATION:

The code generation of our tool works based on a pipeline principle where the input of each stage corresponds to the output of the last one. This way all the main stages have to be executed to obtain the compiled code of the input code. If in any stage there are error reports associated the compilation stops and the user is informed accordingly.

**Compilation Stages**:

1. Jmm code parsing (Syntactic Analysis) and AST generation
2. Symbol Table and Semantic Analysis
3. Abstract Syntax Tree Optimization: (`-o`)
    * Constant Propagation
    * Constant Folding
    * Dead Code Elimination
4. Ollir code generation
5. Ollir code Optimization:
    * While to do-while replacement (`-o`)
    * Register Allocation (`-r=<num>`)
6. Jasmin code generation:
    * Use of JVM efficient instruction

Stages 4 and the first item of stage 6 only are executed when the compiler is called with the flag `-o`. The same way that the second item of stage 6 is executed according to the value of the flag `-r`, or `-1` by default. Unlike stage 7 where the optimization is always considered.

**Possible Problems**:

* We consider that our code generation does not have any associated problems.

## Syntactic Analysis and AST generation

The `java--` language is represented and defined by the corresponding grammar created with specific rules to include all types of statements admissible by the original language.

There were taken into consideration and resolved the **Choice Conflict** and **Operator Priority** problems adjacents to such a complex language. In some cases, to resolve the first issue we use SCAN but never with more than 2 lookahead tokens.

The *AST* of the input *.jmm* file is generated based on this grammar that was annotated in a way that would remove and change unnecessary or redundant AST nodes (**AST Clean Up and Node Annotation**) to simplify the original AST, including the association of some attributes to a specific node as the column and the line the token occurs in the *jmm* file.

## Symbol Table and Semantic Analysis

A `SymbolTable` is an implemented class that stores important information regarding the input .jmm file so that it can be accessed easier. It includes the following information:

* Imports
* Class Name
* Extended Class
* Fields
* Methods:
  * Signature
  * Return Type
  * Parameters
  * Local Variables
  * Static Modifier

To fill the `SymbolTable`, a **visitor** `SymbolTableVisitor` that iterates the AST in *preorder* is defined. In this case, to obtain the wanted information regarding the class, fields, and methods the only nodes that need to be visited are: ***ImportStatement***, ***ClassDeclaration***, and ***MethodDeclaration***.

This is an important step before the **Semantic Analysis** as it centers on important information about the code to be used together with the *AST*.

All the main items of the **semantic rules** defined in the first section were implemented by a *visitor* that extends indirectly a `PreorderJmmVisitor`. For each one of the verifications was used the most convenient visitor and strategy for each task to perform. This analysis is always executed based on the `AST` and `SymbolTable` created beforehand.

## AST Optimization

### Constant folding

For this we created a *post-order visitor* that, when finds arithmetic or logic expression checks if its children are either both integer literals or both boolean literals (true or false). If this happens, the result of the expression is computed and the node is replaced with that same result.

### Constant propagation

For each method, we create a constant propagation table as a map that has the name of the variables as keys and the correspondent constants, at the time, as values.

When an assignment like *b = c*, where *c* is a constant, is found, that information is stored in the propagation table.

The method also has a flag named `propagating` that defines if the propagation is to be performed or not. When `propagating` is true, if an assignment like *b = x*, where *x* is not a constant, appears, and *x* is in the propagation table, the *x* in the assignment is replaced by the corresponding constant.

In the case of while loops the loop is traversed twice:

* The first time with the `propagating` flag set to false, which means a variable b will be removed from the propagation table if it appears in an assignment like *b = x*, where *x* is not a constant.
* The second time with the `propagating` flag set to true, which means that when an assignment like *b = x*, where *x* is not a constant, appears, the value of *x* will be propagated to b.

### Dead Code Elimination

Here we use a pre-order visitor to visit while and if statements. If the condition of a while statement is false it removes the loop from the AST. If the condition of an if is true we replace de if statement with the code inside the if block, while if the condition is false, the code inside the else block is used.

***Note***: these three steps are executed in a loop until the **result stabilizes**, to achieve the maximum possible optimization.

## OLLIR code generation  

After AST validation by the semantic analysis phase, we walk through the AST (*top-down*) using visitors and generate the corresponding **OLLIR code** taking into consideration that this is a *3-address code* and making the necessary adjustments if needed.

## OLLIR code optimization

### While to Do-While

The goal of this optimization is to decrease the number of `goto`s' expressions. So we start by decreasing from 3 to only 2 `goto`s for each while by changing the place the condition of the loop appears in the code.

Before:
```ollir
Loop:
    if (condition) goto Body;
    goto EndLoop;
Body:
    ...
    goto Loop;
EndLoop:
```

After:
```ollir
    goto EndLoopOpt;
LoopOpt:
    ...
EndLoopOpt
    if (condition) goto LoopOpt;
```

We could do even better so, for each while loop, we verify if every variable used in the condition is assigned before the while and not inside an *IfStatement* or other *WhileStatement* (to consider only simple cases of assignments). In case it does, we perform the computation of the expression of the condition. If it evaluates to true, we now have the confirmation that at least the first iteration of the loop is executed, so we can remove the unnecessary `goto` before the label `LoopOpt` in the transformation of the **while** in a **do-while**.

Do While:
```ollir
LoopOpt:
    ...
    if (condition) goto LoopOpt;
```
### Register Allocation

The register allocation was performed method by method, making use of the method `buildCFGs` of the class `ClassUnit`. This method is used to build a control-flow graph in which each basic block corresponds to a single instruction. 

Next, we go through all the instructions and set the sets "use" and "def" for each one, according to the type of the instruction and the presence of a variable in it. 
A variable (not array access) on a left side of an assignment goes to the "def" set, the others go to the "use" set. 

Following this, we build the "in" and "out" sets applying the respective algorithm given in theoretical classes, iteratively.

After that, we were able to build the `LiveRange` for each variable by going through all the instructions of the method and initializing a new variable live range when a new variable comes out in an "out" set and updating the end of the range when the same variable appears in an "in" or "def" set whose instruction has a greater index on the ollir code. 

Given the live ranges (or webs) calculated, we continue the register allocation problem by building the interference graph between all the webs by identifying all the live ranges collisions. 

Then, we check the maximum number of registers pretended to be allocated by the user (*n*). 
If it is 0, we try to verify the availability to color the graph until it is possible, else we verify it with the number of registers required *n*.
The availability to color the graph is achieved by applying the heuristic that removes from the graph a node that has less than *n* edges and put it in a stack until the graph is empty. 
If the graph never comes empty we assume we can't color the graph with *n* registers.  

Finally, with all the nodes in the stack and ready to color it, we proceed with the coloring of the graph by assigning a register that does not collide with the interference webs already removed from the stack.

## Jasmin code generation

The Jasmin Code is generated using as reference the `ClassUnit` of the input ollir code. This way instead of having to deal with a complex string we can use the advantages that this class gives us regarding the organization and distinction of each ollir instruction and its attributes.

First are translated all the general information regarding the class like the **class name** and the **extended class**. Second, is iterated and translated all the class' **fields** (with the indication of its *access modifier* and if it is *static* or *final*). To avoid conflicts all the fields' names are in quotes. Third, the **constructor** of the class is added to the jasmin code. Finally, are iterated all the class' **methods** and made the translation of its signature, including *access modifiers*, *arguments*, and *return type*, as well as its instructions.

To translate each **instruction** is used a `BiFunctionClassMap` that associates each type of instruction with the corresponding function that is responsible to translate the specified instruction, like a visitor approach. These translations were done based on the methods available in the `ClassUnit` and with the support of the Jasmin Official documentation and Jasmin Bytecodes.

### Calculate Jasmin Limits

The limits are calculated for each one of the class methods:

* **locals**: corresponds to the highest number of the register used in the method's `VarTable` plus 1
* **stack**: keep track of the `stackCounter` and `stackLimit` while translating all the instructions of the method. Each instruction, if affects the stack size updates the `stackCounter` by decrementing or incrementing the stack by a given number. Whenever the `stackCounter` is incremented the `stackLimit` is updated always corresponding to the maximum size found. After a method is completed and translated the initial stack's limits assigned to 0 are replaced by the limit found by the algorithm.

### JVM efficient instructions

Regarding Jasmin's instructions were consider the following optimizations:

1. use `load_` and `store_` with registers between 0 and 3
2. use `const`, `bipush`, `sipush`, and `ldc` to load constants according to the most indicated one (include use `const_` with registers between 0 and 5)
3. use `iinc` instructions to simplify assignments like *a = a + 1*; *a = 1 + a*; *a = a - 1*
4. Optimize if statements:
    * use `iflt` and `ifgt` instead of `if_icmplt` and `if_icmpgt` when comparing with 0
    * label of the comparison corresponds to the label of the if
    * when it's possible to know the false label of an if simplify `&&` operation such as if the first fails it jumps to the false label

# PROS

* Our work tool has a complete semantic analysis, performing not only the required verifications but also others that we considered important, such as return type checking and the inability to use class fields in static methods.
* We implemented several optimizations that all together can hugely decrease the complexity of the final code generated, so it can be executed much faster.
* In addition to the tests provided by the teachers, we created more unit tests to test the various implemented features and optimizations to validate our code.
* All the possible cases of failures we could think of were successfully solved.

# CONS

* Although the number of tests performed is considerably high, we acknowledge that it is possible that not all cases are tested, and there may be flaws in those specific cases.
* In Register Allocation optimization we consider that a variable can have a single live range. If we could have several webs for a single variable, in some cases the minimum number of registers could be even less.
* Since we have implemented all the necessary features to get the maximum grade, we consider that we have no more cons in our project. However, a possible improvement would be, for example, to make our compiler accept method overloading.

# USE THE COMPILER:

## Required Software

For this project, you need to install [Java 11](https://jdk.java.net/), [Gradle 7](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Compile and Running

To compile and install the program, run `gradle installDist`. This will compile your classes and create a launcher script in the folder `./build/install/comp2022-1b/bin`. For convenience, there are two script files, one for Windows (`comp2022-1b.bat`) and another for Linux (`comp2022-1b`), in the root folder, that call this launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag `-x test`.

## Test

To test the program, run `gradle test`. This will execute the build, and run the JUnit tests in the `test` folder. If you want to see output printed during the tests, use the flag `-i` (i.e., `gradle test -i`).
You can also see a test report by opening `./build/reports/tests/test/index.html`.

## Run file jmm compiled

After executing the compiler with the command `./comp2022-1b [-r=<num>] [-o] [-d] -i=<input_file.jmm>`, if no errors occur the `.class` file generated from the file `input_file.jmm` will be saved in `./libs-jmm/compiled/` directory. 

To run the generated code you can run the command `java -cp ./libs-jmm/compiled/ <class_name>` from the root of the project, where *<class_name>* is the name of the class in the `input_file.jmm` file.
