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

        //For every token in our expression
        for (String token : postfix){
            //If the character is a number then parse it and add it to the stack
            //It is going to be used later
            if(Character.isDigit(token.charAt(0))){
                stack.add(Double.parseDouble(token));
            }

            //If the character is an operator then take two numbers out from the stack and use the operator on them
            else if (isOp(token)){
                //If there are less than two numbers in the stack then throw an error
                if(stack.size() < 2){
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }

                //Take out two numbers a and b
                double a = stack.pop();
                double b = stack.pop();

                //Apply the operator to get our result
                double res = applyOperator(token, a, b);
                stack.add(res);
            }
        }

        //Return the last element left in our stack after everything is done
        return stack.pop();
    }

    //Apply the operators
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

        has_enough_ops(infix);

        for(String token : infix){
            //If the token is a number then just add it to the result
            if(Character.isDigit(token.charAt(0))){
                result.add(token);
            }

            //Add Opening Brackets to the stack
            else if(token.equals("(")){
                stack.add(token);
            }
            //When you find a Closing Bracket then add everything to the result until the bracket closes
            else if(token.equals(")")){
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.add(stack.pop());
                }

                //If the entire stack gets emptied it means we didn't find a closing bracket
                //Throw a missing operator error
                if(stack.isEmpty()){
                    throw new IllegalArgumentException(MISSING_OPERATOR);
                }

                stack.pop(); // Remove '(' from stack
            }
            
            //If the token is an operator
            //Remove any higher/equal precedence tokens on the stack before you add it
            //Unless it's right associative like ^
            else if(isOp(token)){
                while(
                    !stack.isEmpty() && //Make sure the stack is not empty
                    !stack.peek().equals("(") && //If it's a bracket then stop
                    getPrecedence(token) <= getPrecedence(stack.peek()) && // The operator on the stack has higher/equal precedence
                    getAssociativity(token) == Assoc.LEFT //Only when it's left associative a.k.a not a ^
                ){
                    //Add the higher/equal precedence operator to the result
                    result.add(stack.pop());
                }
                //Add the current operator to the stack
                stack.add(token);
            }
        }

        //Remove all the remaining operators from the stack to the result
        while(!stack.isEmpty()){
            //all ( should have already been closed before we get here
            //So we throw an error if we encounter one
            if(stack.peek().equals("(")){
                throw new IllegalArgumentException(MISSING_OPERATOR);
            }
            result.add(stack.pop());
        }

        return result;
    }

    //Check if the expression has enough operands for its operators
    // for example 2 + 3 * 6, there is always one less 
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

        if(operands-1 < operators){
            throw new IllegalArgumentException(MISSING_OPERAND);
        }else if(operands-1 > operators){
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }

        return operands-1 == operators;
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
