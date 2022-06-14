# GROUP: comp2022-1b

| NAME | NR | GRADE | CONTRIBUTION |
| --- | --- | --- | --- |
| Henrique Ribeiro Nunes | 201906852 | 20 | 25% |
| José António Dantas Macedo | 201705226 | 20 | 25% |
| Margarida Assis Ferreira | 201905046 | 20 | 25% |
| Patrícia do Carmo Nunes Oliveira | 201905427 | 20 | 25% |

GLOBAL Grade of the project: 20

# SUMMARY:

This project's aim was to build a working compiler for the programming language “Java-minus-minus”.

It takes **.jmm files** as input and performs **syntactical** and **semantic analysis**, which is followed by the **generation of OLLIR** (OO-based Low-Level Intermediate Representation) code.

The compiler generates valid **JVM** (Java Virtual Machine) instructions in the jasmin format correspondent to OLLIR code generated, that can be optimized, which are then translated into Java bytecodes by the jasmin assembler.

## Command Line Arguments

```bash
./comp2022-1b [-r=<num>] [-o] [-d] -i=<input_file.jmm>
```

The program accepts 4 arguments where 3 of them are optional arguments:

* `-i` option specifies the input file with the extension **.jmm** to be compiled;
* `-o` option directs the compiler to perform code optimizations (default *false*);
* `-r` option directs the compiler to use only till a specific number of regiters (default *-1*);
* `-d` option enables additional output related to the compilation process, to help programmers understand the compiler's operation (default *false*).

# SEMANTIC ANALYSIS:

This compiler stage is responsible for validating the contents of the AST, verifying if the program is according to the definitions of the programming language, by ensuring and enforcing semantic rules and accordingly reporting the semantic errors associated with useful messages to the user.

Our tool implements the following semantic rules:

* Fields in static methods:
    1. Can't use class fields in a static method.
* Check undefined vars:
    1. The variables must be defined;
    2. The local variables must be initialized before being used.
* Check imports errors:
    1. Unable to find import;
    2. Return type not found;
    3. Type of variable not imported;
    4. Super class was not imported;
    5. Cannot find the object.
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
    10. Variable used is not declared;
    11. Built in types has no methods;
    12. Length is a property of an array.
* Function arguments:
    1. Incompatible types;
    2. The number of arguments does not match the number of parameters;
    3. The method does not exist in the class;
    4. Cannot call this object in a static method;
    5. Cannot call class' non static methods like static methods.
* Return checking:
    1. Check if the return expression matches the method return type.

# CODE GENERATION:

The code generation of our tool works based on a pipeline principle where the input of each stage corresponds to the output of the last one. This way all the main stages have to be executed in order to obtain the compiled code of the input code. If in any stage there are error reports associated the compilation stops and the user is informed accordingly.

**Compilation Stages**:

1. Jmm code parsing (Syntatic Analysis) and AST generation
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

Stages 4 and the first item of stage 6 only are executed when the compiler is called with the flag `-o`. The same way that the second item of the stage 6 is executed according to the value of the flag `-r`, or `-1` by default. Unlike stage 7 where the otimization is always considered.

**Possible Problems**:

* We consider that our code generation does not have any associated problems.

## Syntatic Analysis and AST generation

The `java--` language is represented and defined by the corresponding grammar created with specific rules to include all type of statements admissible by the original language.

There were taken in consideration and resolved the **Choice Conflict** and **Operator Priority** problems adjacents to such an complex language. In some cases, in order to resolve the first issue we use SCAN but never with more than 2 lookahead tokens.

The *AST* of the input *.jmm* file is generated based in this grammar that was annotated in a way that would remove and change unnecessary or redundant AST nodes (**AST Clean Up and Node Annotation**) in order to simplify the original AST, including the association of some atributes to a specific node.

## Symbol Table and Semantic Analysis

A `SymbolTable` is a implemented class that stores important information regarding the input .jmm file, so that it can be accessed easier. It includes the following informations:

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

To filled the `SymbolTable` is defined a **visitor** that extends a `SemanticAnalyserVisitor` that iterates the AST in *preorder*. In this case, to obtain the wanted information regarding the class, fields and methods the only nodes that need to be visited are: ***ImportStatement***, ***ClassDeclaration*** and ***MethodDeclaration***.

This is an important step before the **Semantic Analysis** as it centers important information about the code to be used together with the AST.

All the main items of the *semantic rules* defined in the first section were implemented by a *visitor*. To each one of the verifications was used the most convenient visitor and strategy to each task to perform. This analysis is always executed based on the `AST` and `SymbolTable` created beforehand.

## AST Optimization

### Constant folding

For this we created a *post-order visitor* that, when finds an arithmetic or logic expression checks if its children are integer literals  or a boolean literals (true or false), respectively. If this happens, the result of the expression is computed and the node is replaced with that same result.

### Constant propagation

For each method we create a constant propagation table as a map that has the name of the variables as keys and the correspondent constants, at the time, as values.

When an assignment like b = c, where c is a constant, is found, that information is stored in the propagation table.

The method also has a flag named `propagating` that defines if the propagation is to be performed or not. When `propagating` is true, if an assigment like b = c, where c is not a constant, appears, and c is in the propagation table, the c in the assigment is replaced by the corresponding constant.

In the case of while loops the loop is traversed twice:

* The first time with the `propagating` flag set to false, which means a variable b will be removed from the propagation table if it appears in an assignment like b = c, where c is not a constant.
* The second time with the `propagating` flag set to true, which means that when an assigment like b = c, where c is not a constant, appears, the value of c will be propagated to b.

