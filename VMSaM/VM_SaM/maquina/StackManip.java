package maquina;

import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class StackManip {
   public void stackMan(String instruction, Stack<Integer> stack, HashMap<String, Integer> symbolTable, AtomicInteger heapPointer, Integer[] heapMemory, AtomicInteger framePointer){
      String[] parts = instruction.split("\\s+");
      String op = parts[0];
      switch(op) {
         case "PUSH" -> {
             int value = Integer.parseInt(parts[1]);
             stack.push(value);
           }

            case "POP" -> stack.pop();

                
            case "STORE" -> {
                String varName = parts[1];
               int value = stack.pop();
               symbolTable.put(varName, value);
           }
                
            case "LOAD" -> {
               String varName = parts[1];
               int value = symbolTable.get(varName);
               stack.push(value);
           }

            case "DUP" -> {
                Integer a = stack.pop();
                stack.push(a);
                stack.push(a);
           }


            case "SWAP" -> {
               Integer a = stack.pop();
               Integer b = stack.pop();
               stack.push(b);
               stack.push(a);
           }


            case "MALLOC" -> {
                if (stack.isEmpty()) {
                    throw new RuntimeException("Stack underflow in MALLOC");
                }
                int requestedSize = stack.pop();
                int totalSize = requestedSize + 1;
                if (heapPointer.get() + totalSize > heapMemory.length) {
                    throw new RuntimeException("Heap overflow in MALLOC");
                }
                heapMemory[heapPointer.get()] = totalSize;
                int userAddress = heapPointer.get()+ 1;
                heapPointer.set(heapPointer.get() + totalSize);
                stack.push(userAddress);
           }         

            case "PUSHIND" -> {
                if (stack.isEmpty()) {
                    throw new RuntimeException("Stack underflow in PUSHIND");
                }
                int address = stack.pop();
                int stackPosition = stack.size() - 1 - address;
                if (stackPosition < 0 || address >= stack.size()) {
                    throw new RuntimeException("Invalid memory address in PUSHIND: " + address);
                }
               int value = stack.get(stackPosition);
               stack.push(value);
           }

            case "STOREIND" -> {
                if(stack.size() < 2){
                    throw new RuntimeException("Stack underflow in STOREIND - need at least 2 elements");
                }
               int value = stack.pop();
               int m = stack.pop();
               int stackPosition = stack.size() - 1 - m;
               if (stackPosition < 0 || stackPosition >= stack.size()) {
                   throw new RuntimeException("Invalid stack index in STOREIND: " + m);
               }
               stack.set(stackPosition, value);
           }

            case "ADDSP" -> {
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
                    for (int i = 0; i < n; i++) {
                        stack.push(0);  
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
           }

            case "PUSHOFF" -> {
                int offsetPush = Integer.parseInt(parts[1]);
                int address = framePointer.get() + offsetPush;
                if(stack.size() <= address) {
                    while(stack.size() <= address) {
                        stack.push(0);
                    }
                }
                stack.push( stack.get(address));
           }
               
            case "STOREOFF" -> {
               int offset = Integer.parseInt(parts[1]);
               int address = framePointer.get() + offset;
               Integer a = stack.pop();
               if(stack.size() <= address) {
                   while(stack.size() <= address) {
                       stack.push(0);
                }
               }
               stack.set(address, a);
           }

      }
   }
}