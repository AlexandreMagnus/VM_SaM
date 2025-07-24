
import java.util.*;
import java.util.stream.Collectors;

public class FirstFollowCalculator {
    private final Map<String, List<List<String>>> grammar;
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final String startSymbol;
    private final Map<String, Set<String>> firstSets = new HashMap<>();
    private final Map<String, Set<String>> followSets = new HashMap<>();

    public FirstFollowCalculator(Map<String, List<List<String>>> grammar, String startSymbol) {
        this.grammar = grammar;
        this.startSymbol = startSymbol;
        this.nonTerminals = grammar.keySet();
        this.terminals = findTerminals();
        computeFirstSets();
        computeFollowSets();
    }

    // Encontra todos os terminais na gramática
    private Set<String> findTerminals() {
        Set<String> terms = new HashSet<>();
        for (List<List<String>> productions : grammar.values()) {
            for (List<String> production : productions) {
                for (String symbol : production) {
                    if (!grammar.containsKey(symbol) && !symbol.equals("ε")) {
                        terms.add(symbol);
                    }
                }
            }
        }
        return terms;
    }

    // Calcula os conjuntos FIRST
    private void computeFirstSets() {
        // Inicializa FIRST para todos os não-terminais
        nonTerminals.forEach(nt -> firstSets.put(nt, new HashSet<>()));

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<List<String>>> entry : grammar.entrySet()) {
                String nonTerminal = entry.getKey();
                for (List<String> production : entry.getValue()) {
                    Set<String> first = computeFirstForSequence(production);
                    if (firstSets.get(nonTerminal).addAll(first)) {
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    // Calcula FIRST para uma sequência de símbolos
    public Set<String> computeFirstForSequence(List<String> sequence) {
        Set<String> result = new HashSet<>();
        boolean allCanDeriveEpsilon = true;

        for (String symbol : sequence) {
            //Caso 1: símbolo vazio
            if (symbol.equals("ε")) {
                result.add("ε");
                break;
            }
            //Caso 2: terminal
            if (terminals.contains(symbol)) {
                result.add(symbol);
                allCanDeriveEpsilon = false;
                break;
            }
            //Caso 3: não terminal
            Set<String> firstOfSymbol = firstSets.get(symbol);
            result.addAll(firstOfSymbol.stream()
                .filter(s -> !s.equals("ε"))
                .collect(Collectors.toSet()));

            if (!firstOfSymbol.contains("ε")) {
                allCanDeriveEpsilon = false;
                break;
            }
        }
        //Se todos os símbolos da direita podem derivar vazio,
        // vazio faz parte do first do não-terminal da direita
        if (allCanDeriveEpsilon) {
            result.add("ε");
        }

        return result;
    }

    // Calcula os conjuntos FOLLOW
    private void computeFollowSets() {
        // Inicializa FOLLOW para todos os não-terminais
        nonTerminals.forEach(nt -> followSets.put(nt, new HashSet<>()));
        followSets.get(startSymbol).add("$"); // Símbolo de fim de entrada

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<List<String>>> entry : grammar.entrySet()) {
                String nonTerminal = entry.getKey();
                for (List<String> production : entry.getValue()) {
                    for (int i = 0; i < production.size(); i++) {
                        String symbol = production.get(i);
                        if (nonTerminals.contains(symbol)) {
                            // Calcula FOLLOW para o símbolo atual
                            List<String> remaining = production.subList(i + 1, production.size());
                            Set<String> firstOfRemaining = computeFirstForSequence(remaining);

                            // Adiciona FIRST(remaining) - {ε} ao FOLLOW(symbol)
                            Set<String> toAdd = firstOfRemaining.stream()
                                .filter(s -> !s.equals("ε"))
                                .collect(Collectors.toSet());
                            
                            if (followSets.get(symbol).addAll(toAdd)) {
                                changed = true;
                            }

                            // Se ε está em FIRST(remaining) ou remaining está vazio
                            if (firstOfRemaining.contains("ε") || remaining.isEmpty()) {
                                if (followSets.get(symbol).addAll(followSets.get(nonTerminal))) {
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        } while (changed);
    }

    // Getters
    public Map<String, Set<String>> getFirstSets() { return firstSets; }
    public Map<String, Set<String>> getFollowSets() { return followSets; }
    public Set<String> getTerminals() { return terminals; }
    public Set<String> getNonTerminals() { return nonTerminals; }

    // Métodos de visualização
    public void printFirstSets() {
        System.out.println("\nFIRST Sets:");
        firstSets.forEach((nt, set) -> System.out.println(nt + " -> " + set));
    }

    public void printFollowSets() {
        System.out.println("\nFOLLOW Sets:");
        followSets.forEach((nt, set) -> System.out.println(nt + " -> " + set));
    }
}
