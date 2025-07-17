package virtual;

import java.util.Objects;
import java.util.Stack;

public class Arithmetic_Logic {
   

   public void ariLog(String instruction, Stack<Integer> stack) {
        String[] parts = instruction.split("\\s+");
        String op = parts[0];
        Integer a;
        Integer b;
        
        switch (op) {
                
            case "ADD" -> {
             a = stack.pop();
             b = stack.pop();
                stack.push(a + b);
           }
                
            case "SUB" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(b - a);
           }

                
            case "TIMES" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(a * b);
           }
                
            case "DIV" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(b / a);
           }

            case "MOD" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(b % a);
           }

            case "LSHIFT" -> {
                a = stack.pop();
                b = Integer.valueOf(parts[1]);
               stack.push(a << b);
           }

            case "RSHIFT" -> {
                a = stack.pop();
                b = Integer.valueOf(parts[1]);
               stack.push(a >> b);
           }    
            
            case "NOT" -> {
                a = stack.pop();
               stack.push(a == 0 ? 1:0);
           }

            case "OR" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push((a != 0 && b != 0) ? 1 : 0);
           }

            case "AND" -> {
                b = stack.pop();
                a = stack.pop();
               stack.push((a != 0 && b != 0) ? 1 : 0);
           }   

            case "XOR" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(((a == 0 && b != 0) || (a != 0 && b == 0)) ? 1 : 0);
           } 
               
            case "NAND" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push((a != 0 && b != 0) ? 0 : 1);
           }

            case "BITNOT" -> {
                a = stack.pop();
               stack.push(~a);
           }   
             
            case "BITAND" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(a & b);
           }

            case "BITOR" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(a | b);
           } 

            case "BITXOR" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(a ^ b);
           } 

            case "BITNAND" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(~(a & b));
           }

            case "GREATER" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(b > a ? 1:0);
           }

            case "LESS" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(b < a ? 1:0);
           }

            case "EQUAL" -> {
                a = stack.pop();
                b = stack.pop();
               stack.push(Objects.equals(a, b) ? 1:0);
           }  

            case "ISNIL" -> {
                a = stack.pop();
               stack.push(a == 0 ? 1:0);
           }      

            case "ISPOS" -> {
                a = stack.pop();
               stack.push(a > 0 ? 1:0);
           } 

            case "ISNEG" -> {
                a = stack.pop();
               stack.push(a < 0 ? 1:0);
           }

            case "CMP" -> {
                a = stack.pop();
                b = stack.pop();
               if(b < a){
                   stack.push(-1);
               }else if(Objects.equals(a, b)){
                   stack.push(0);
               }else{
                   stack.push(1);
               }
           }   
        }
       //operações lógicas e aritméticas:
          }
}