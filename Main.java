import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.print( "file name: ");
        Scanner file_name  = new Scanner(System.in);
        //this var is our result
        String result = " ; ModuleID = 'stm2ir\ndeclare i32 @printf(i8*,...)\n@print.str = constant [4 x i8] c\"%d\\0A\\00\"\n\ndefine i32 @main() {";
        //this is last operation number.
        int lastOptNo = 1;
        java.io.File file = new java.io.File(file_name.next() + ".txt");
        Scanner input1 = new Scanner(file);
        Scanner input2 = new Scanner(file);
        //this arraylist store variable names afterwards we will use var names for printing allocate function.
        ArrayList<String> variables  = new ArrayList<>();

        //allocate edir
        while (input1.hasNext()){
            String a =input1.next();
            //this code will split sting to find variables
            String[] arr = a.split("[()=/*+-]");
            for (String ax:arr) {

                if (!isEmpty(ax)&&isVar(ax)&&!variables.contains(ax)){
                    result+="\n\t%" + ax + "= alloca i32";

                    variables.add(ax);
                }
            }



        }
        //
        while (input2.hasNextLine()){
            String girdi = input2.nextLine();
            result+=translator(girdi,lastOptNo)[0];
            int tempLastOpt=Integer.parseInt(translator(girdi,lastOptNo)[1]);
            // if last operation number changed this code will update last operation number
           if (tempLastOpt-lastOptNo>0) lastOptNo=tempLastOpt;
        }
        result+="\n\tret i32 0\n}";

        System.out.println(result);
        // for creating txt file
        try (PrintWriter out = new PrintWriter("result.txt")) {
            out.println(result);

        }


    }
    //this method will convert infix to postfix
    public static String postFix(String infix){
        String result = "";
        Stack<Character> operators = new Stack<>();
        //we converting infix line to character array and checking for opperands and operators.
        char[] chars = infix.toCharArray();
        for (int i  =0 ; i<chars.length;i++ ){
            //if current character not opperand it will append to result string
            if (chars[i]=='+'||chars[i]=='-'||chars[i]=='(' ||chars[i]=='*'||chars[i]=='/'){
                operators.push(chars[i]);
            }
            //if current character is closing parenthesis then it will pop all operators to result array.
            else if(chars[i]==')'){

                while (!operators.isEmpty() ) {
                    char k = operators.pop();
                    if (k=='(') continue;
                    result+=" ";
                    result+=String.valueOf(k);
                    result+=" ";

                }
            }
            //if following character is operator then this code crate spaces between numbers. Ex : 99 5 +
            else if (i!=chars.length-1&&(chars[i+1]=='+'||chars[i+1]=='-'||chars[i+1]=='(' ||chars[i+1]=='*'||chars[i+1]=='/')){
                result+=String.valueOf(chars[i]);
                result+=" ";
            }
            else result+=String.valueOf(chars[i]);
            // if following character is operator and last operator was  * or / then this code will append  * or / to result array
            if ((i!=chars.length-1&&!operators.isEmpty())&&(chars[i+1]=='+'||chars[i+1]=='-'||chars[i+1]=='*'||chars[i+1]=='/')&&(operators.peek()=='*'||operators.peek()=='/')){
                char k = operators.pop();
                result+=" ";
                result+=String.valueOf(k);
                result+=" ";
            }
            // at end if operator stack is not empty following code append them to result sting
            if (i==chars.length-1&&!operators.isEmpty()){
                while (!operators.isEmpty() ) {
                    char k = operators.pop();
                    if (k=='(') continue;
                    result+=" ";
                    result+=String.valueOf(k);
                }
            }



        }
        return result;
    }
    //this method will translate every line of code. if only var name written or there is opration but now assigmnet  then it will print

    public static String[] translator(String line, Integer opeationNumber){
        // this dictionary will store ir instruction of corresponding binary operator
        Map<String,String> operatorNames = new HashMap<>();
        operatorNames.put("+","add");
        operatorNames.put("-","sub");
        operatorNames.put("/","udiv");
        operatorNames.put("*","mul");
        // this array's first item is ir instruction, second item is last operation number
        String[] resultArray= new String[2];
        String result = "";
        //if line contains operator then this code will be executed
        if (line.contains("*")||line.contains("+")||line.contains("-")||line.contains("/")){
            String postfix = "";
            // if there is an assignment then String will be divided by '=' and right part will be transfer to postfix by postFix() method
            if (line.contains("=")){
                String[] arr=line.split("=");
                postfix = postFix(arr[1]);
            }
            else {
                postfix  = postFix(line);
            }

            //we are creating operands stack for binary operations
            Stack<String> operands  = new Stack<>();
             // this hashmap will store variables and corresponded temporary variable number. Ex = %x1 , %2
            Map<String,Integer> varNames = new HashMap<>();
            //Splitting postfix string for individual operation
            String[] postFixArr = postfix.split(" ");


            //After converting to postfix we execute code below for write IR instruction.
            for (int i = 0;i<postFixArr.length;i++){

                // if current string is variable then code generate IR instruction for load.
                // then code below will store temporary variable name and variable name in varNames hasmap and push variable name to operands stack.
                if (isVar(postFixArr[i])&&!isEmpty(postFixArr[i])){
                    result+="\n\t%" + opeationNumber+" = "+"load i32* %" + postFixArr[i];
                    varNames.put(postFixArr[i],opeationNumber);
                    operands.push(postFixArr[i]);
                    opeationNumber++;
                }
                // if it is number then code below will push number to operands stack
                else if (!isEmpty(postFixArr[i])&&!isOperator(postFixArr[i])) {
                    operands.push(postFixArr[i]);
                }
                //if current string is operator then this loop will be executed
                else if (isOperator(postFixArr[i])){

                    //because binary operations require two operand we pop two of them for operation
                    // after operation we push incremented lastOptNumber with prefix '%'
                    String firstOperand = operands.pop();

                    String secondOperand = operands.pop();
                    // if both of the opperands are variable then we will get assigned temporary variable number from varNames arrayList
                    if (isVar(firstOperand)&&isVar(secondOperand)){
                        result+="\n\t%" + opeationNumber+" = " + operatorNames.get(postFixArr[i]) + " i32 %" + varNames.get(secondOperand) + " , %" + varNames.get(firstOperand);
                        operands.push("%"+opeationNumber);

                        opeationNumber++;
                    }
                    //if one of them is variable we will put '%' prefix to variable.
                    else if (isVar(firstOperand)||isVar(secondOperand)){
                        if (isVar(firstOperand)){
                            result+="\n\t%" + opeationNumber+" = " + operatorNames.get(postFixArr[i]) +" i32 " +secondOperand+ ",  %"+ varNames.get(firstOperand) ;
                            operands.push("%" + opeationNumber);
                            opeationNumber++;
                        }
                        else{
                            result+="\n\t%" + opeationNumber+" = " + operatorNames.get(postFixArr[i]) + " i32  %" + varNames.get(secondOperand) +",  "  +firstOperand ;
                            operands.push("%" + opeationNumber);
                            opeationNumber++;

                        }
                    }
                    //if both of them number then this code will be executed
                    else {
                        result+="\n\t%" +opeationNumber+" = " + operatorNames.get(postFixArr[i]) + " i32 " + secondOperand + " , " + firstOperand;

                        operands.push("%" + opeationNumber);
                        opeationNumber++;

                    }

                }


            }
            //if there is an assigment then we will add store to our result string
            if (line.contains("=")){
                String[] arr=line.split("=");
                result+="\n\tstore i32 %" + (opeationNumber-1)+" , i32* %" + arr[0];

            }
            //if line not have '=' then we will print result
            else if (!line.contains("=")){
                result+="\n\tcall i32(i8*,...)* @printf(i8*getelementptr([4 x i8]* @print.str, i32 0, i32 0), i32 %"+(opeationNumber-1) + ")";
                opeationNumber++;
            }

        }
        //if line contains assignment but not operations then we will store value for variable
        else if (line.contains("=")){
            String[] arr=line.split("=");
            result+="\n\tstore i32 "+ arr[1] +", i32* %"+arr[0];
        }
        //if there is only name of variable then we will add instruction for print value of variable
        else if (isVar(line)&&!isEmpty(line)){
            result+="\n\t%"+opeationNumber+" = load i32* %"+line;
            result+="\n\tcall i32(i8*,...)* @printf(i8*getelementptr([4 x i8]* @print.str, i32 0, i32 0), i32 %"+opeationNumber + ")";

            opeationNumber+=2;
        }
        //result string
        resultArray[0] = result;
        //last operation number
        resultArray[1] = String.valueOf(opeationNumber);
        return resultArray;
    }
    //method below check for string is variable or not
    public static boolean isVar(String s){

        if (s.equals("*")||s.equals("-")||s.equals("/")||s.equals("+")||s.contains("%")) return false;
        try {
           int a= Integer.valueOf(s);
           if (a<=0||a>0) return false;
        } catch (NumberFormatException e) {

            return true;
        }
        return false;
    }
    //method below check for string is operator or not
    public static boolean isOperator(String s){
        if (s.equals("*")||s.equals("-")||s.equals("/")||s.equals("+"))return true;
        return false;
    }

    //method below check for string is empty or not
    public static boolean isEmpty(String s){

        char[] kontrol = s.toCharArray();
        if (kontrol.length==0||kontrol[0]==' ') return true;
        return false;
    }
}