### Dead Code Elimination

Here we use a pre-order visitor to visit while and if statements. If the condition of a while statement is false it removes the loop from the AST. If the condition of an if is true we replace de if statement by the code inside the if block, while if the condition is false, the code inside the else block is used.

***Note***: this three steps are executed in a loop until the **result stabilizes**, in order to achieve the maximum possible optimization.

## OLLIR code generation  

After AST validation by the semantic analysis phase, using visitors we walk through the AST (*top-down*) and generate the corresponding **OLLIR code** taking into consideration that this is a *3-address code* and making the necessary adjustements if needed.

## OLLIR code optimization

### While to Do-While

In order to perform this optimization we check, for each while loop, if every variable used in the condition is assigned in the basic block before the while (in order to considerer only simples cases of assignment). In case it does we perform the computation of the expression of the condition. If it evaluates to true, we now have the confirmation that at least the first iteration of the loop is executed, so we can remove an unecessary `goto` in the transformation of the **while** in a **do-while**.

### Register Allocation

The register allocation was performed method by method, making use of the method `buildCFG` of the class `ClassUnit`. This method is used build a control-flow graph in which each basic block corresponds to a single instruction. Next, we go through all the instructions and set the sets "use" and "def" for each one, according to the type of the instruction and the presence of a variable in it. A variable (not array access) in a left side of an assignment goes to the "def" set, the others go to the "use" set. Following this, we build the "in" and "out" sets applying the given algorithm iteratively. After that, we were able to build the `LiveRange` for each variable by going through all the instructions of the method and initializing a new variable live range when a new variable comes out in an "out" set and updating the end of the range when the same variable appears in an "in" set whose instruction has a greater index on the ollir code. Given the live ranges (aka webs) calculated, we continue the register allocation problem by building the interference graph between all the webs by identifying all the live ranges collisions. Then, we check for the maximum registers pretended to allocate by the user and if it is 0, we try to verify the availability to color the graph until it is possible, else we verify it with the number of registers required. Finally, with all the nodes in the stack and ready to color it, we proceed with the coloring of the graph by assigning a register that does not collide with the interference webs already removed from the stack.
 
## Jasmin code generation

The Jasmin Code is generated using as reference the `ClassUnit` of the input ollir code. This way instead of having to deal with a complex string we can use the advantages that this class gives us regarding the organization and distinction of each ollir instruction and its atributes.

First are translated all the general informations regarding the class like the **class name** and the **extended class**. Second are iterated and translated all the class' **fields** (with the indication of it's *access modifier* and if it is *static* or *final*). To avoid conflits all the fields' names are in quotes. Third the **constructor** of the class is added to the jasmin code. Finally, are iterated all the class' **methods** and make the translation of it's signature, including *access modifiers*, *arguments* and *return type*, as well as it's intructions.

To translate each **instruction** is used a `BiFunctionClassMap` that associated each type of instruction to the corresponding function that is responsable to translate the specified intrustuction, like a visitor approach. This translations were done based on the methods available in the `ClassUnit` and with the support of the Jasmin Official documentation and Jasmin Bytecodes.

### Calculate Jasmin Limits

The limits are calculated to each one of the class' methods:

* **locals**: corresponds to the highest number of the register used in the method's `VarTable` plus 1
* **stack**: keep track of the `stackCounter` and `stackLimit` while translating all the intructions of the method. Each instruction, if affects the stack size updates the `stackCounter` by decrementing or incrementing the stack by a given number. Whenever the `stackCounter` is incremented the `stackLimit` is updated always corresponding to the maximum size found. After a method is completed translated the initial stack's limits assigned to 0 is replaced by the limit found by the algorithm.

### JVM efficient instructions

Regarding the Jasmin' instructions were consider the following optimizations:

1. use `load_` and `store_` with registers between 0 and 3
2. use `const`, `bipush`, `sipush` and `ldc` to load constants according to the most indicated one (include use `const_` with registers between 0 and 5)
3. use `iinc` instructions to simplify assignments like *a = a + 1*; *a = 1 + a*; *a = a - 1*
4. Optimize if statements:
    * use `iflt` and `ifgt` instead of `if_icmplt` and `if_icmpgt` when comparin with 0
    * label of the comparision corresponds to the label of the if
    * when is possible to know the false label of an if simplify `&&` operation such as if the first fails it jumps to the false label

# PROS

* Our work tool has a complete semantic analysis, performing not only the required verifications but also others that we considered important, such as return type checking and the inability to use class fields in static methods.
* We implemented several optimizations including the replacement of while by do-while when the variables used in the while condition are assigned in the previous basic block and the condition is verified, which means the first iteration of the loop is always executed.
* In addition to the tests provided by the teachers, we created more unit tests to test the various implemented features and optimizations to validate our code.

# CONS

* Although the number of tests performed is considerably high, we acknowledge that it is possible that not all cases are tested, and there may be flaws in those specific cases.
* Since we have implemented all the necessary features to get the maximum grade, we consider that we have no more cons in our project. However, a possible improvement would be, for example, to make our compiler accept method overloading.

# USE THE COMPILER:

## Required Software

For this project, you need to install [Java 11](https://jdk.java.net/), [Gradle 7](https://gradle.org/install/), and [Git](https://git-scm.com/downloads/) (and optionally, a [Git GUI client](https://git-scm.com/downloads/guis), such as TortoiseGit or GitHub Desktop). Please check the [compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) for Java and Gradle versions.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-1b/bin``. For convenience, there are two script files, one for Windows (``comp2022-1b.bat``) and another for Linux (``comp2022-1b``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.
