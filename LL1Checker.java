import java.util.*;
import java.util.stream.Collectors;

public class LL1Checker {
    private final Map<String, List<List<String>>> grammar;
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final String startSymbol;
    private final Map<String, Set<String>> firstSets = new HashMap<>();
    private final Map<String, Set<String>> followSets = new HashMap<>();

    public LL1Checker(Map<String, List<List<String>>> grammar, String startSymbol) {
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
    private Set<String> computeFirstForSequence(List<String> sequence) {
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

    // Verifica se a gramática é LL(1)
    public boolean isLL1() {
        for (String nonTerminal : nonTerminals) {
            List<List<String>> productions = grammar.get(nonTerminal);
            
            // Regra 1: FIRST de cada produção deve ser disjunto
            for (int i = 0; i < productions.size(); i++) {
                for (int j = i + 1; j < productions.size(); j++) {
                    Set<String> firstI = computeFirstForSequence(productions.get(i));
                    Set<String> firstJ = computeFirstForSequence(productions.get(j));
                    
                    // Remove ε para comparação
                    firstI.remove("ε");
                    firstJ.remove("ε");
                    
                    if (!Collections.disjoint(firstI, firstJ)) {
                        System.out.println("Conflito entre producoes " + (i+1) + " e " + (j+1) + 
                                         " de " + nonTerminal + ": " + firstI + " ∩ " + firstJ);
                        return false;
                    }
                }
            }
            
            // Regra 2: Se ε está em algum FIRST, FIRST ∩ FOLLOW deve ser vazio
            for (List<String> production : productions) {
                Set<String> first = computeFirstForSequence(production);
                if (first.contains("ε")) {
                    Set<String> follow = followSets.get(nonTerminal);
                    Set<String> intersection = new HashSet<>(first);
                    intersection.retainAll(follow);
                    intersection.remove("ε");
                    
                    if (!intersection.isEmpty()) {
                        System.out.println("Conflito FIRST/FOLLOW para " + nonTerminal + 
                                         ": FIRST " + first + " ∩ FOLLOW " + follow);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Métodos auxiliares para visualização
    public void printFirstSets() {
        System.out.println("\nFIRST Sets:");
        firstSets.forEach((nt, set) -> System.out.println(nt + " -> " + set));
    }

    public void printFollowSets() {
        System.out.println("\nFOLLOW Sets:");
        followSets.forEach((nt, set) -> System.out.println(nt + " -> " + set));
    }

    // Exemplo de uso
    public static void main(String[] args) {
        // Exemplo de gramática (substitua pela sua)
        Map<String, List<List<String>>> grammar = new HashMap<>();
        /*grammar.put("S", Arrays.asList(
            Arrays.asList("A", "C"),
            Arrays.asList("a")
        ));
        grammar.put("A", Arrays.asList(
            Arrays.asList("a", "B"),
            Arrays.asList("ε")
        ));
        grammar.put("B", Arrays.asList(
            Arrays.asList("b"),
            Arrays.asList("ε")
        ));
        grammar.put("C", Arrays.asList(
            Arrays.asList("c")
        ));*/
         //S → statement*
        grammar.put("S", Arrays.asList(
            Arrays.asList("statement", "S"),
            Arrays.asList("ε") // Fim da recursão
        ));

        // statement → declaration | assignment | ... | identifier_stmt
        grammar.put("statement", Arrays.asList(
            Arrays.asList("declaration"),
            Arrays.asList("if_statement"),
            Arrays.asList("while_loop"),
            Arrays.asList("function_decl"),
            Arrays.asList("return_stmt"),
            Arrays.asList("break_stmt"),
            Arrays.asList("continue_stmt"),
            Arrays.asList("block"),
            Arrays.asList("IDENTIFIER", "identifier_stmt_tail")
        ));
        grammar.put("identifier_stmt_tail", Arrays.asList(
            Arrays.asList("=", "expression", ";"),    // Atribuição
            Arrays.asList("(", "args_opt", ")", ";")  // Chamada de função
        ));

        // declaration → type IDENTIFIER ("=" expression)? ";"
        grammar.put("declaration", Arrays.asList(
          Arrays.asList("type", "IDENTIFIER", "declaration_tail")
        ));

        grammar.put("declaration_tail", Arrays.asList(
          Arrays.asList("=", "expression", ";"),
          Arrays.asList(";")
        ));

        // type → "xaropinho" | "xarope" | "letra"
        grammar.put("type", Arrays.asList(
            Arrays.asList("xaropinho"),
            Arrays.asList("xarope"),
            Arrays.asList("letra")
        ));

        // identifier_stmt → IDENTIFIER ("=" expression | "(" args? ")") ";"
        grammar.put("identifier_stmt", Arrays.asList(
            Arrays.asList("IDENTIFIER", "identifier_tail")  // Produção unificada
        ));

        grammar.put("identifier_tail", Arrays.asList(
            Arrays.asList("=", "expression", ";"),         // Caso atribuição
            Arrays.asList("(", "args_opt", ")", ";")         // Caso chamada com args
        ));

        // expression → logical_or
        grammar.put("expression", Arrays.asList(
            Arrays.asList("logical_or")
        ));

        // logical_or → logical_and ("ou" logical_and)*
        grammar.put("logical_or", Arrays.asList(
            Arrays.asList("logical_and", "logical_or_tail")
        ));
        grammar.put("logical_or_tail", Arrays.asList(
            Arrays.asList("ou", "logical_and", "logical_or_tail"),
            Arrays.asList("ε")
        ));
        // logical_and → equality ("e" equality)*
        grammar.put("logical_and", Arrays.asList(
            Arrays.asList("equality", "logical_and_tail")
        ));
        grammar.put("logical_and_tail", Arrays.asList(
            Arrays.asList("e", "equality", "logical_and_tail"),
            Arrays.asList("ε")
        ));

        // equality → comparison (("==" | "!=") comparison)*
        grammar.put("equality", Arrays.asList(
            Arrays.asList("comparison", "equality_tail")
        ));
        grammar.put("equality_tail", Arrays.asList(
            Arrays.asList("==", "comparison", "equality_tail"),
            Arrays.asList("!=", "comparison", "equality_tail"),
            Arrays.asList("ε")
        ));

        // comparison → term ((">" | ">=" | "<" | "<=") term)*
        grammar.put("comparison", Arrays.asList(
            Arrays.asList("term", "comparison_tail")
        ));
        grammar.put("comparison_tail", Arrays.asList(
            Arrays.asList(">", "term", "comparison_tail"),
            Arrays.asList(">=", "term", "comparison_tail"),
            Arrays.asList("<", "term", "comparison_tail"),
            Arrays.asList("<=", "term", "comparison_tail"),
            Arrays.asList("ε")
        ));

        // term → factor (("+" | "-") factor)*
        grammar.put("term", Arrays.asList(
            Arrays.asList("factor", "term_tail")
        ));
        grammar.put("term_tail", Arrays.asList(
            Arrays.asList("+", "factor", "term_tail"),
            Arrays.asList("-", "factor", "term_tail"),
            Arrays.asList("ε")
        ));

        // factor → unary (("*" | "/" | "%") unary)*
        grammar.put("factor", Arrays.asList(
            Arrays.asList("unary", "factor_tail")
        ));
        grammar.put("factor_tail", Arrays.asList(
            Arrays.asList("*", "unary", "factor_tail"),
            Arrays.asList("/", "unary", "factor_tail"),
            Arrays.asList("%", "unary", "factor_tail"),
            Arrays.asList("ε")
        ));

        // unary → ("danca" | "-") unary | primary
        grammar.put("unary", Arrays.asList(
            Arrays.asList("danca", "unary"),
            Arrays.asList("-", "unary"),
            Arrays.asList("primary")
        ));

        // primary → NUMBER | REALNUMBER | IDENTIFIER | "(" expression ")" | function_call
        grammar.put("primary", Arrays.asList(
            Arrays.asList("NUMBER"),
            Arrays.asList("REALNUMBER"),
            Arrays.asList("IDENTIFIER", "primary_tail"),
            Arrays.asList("(", "expression", ")")
        ));

        grammar.put("primary_tail", Arrays.asList(
          Arrays.asList("(", "args_opt", ")"),
          Arrays.asList("ε")
        ));

        // function_call → IDENTIFIER "(" args_opt ")"
        grammar.put("function_call", Arrays.asList(
            Arrays.asList("IDENTIFIER", "(", "args_opt", ")")
        ));
        grammar.put("args_opt", Arrays.asList(
            Arrays.asList("args"),
            Arrays.asList("ε")
        ));

        // args → expression ("," expression)*
        grammar.put("args", Arrays.asList(
            Arrays.asList("expression", "args_tail")
        ));
        grammar.put("args_tail", Arrays.asList(
            Arrays.asList(",", "expression", "args_tail"),
            Arrays.asList("ε")
        ));

        // function_call_stmt → function_call ";"
        grammar.put("function_call_stmt", Arrays.asList(
            Arrays.asList("function_call", ";")
        ));

        // if_statement → "uepa" "(" expression ")" "shi" statement ("ui" statement)?
        grammar.put("if_statement", Arrays.asList(
           Arrays.asList("uepa", "(", "expression", ")", "shi", "statement", "if_tail")
        ));

        grammar.put("if_tail", Arrays.asList(
           Arrays.asList("ui", "statement"),  // Com else
           Arrays.asList("ε")                 // Sem else
        ));

        // while_loop → "ratinho" "(" expression ")" statement
        grammar.put("while_loop", Arrays.asList(
            Arrays.asList("ratinho", "(", "expression", ")", "statement")
        ));

        // break_stmt → "pare" ";"
        grammar.put("break_stmt", Arrays.asList(
            Arrays.asList("pare", ";")
        ));

        // continue_stmt → "cavalo" ";"
        grammar.put("continue_stmt", Arrays.asList(
            Arrays.asList("cavalo", ";")
        ));

        // function_decl → "rapaz" IDENTIFIER "(" params? ")" block
        grammar.put("function_decl", Arrays.asList(
            Arrays.asList("rapaz", "IDENTIFIER", "(", "params_opt", ")", "block")
        ));

        grammar.put("params_opt", Arrays.asList(
            Arrays.asList("params"),
            Arrays.asList("ε")
        ));

        // params → param ("," param)*
        grammar.put("params", Arrays.asList(
            Arrays.asList("param", "params_tail")
        ));
        grammar.put("params_tail", Arrays.asList(
            Arrays.asList(",", "param", "params_tail"),
            Arrays.asList("ε")
        ));

        // param → type IDENTIFIER
        grammar.put("param", Arrays.asList(
            Arrays.asList("type", "IDENTIFIER")
        ));

        // block → "{" statement* "}"
        grammar.put("block", Arrays.asList(
            Arrays.asList("{", "statements", "}")
        ));
        grammar.put("statements", Arrays.asList(
            Arrays.asList("statement", "statements"),
            Arrays.asList("ε")
        ));

        // assignment → IDENTIFIER "=" expression ";"
        grammar.put("assignment", Arrays.asList(
            Arrays.asList("IDENTIFIER", "=", "expression", ";")
        ));

        // return_stmt → "volta" expression? ";"
        grammar.put("return_stmt", Arrays.asList(
            Arrays.asList("volta", "expression_opt", ";")
        ));

        grammar.put("expression_opt", Arrays.asList(
           Arrays.asList("expression"),
           Arrays.asList("ε")  // Caso sem expressão
        ));


        LL1Checker checker = new LL1Checker(grammar, "S");
        checker.printFirstSets();
        checker.printFollowSets();
        
        if (checker.isLL1()) {
            System.out.println("\nA gramatica e LL(1)");
        } else {
            System.out.println("\nA gramatica nao e LL(1)");
        }
    }
}