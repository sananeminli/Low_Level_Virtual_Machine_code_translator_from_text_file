# LLVM_translator_from_text_file
This code is a translator that converts the expressions written in infix notation into postfix notation, and then generates the LLVM IR code from it.
# How to Use
You will need to have a .txt file that contains expressions written in infix notation.
To run the code, write java -jar stmr2.jar from command line.
When you run the code, it will prompt you for the file name. Enter the file name without the extension. For example, if your file name is expressions.txt, enter expressions and hit Enter.
The code will generate the LLVM IR code and print it on the console. It will also create a result.txt file in the same directory as the input file, and write the LLVM IR code to that file.
For running LLVM code you could use https://kripken.github.io/llvm.js/demo.html site.
# How it Works
The code first reads the input file and converts the expressions in infix notation to postfix notation using the postFix method.
The code then generates the LLVM IR code from the postfix notation using the translator method.
The LLVM IR code is printed on the console and written to the result.txt file.



