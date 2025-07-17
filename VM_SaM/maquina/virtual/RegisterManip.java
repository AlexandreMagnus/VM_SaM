package virtual;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterManip {
    public void regMan (String instruction, Stack<Integer> stack, AtomicBoolean halt, AtomicInteger framePointer){
        String[] parts = instruction.split("\\s+");
        String op = parts[0];
        Integer a;
        switch (op){
            case "PUSHSP" -> stack.push(stack.size());
               
            case "POPSP" -> {
                int newTop = stack.pop();
                while(stack.size() > newTop){
                    stack.pop();
                }
            }

            case "PUSHFBR" -> stack.push(framePointer.get());
               
            case "POPFBR" -> {
                a = stack.pop();
                framePointer.set(a);
            }
               
            case "LINK" -> {
                stack.push(framePointer.get());
                framePointer.set(stack.size() - 1);
            }  
                
            case "STOP" -> halt.set(true);
        }

    }
}