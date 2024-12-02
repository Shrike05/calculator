import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;

/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        Stack<Double> stack = new Stack<Double>();

        for (String token : postfix){
            if(Character.isDigit(token.charAt(0))){
                stack.add(Double.parseDouble(token));
            }

            else if (isOp(token)){
                if(stack.size() < 2){
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
                double a = stack.pop();
                double b = stack.pop();

                double res = applyOperator(token, a, b);
                stack.add(res);
            }
        }

        return stack.pop();
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------
    boolean isOp(String c){
        return c.equals("+") || 
            c.equals("-") || 
            c.equals("*") || 
            c.equals("/") || 
            c.equals("^");
    }
    
    List<String> infix2Postfix(List<String> infix) {
        List<String> result = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();

        if(!has_enough_ops(infix)){
            throw new IllegalArgumentException(MISSING_OPERATOR); 
        }

        for(String token : infix){
            //If the token is a number then just add it to the result
            if(Character.isDigit(token.charAt(0))){
                result.add(token);
            }

            //Opening Bracket
            else if(token.equals("(")){
                stack.add(token);
            }
            //Closing Bracket
            else if(token.equals(")")){
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.add(stack.pop());
                }

                if(stack.isEmpty()){
                    throw new IllegalArgumentException(MISSING_OPERATOR);
                }

                stack.pop(); // Remove '(' from stack
            }
            
            //Is an operator
            else{
                while(
                    !stack.isEmpty() &&
                    !stack.peek().equals("(") &&
                    getPrecedence(token) <= getPrecedence(stack.peek()) &&
                    getAssociativity(token) == Assoc.LEFT
                ){
                    result.add(stack.pop());
                }
                stack.add(token);
            }
        }

        //Remove all the remaining operators from the stack
        while(!stack.isEmpty()){
            if(stack.peek().equals("(")){
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
            result.add(stack.pop());
        }

        return result;
    }

    boolean has_enough_ops(List<String> infix){
        int operands = 0;
        int operators = 0;
        for(String i : infix){
            if(isOp(i)){
                operators++;
            }else if( Character.isDigit(i.charAt(0)) ){
                operands++;
            }
        }

        return operands-1 <= operators;
    }

    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else if ("()".contains(op)) {
            return -1;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)
    List<String> tokenize(String expr) {
        List<String> result = new ArrayList<String>();

        String num = "";
        for(int i = 0; i < expr.length(); i++){
            char c = expr.charAt(i);
            //If we are at the end then add whatever is left and leave
            if(i == expr.length()-1){
                if(Character.isDigit(c)){
                    num += c;
                }
                if(num != ""){
                    result.add(num);
                    num = "";
                }
            }

            //If we have found a number then keep track of it and continue
            if(Character.isDigit(c)){
                num += c;                
                continue;
            }

            //If we haven't found a number but there were numbers previously then add em'
            else if(num != ""){
                result.add(num);
                num = "";
            }

            //If the character is any of these symbols then add em'
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == '^') {
                result.add(String.valueOf(c));
            }
        }

        return result;
    }

}
