package maquina; 

import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class SamInterpreter {
    //scanner para instruções de READ
    private static final Scanner scanner = new Scanner(System.in);
    // Pilha de operações
    private final Stack<Integer> stack;
    
    // Tabela de símbolos (variáveis)
    private HashMap<String, Integer> symbolTable;
    
    // Programa (lista de instruções)
    private String[] program;
    
    // Contador de programa
    AtomicInteger pc = new AtomicInteger(0);

    AtomicInteger heapPointer = new AtomicInteger(0); // Aponta para o próximo endereço livre na heap
    private Integer[] heapMemory; // Registra alocações (endereço -> tamanho)
    AtomicInteger framePointer = new AtomicInteger(0); //funciona como o FBR da maquina
     AtomicBoolean halt = new AtomicBoolean(false); //registrador HALT
    
    public SamInterpreter(String[] program, int heapSize) {
        this.stack = new Stack<>();
        this.symbolTable = new HashMap<>();
        this.program = program;
        this.heapMemory = new Integer[heapSize];
    }
    
    public void execute(IO io, Arithmetic_Logic al, RegisterManip rm, StackManip sm, Control con) {
        while (pc.get() < program.length && !halt.get()) {
            String instruction = program[pc.get()].trim();
            if(instruction.endsWith(":")){
               pc.set(pc.get() + 1);
               continue;
            }
            sm.stackMan(instruction, stack, symbolTable, heapPointer, heapMemory, framePointer);
            rm.regMan(instruction, stack, halt, framePointer);
            al.ariLog(instruction, stack);
            io.inputOutput(instruction, stack, scanner);
            con.control(instruction, stack, program, pc);
            pc.set(pc.get() + 1);
        }
    }
    
    
    public static void main(String[] args) {
        try{
        List<String> lines = Files.readAllLines(Paths.get("maquina/programa.txt"));
        String[] program = lines.toArray(new String[0]);
        
        SamInterpreter interpreter = new SamInterpreter(program, 1024);
        Arithmetic_Logic al = new Arithmetic_Logic();
        IO io = new IO();
        RegisterManip rm = new RegisterManip();
        StackManip sm = new StackManip();
        Control con = new Control();
        interpreter.execute(io, al, rm, sm, con);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
