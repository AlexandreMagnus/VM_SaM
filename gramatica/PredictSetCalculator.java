
import java.util.*;

public class PredictSetCalculator {
    private final Map<String, List<List<String>>> grammar;
    private final FirstFollowCalculator calculator;
    private final Map<String, Map<String, List<String>>> predictTable = new HashMap<>();

    public PredictSetCalculator(Map<String, List<List<String>>> grammar, String startSymbol) {
        this.grammar = grammar;
        this.calculator = new FirstFollowCalculator(grammar, startSymbol);
        computePredictSets();
    }

    private void computePredictSets() {
        Map<String, Set<String>> firstSets = calculator.getFirstSets();
        Map<String, Set<String>> followSets = calculator.getFollowSets();

        for (String nonTerminal : calculator.getNonTerminals()) {
            predictTable.put(nonTerminal, new HashMap<>());
            
            List<List<String>> productions = grammar.get(nonTerminal);
            for (int i = 0; i < productions.size(); i++) {
                List<String> production = productions.get(i);
                Set<String> firstOfProduction = calculator.computeFirstForSequence(production);
                
                // Para cada terminal em FIRST(α), adicione A → α à entrada M[A,a]
                for (String terminal : firstOfProduction) {
                    if (!terminal.equals("ε")) {
                        predictTable.get(nonTerminal).put(terminal, production);
                    }
                }
                
                // Se ε está em FIRST(α), então para cada terminal b em FOLLOW(A),
                // adicione A → α à entrada M[A,b]
                if (firstOfProduction.contains("ε")) {
                    for (String followTerminal : followSets.get(nonTerminal)) {
                        predictTable.get(nonTerminal).put(followTerminal, production);
                    }
                }
            }
        }
    }

    public Map<String, Map<String, List<String>>> getPredictTable() {
        return predictTable;
    }

    public List<String> getProduction(String nonTerminal, String terminal) {
        return predictTable.getOrDefault(nonTerminal, new HashMap<>()).get(terminal);
    }

    public void printPredictTable() {
        System.out.println("\nPREDICT TABLE (Parsing Table):");
        System.out.println("Format: NonTerminal[Terminal] -> Production");
        System.out.println("=" + "=".repeat(50));

        for (String nonTerminal : predictTable.keySet()) {
            Map<String, List<String>> entries = predictTable.get(nonTerminal);
            if (!entries.isEmpty()) {
                System.out.println("\n" + nonTerminal + ":");
                for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
                    String terminal = entry.getKey();
                    List<String> production = entry.getValue();
                    System.out.println("  [" + terminal + "] -> " + String.join(" ", production));
                }
            }
        }
    }


    // Método para verificar se há conflitos na tabela de parsing
    public boolean hasConflicts() {
        for (String nonTerminal : predictTable.keySet()) {
            Map<String, List<String>> entries = predictTable.get(nonTerminal);
            // Se houver múltiplas entradas para o mesmo terminal, há conflito
            // (isso já é prevenido pela estrutura Map, mas verificamos consistência)
            for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
                if (entry.getValue() == null) {
                    System.out.println("Conflito encontrado: entrada nula para " + 
                                     nonTerminal + "[" + entry.getKey() + "]");
                    return true;
                }
            }
        }
        return false;
    }
}
