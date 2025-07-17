package maquina; 

import java.util.Scanner;
import java.util.Stack;

public class IO {
    public void inputOutput(String instruction, Stack<Integer> stack, Scanner scanner) {
        String[] parts = instruction.split("\\s+");
        String op = parts[0];

         switch (op) {
            case "PRINT" -> System.out.println(stack.peek());
                
            case "READ" -> {
                System.out.print("Digite um número: ");
                int input = scanner.nextInt();
                stack.push(input);
            }

            case "READC" -> {
                System.out.print("Digite um caractere: ");
                char c = scanner.next().charAt(0);
                stack.push((int) c);
            }

            case "READF" -> {
                System.out.print("Digite um float: ");
                float f = scanner.nextFloat();
                stack.push((int) f);
            }


            case "PRINTLN" -> System.out.println(stack.peek());

            case "PRINTC" -> System.out.print((char) stack.peek().intValue());

            case "DUMP" -> System.out.println("Stack: " + stack);
   
            }

         }
    }
        
  




