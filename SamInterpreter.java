package maquina;

import java.util.HashMap;
import java.util.Stack;
import java.util.Scanner;

public class SamInterpreter {
    //scanner para instruções de READ
    private static final Scanner scanner = new Scanner(System.in);
    // Pilha de operações
    private Stack<Integer> stack;
    
    // Tabela de símbolos (variáveis)
    private HashMap<String, Integer> symbolTable;
    
    // Programa (lista de instruções)
    private String[] program;
    
    // Contador de programa
    private int pc;

    private int heapPointer; // Aponta para o próximo endereço livre na heap
    private int[] heapMemory; // Registra alocações (endereço -> tamanho)
    private int framePointer; //funciona como o FBR da maquina
    private boolean halt; //registrador HALT
    
    public SamInterpreter(String[] program, int heapSize) {
        this.stack = new Stack<>();
        this.symbolTable = new HashMap<>();
        this.program = program;
        this.pc = 0;
        this.heapPointer = 0;
        this.heapMemory = new int[heapSize];
        this.framePointer = 0;
        this.halt = false;
    }
    
    public void execute() {
        while (pc < program.length && !halt) {
            String instruction = program[pc];
            processInstruction(instruction);
            pc++;
        }
    }
    
    private void processInstruction(String instruction) {
        String[] parts = instruction.split("\\s+");
        String op = parts[0];
        
        switch (op) {
            //operações lógicas e aritméticas:
                
            case "ADD":
                int a = stack.pop();
                int b = stack.pop();
                stack.push(a + b);
                break;
                
            case "SUB":
                a = stack.pop();
                b = stack.pop();
                stack.push(b - a);
                break;

                
            case "TIMES":
                a = stack.pop();
                b = stack.pop();
                stack.push(a * b);
                break;
                
            case "DIV":
                a = stack.pop();
                b = stack.pop();
                stack.push(b / a);
                break;

            case "MOD":
                a = stack.pop();
                b = stack.pop();
                stack.push(b % a);
                break;

            case "LSHIFT":
                a = stack.pop();
                b = Integer.parseInt(parts[1]);
                stack.push(a << b);
                break;

            case "RSHIFT":
                a = stack.pop();
                b = Integer.parseInt(parts[1]);
                stack.push(a >> b);
                break;    
            
            case "NOT":
               a = stack.pop();
               stack.push(a == 0 ? 1:0);    
               break;

            case "OR":
               a = stack.pop();
               b = stack.pop();
               stack.push((a != 0 && b != 0) ? 1 : 0);
               break;

            case "AND":
               b = stack.pop();
               a = stack.pop();
               stack.push((a != 0 && b != 0) ? 1 : 0);
               break;   

            case "XOR":
               a = stack.pop();
               b = stack.pop();
               stack.push(((a == 0 && b != 0) || (a != 0 && b == 0)) ? 1 : 0);
               break; 
               
            case "NAND":
               a = stack.pop();
               b = stack.pop();
               stack.push((a != 0 && b != 0) ? 0 : 1);   
               break;

            case "BITNOT":
               a = stack.pop();
               stack.push(~a);    
               break;   
             
            case "BITAND":
               a = stack.pop();
               b = stack.pop();
               stack.push(a & b);
               break;

            case "BITOR":
               a = stack.pop();
               b = stack.pop();
               stack.push(a | b);
               break; 

            case "BITXOR":
               a = stack.pop();
               b = stack.pop();
               stack.push(a ^ b);
               break; 

            case "BITNAND":
               a = stack.pop();
               b = stack.pop();
               stack.push(~(a & b));
               break;

            case "GREATER":
               a = stack.pop();
               b = stack.pop();
               stack.push(b > a ? 1:0);
               break;

            case "LESS":
               a = stack.pop();
               b = stack.pop();
               stack.push(b < a ? 1:0);
               break;

            case "EQUAL":
               a = stack.pop();
               b =  stack.pop();
               stack.push(a == b ? 1:0);
               break;  

            case "ISNIL":
               a = stack.pop();
               stack.push(a == 0 ? 1:0);
               break;      

            case "ISPOS":
               a = stack.pop();
               stack.push(a > 0 ? 1:0);
               break; 

            case "ISNEG":
               a = stack.pop();
               stack.push(a < 0 ? 1:0);
               break;

            case "CMP":
               a = stack.pop();
               b = stack.pop();
               if(b < a){
                stack.push(-1);
               }else if(a == b){
                stack.push(0);
               }else{
                stack.push(1);
               }
               break;   
            

            //operações de manipulação de pilha: 
            
            case "PUSH":
                int value = Integer.parseInt(parts[1]);
                stack.push(value);
                break;

            case "POP":
                stack.pop();
                break;

                
            case "STORE":
                String varName = parts[1];
                value = stack.pop();
                symbolTable.put(varName, value);
                break;
                
            case "LOAD":
                varName = parts[1];
                value = symbolTable.get(varName);
                stack.push(value);
                break;

            case "DUP":
               a = stack.pop();
               stack.push(a);
               stack.push(a);
               break;

            case "SWAP":
               a = stack.pop();
               b = stack.pop();
               stack.push(b);
               stack.push(a);
               break;

            case "MALLOC":
                if (stack.isEmpty()) {
                   throw new RuntimeException("Stack underflow in MALLOC");
                }
                int requestedSize = stack.pop();
                int totalSize = requestedSize + 1;
                if (heapPointer + totalSize > heapMemory.length) {
                   throw new RuntimeException("Heap overflow in MALLOC");
                }
                heapMemory[heapPointer] = totalSize;
                int userAddress = heapPointer + 1;
               heapPointer += totalSize;
               stack.push(userAddress);
              
                break;         

            case "PUSHIND":
                if (stack.isEmpty()) {
                   throw new RuntimeException("Stack underflow in PUSHIND");
                }
    
               int address = stack.pop();
               int stackPosition = stack.size() - 1 - address;
               if (stackPosition < 0 || address >= stack.size()) {
                   throw new RuntimeException("Invalid memory address in PUSHIND: " + address);
                }
                value =stack.get(stackPosition);
                stack.push(value);

                break;  

            case "STOREIND":
                if(stack.size() < 2){
                    throw new RuntimeException("Stack underflow in STOREIND - need at least 2 elements");
                }
                value = stack.pop();
                int m = stack.pop();     
                stackPosition = stack.size() - 1 - m;  
                if (stackPosition < 0 || stackPosition >= stack.size()) {
                   throw new RuntimeException("Invalid stack index in STOREIND: " + m);
                }
                stack.set(stackPosition, value);
                break;

            case "ADDSP":
                if (parts.length != 2) {
                   throw new RuntimeException("Syntax error: ADDSP requires exactly one operand");
                }
                int n;
                try {
                    n = Integer.parseInt(parts[1]);
                }catch (NumberFormatException e) {
                     throw new RuntimeException("Invalid integer operand for ADDSP: " + parts[1]);
                 }
                if (n > 0) {
                   if (n > 1000) {
                      throw new RuntimeException("ADDSP operand too large: " + n);
                   }
                   for (int i = 0; i < n; i++) {
                      stack.push(0);  // Poderia ser null dependendo da implementação
                   }
                } 
                else if (n < 0) {
                int toRemove = -n;
                if (stack.size() < toRemove) {
                   throw new RuntimeException(
                   String.format("Cannot remove %d elements from stack of size %d", 
                   toRemove, stack.size()));
                }
                for (int i = 0; i < toRemove; i++) {
                    stack.pop();
                }
                }
                break;

            case "PUSHOFF":
               int offsetPush = Integer.parseInt(parts[1]);
               int valuePush = stack.get(framePointer  + offsetPush);
               stack.push(valuePush);
               break;
               
            case "STOREOFF":
            int offsetStore = Integer.parseInt(parts[1]);
               a = stack.pop();
               int storeAddress = stack.get(framePointer + offsetStore);
               stack.set(storeAddress, a);  
               break;

            //instruções de manipulação de registradores:

            case "PUSHSP":
               stack.push(stack.size());
               break;
               
            case "POPSP":
               int newTop = stack.pop();
               while(stack.size() > newTop){
                stack.pop();
               }
               break;

            case "PUSHFBR":
               stack.push(framePointer);
               break;
               
            case "POPFBR":
               a = stack.pop();
               framePointer = a; 
               break;
               
            case "LINK":
                stack.push(framePointer);
                framePointer = stack.size() - 1;
                break;  
                
            case "STOP":
               halt = true;
               break;
                    


            //operações de controle:    
                
            case "JUMP":
                int target = Integer.parseInt(parts[1]);
                pc = target - 1; // -1 porque pc++ vai acontecer depois
                break;

            case "JUMPC":
                target = Integer.parseInt(parts[1]);
                a = stack.pop();
                if(a != 0){
                  pc = target - 1;
                }
                break;   
                
            case "JUMPIND":
               if (stack.isEmpty()) {
                  throw new RuntimeException("Stack underflow in JUMPIND");
               }
               a = stack.pop();
               if (a < 0 || a >= program.length) {
                  throw new RuntimeException("Invalid jump target in JUMPIND: " + a);
               }
               pc = a - 1; // -1 porque o pc++ ocorre após a instrução
               break;

            case "JSR":
               stack.push(pc + 1);
               target = Integer.parseInt(parts[1]);
               pc = target - 1;
               break;
               
            case "JSRIND":
               if (stack.isEmpty()){
                  throw new RuntimeException("Stack underflow in JSRIND");
               } 
               target = stack.pop();
               stack.push(pc + 1);
               if(target < 0 || target >= program.length){
                  throw new RuntimeException("Invalid jump target in JSRIND: " + target);
               }  
               pc = target - 1;
               break;

            case "SKIP":
               a = stack.pop();
               pc = pc + a;
               break;   


            //operações de I/O:    
                
            case "PRINT":
                System.out.println(stack.peek());
                break;
                
            case "READ":
                System.out.print("Digite um número: ");
                int input = scanner.nextInt();
                stack.push(input);
                break;

            case "READC":
               System.out.print("Digite um caractere: ");
               char c = scanner.next().charAt(0);
               stack.push((int) c);
               break;

            case "READF":
               System.out.print("Digite um float: ");
               float f = scanner.nextFloat();
               stack.push((int) f);
               break;


            case "PRINTLN":
               System.out.println(stack.peek());
               break;

            case "PRINTC":
               System.out.print((char) stack.peek().intValue());
               break;

            case "DUMP":
               System.out.println("Stack: " + stack);
               break;
   
   
    

            default:
                throw new RuntimeException("Instrução desconhecida: " + op);
        }
    }
    
    public static void main(String[] args) {
        // Exemplo de programa SaM
        String[] program = {
            "PUSH 4",
            "JUMPIND",
            "PUSH 99",
            "PRINT",
            "PUSH 123",
            "PRINT"

        };
        
        SamInterpreter interpreter = new SamInterpreter(program, 1024);
        interpreter.execute();
    }
}