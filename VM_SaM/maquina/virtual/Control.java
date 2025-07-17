package virtual;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class Control{
    private int findLabelLine(String[] program, String label){
        for (int i = 0; i < program.length; i++){
            if (program[i].equals(label + ":")){
                return i;
            }
        }
        throw new RuntimeException("Label not found: " + label);
    }
    public void control(String instruction, Stack<Integer> stack, String[] program, AtomicInteger pc){
      String[] parts = instruction.split("\\s+");
      String op = parts[0];
      Integer a;
      switch(op) {
            case "JUMP" -> {
                String label = parts[1];
                int target = findLabelLine(program, label);
                pc.set(target - 1); // -1 porque pc++ vai acontecer depois
            }

            case "JUMPC" -> {
                String label = parts[1];
                int target = findLabelLine(program, label);
                if(stack.pop() != 0) {
                    pc.set(target - 1);
                }
            }   
                
            case "JUMPIND" -> {
                if (stack.isEmpty()) {
                    throw new RuntimeException("Stack underflow in JUMPIND");
                }
                a = stack.pop();
                if (a < 0 || a >= program.length) {
                    throw new RuntimeException("Invalid jump target in JUMPIND: " + a);
                }
                pc.set(a - 1); // -1 porque o pc++ ocorre após a instrução
            }

            case "JSR" -> {
                stack.push(pc.get() + 1);
                int target = Integer.parseInt(parts[1]);
                pc.set(target - 1);
            }
               
            case "JSRIND" -> {
                if (stack.isEmpty()){
                    throw new RuntimeException("Stack underflow in JSRIND");
                }int target = stack.pop();
                stack.push(pc.get() + 1);
                if(target < 0 || target >= program.length){
                    throw new RuntimeException("Invalid jump target in JSRIND: " + target);
                }pc.set(target - 1);
            }

            case "SKIP" -> {
                a = stack.pop();
               pc.set(pc.get() + a);
            }   

      }
    }
}