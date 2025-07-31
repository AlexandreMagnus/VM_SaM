import java.util.*;

public class LL1Checker {
    private final Map<String, List<List<String>>> grammar;
    private final FirstFollowCalculator calculator;
    private final PredictSetCalculator predictCalculator;
    // private final String startSymbol;

    public LL1Checker(Map<String, List<List<String>>> grammar, String startSymbol) {
        // this.startSymbol = startSymbol;
        this.grammar = grammar;
        this.calculator = new FirstFollowCalculator(grammar, startSymbol);
        this.predictCalculator = new PredictSetCalculator(grammar, startSymbol);
    }

    // Verifica se a gramática é LL(1)
    public boolean isLL1() {
        Set<String> nonTerminals = calculator.getNonTerminals();
        Map<String, Set<String>> firstSets = calculator.getFirstSets();
        Map<String, Set<String>> followSets = calculator.getFollowSets();
        for (String nonTerminal : nonTerminals) {
            List<List<String>> productions = grammar.get(nonTerminal);
            // Regra 1: FIRST de cada produção deve ser disjunto
            for (int i = 0; i < productions.size(); i++) {
                for (int j = i + 1; j < productions.size(); j++) {
                    Set<String> firstI = calculator.computeFirstForSequence(productions.get(i));
                    Set<String> firstJ = calculator.computeFirstForSequence(productions.get(j));
                    // Remove ε para comparação
                    firstI.remove("ε");
                    firstJ.remove("ε");
                    if (!Collections.disjoint(firstI, firstJ)) {
                        System.out.println("Conflito entre producoes " + (i + 1) + " e " + (j + 1) +
                                " de " + nonTerminal + ": " + firstI + " ∩ " + firstJ);
                        return false;
                    }
                }
            }
            // Regra 2: Se ε está em algum FIRST, FIRST ∩ FOLLOW deve ser vazio
            for (List<String> production : productions) {
                Set<String> first = calculator.computeFirstForSequence(production);
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
        calculator.printFirstSets();
    }

    public void printFollowSets() {
        calculator.printFollowSets();
    }

    public void printPredictTable() {
        predictCalculator.printPredictTable();
    }

    public PredictSetCalculator getPredictCalculator() {
        return predictCalculator;
    }

    public boolean parse(List<String> tokens) {
        // Pilha para controle da análise, começa com o símbolo inicial + fim $
        Deque<String> stack = new ArrayDeque<>();
        stack.push("$");
        stack.push(this.calculator.getStartSymbol());

        // Índice da posição atual na lista de tokens
        int index = 0;

        while (!stack.isEmpty()) {
            String top = stack.peek();
            String currentToken = (index < tokens.size()) ? tokens.get(index) : null;

            if (top.equals("$") && currentToken.equals("$")) {
                // Aceita a entrada
                return true;
            }

            // Se o topo da pilha é terminal
            if (isTerminal(top) || top.equals("$")) {
                if (top.equals(currentToken)) {
                    // Consumir token e desempilhar
                    stack.pop();
                    index++;
                } else {
                    // Token esperado diferente do atual -> erro
                    System.out.println("Erro: token esperado '" + top + "' mas encontrado '" + currentToken + "'");
                    return false;
                }
            } else {
                // topo é não-terminal: consulta tabela preditiva
                List<String> production = predictCalculator.getProduction(top, currentToken);

                if (production == null) {
                    System.out.println(
                            "Erro: nao ha producao para nao-terminal '" + top + "' com token '" + currentToken + "'");
                    return false;
                }

                stack.pop();
                // Empilha produção em ordem reversa (exceto ε)
                if (!(production.size() == 1 && production.get(0).equals("ε"))) {
                    ListIterator<String> it = production.listIterator(production.size());
                    while (it.hasPrevious()) {
                        stack.push(it.previous());
                    }
                }
            }
        }

        // Se terminou pilha mas ainda há tokens -> erro
        if (index < tokens.size()) {
            System.out.println("Erro: tokens restantes apos fim da analise");
            return false;
        }

        return true;
    }

    private boolean isTerminal(String symbol) {
        // Considere que terminais são símbolos que não estão na gramática
        // (não-terminais)
        return !grammar.containsKey(symbol);
    }

    // Exemplo de uso
    public static void main(String[] args) {
        // Exemplo de gramática
        Map<String, List<List<String>>> grammar = new HashMap<>();
        // S → statement*
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
                Arrays.asList("IDENTIFIER", "identifier_stmt_tail")));
        grammar.put("identifier_stmt_tail", Arrays.asList(
                Arrays.asList("=", "expression", ";"), // Atribuição
                Arrays.asList("(", "args_opt", ")", ";") // Chamada de função
        ));
        // declaration → type IDENTIFIER ("=" expression)? ";"
        grammar.put("declaration", Arrays.asList(
                Arrays.asList("type", "IDENTIFIER", "declaration_tail")));
        grammar.put("declaration_tail", Arrays.asList(
                Arrays.asList("=", "expression", ";"),
                Arrays.asList(";")));
        // type → "xaropinho" | "xarope" | "letra"
        grammar.put("type", Arrays.asList(
                Arrays.asList("xaropinho"),
                Arrays.asList("xarope"),
                Arrays.asList("letra")));
        // expression → logical_or
        grammar.put("expression", Arrays.asList(
                Arrays.asList("logical_or")));
        // logical_or → logical_and ("ou" logical_and)*
        grammar.put("logical_or", Arrays.asList(
                Arrays.asList("logical_and", "logical_or_tail")));
        grammar.put("logical_or_tail", Arrays.asList(
                Arrays.asList("ou", "logical_and", "logical_or_tail"),
                Arrays.asList("ε")));
        // logical_and → equality ("e" equality)*
        grammar.put("logical_and", Arrays.asList(
                Arrays.asList("equality", "logical_and_tail")));
        grammar.put("logical_and_tail", Arrays.asList(
                Arrays.asList("e", "equality", "logical_and_tail"),
                Arrays.asList("ε")));
        // equality → comparison (("==" | "!=") comparison)*
        grammar.put("equality", Arrays.asList(
                Arrays.asList("comparison", "equality_tail")));
        grammar.put("equality_tail", Arrays.asList(
                Arrays.asList("==", "comparison", "equality_tail"),
                Arrays.asList("!=", "comparison", "equality_tail"),
                Arrays.asList("ε")));
        // comparison → term ((">" | ">=" | "<" | "<=") term)*
        grammar.put("comparison", Arrays.asList(
                Arrays.asList("term", "comparison_tail")));
        grammar.put("comparison_tail", Arrays.asList(
                Arrays.asList(">", "term", "comparison_tail"),
                Arrays.asList(">=", "term", "comparison_tail"),
                Arrays.asList("<", "term", "comparison_tail"),
                Arrays.asList("<=", "term", "comparison_tail"),
                Arrays.asList("ε")));
        // term → factor (("+" | "-") factor)*
        grammar.put("term", Arrays.asList(
                Arrays.asList("factor", "term_tail")));
        grammar.put("term_tail", Arrays.asList(
                Arrays.asList("+", "factor", "term_tail"),
                Arrays.asList("-", "factor", "term_tail"),
                Arrays.asList("ε")));
        // factor → unary (("*" | "/" | "%") unary)*
        grammar.put("factor", Arrays.asList(
                Arrays.asList("unary", "factor_tail")));
        grammar.put("factor_tail", Arrays.asList(
                Arrays.asList("*", "unary", "factor_tail"),
                Arrays.asList("/", "unary", "factor_tail"),
                Arrays.asList("%", "unary", "factor_tail"),
                Arrays.asList("ε")));
        // unary → ("danca" | "-") unary | primary
        grammar.put("unary", Arrays.asList(
                Arrays.asList("danca", "unary"),
                Arrays.asList("-", "unary"),
                Arrays.asList("primary")));
        // primary → NUMBER | REALNUMBER | IDENTIFIER | "(" expression ")" |
        // function_call
        grammar.put("primary", Arrays.asList(
                Arrays.asList("NUMBER"),
                Arrays.asList("REALNUMBER"),
                Arrays.asList("IDENTIFIER", "primary_tail"),
                Arrays.asList("(", "expression", ")")));
        grammar.put("primary_tail", Arrays.asList(
                Arrays.asList("(", "args_opt", ")"),
                Arrays.asList("ε")));
        grammar.put("args_opt", Arrays.asList(
                Arrays.asList("args"),
                Arrays.asList("ε")));
        // args → expression ("," expression)*
        grammar.put("args", Arrays.asList(
                Arrays.asList("expression", "args_tail")));
        grammar.put("args_tail", Arrays.asList(
                Arrays.asList(",", "expression", "args_tail"),
                Arrays.asList("ε")));
        // if_statement → "uepa" "(" expression ")" "shi" statement ("ui" statement)?
        grammar.put("if_statement", Arrays.asList(
                Arrays.asList("uepa", "(", "expression", ")", "shi", "block", "if_tail")));
        grammar.put("if_tail", Arrays.asList(
                Arrays.asList("ui", "statement"), // Com else
                Arrays.asList("ε") // Sem else
        ));
        // while_loop → "ratinho" "(" expression ")" statement
        grammar.put("while_loop", Arrays.asList(
                Arrays.asList("ratinho", "(", "expression", ")", "block")));
        // break_stmt → "pare" ";"
        grammar.put("break_stmt", Arrays.asList(
                Arrays.asList("pare", ";")));
        // continue_stmt → "cavalo" ";"
        grammar.put("continue_stmt", Arrays.asList(
                Arrays.asList("cavalo", ";")));
        // function_decl → "rapaz" IDENTIFIER "(" params? ")" block
        grammar.put("function_decl", Arrays.asList(
                Arrays.asList("rapaz", "IDENTIFIER", "(", "params_opt", ")", "block")));
        grammar.put("params_opt", Arrays.asList(
                Arrays.asList("params"),
                Arrays.asList("ε")));
        // params → param ("," param)*
        grammar.put("params", Arrays.asList(
                Arrays.asList("param", "params_tail")));
        grammar.put("params_tail", Arrays.asList(
                Arrays.asList(",", "param", "params_tail"),
                Arrays.asList("ε")));
        // param → type IDENTIFIER
        grammar.put("param", Arrays.asList(
                Arrays.asList("type", "IDENTIFIER")));
        // block → "{" statement* "}"
        grammar.put("block", Arrays.asList(
                Arrays.asList("{", "statements", "}")));
        grammar.put("statements", Arrays.asList(
                Arrays.asList("statement", "statements"),
                Arrays.asList("ε")));
        // return_stmt → "volta" expression? ";"
        grammar.put("return_stmt", Arrays.asList(
                Arrays.asList("volta", "expression_opt", ";")));
        grammar.put("expression_opt", Arrays.asList(
                Arrays.asList("expression"),
                Arrays.asList("ε") // Caso sem expressão
        ));
        LL1Checker checker = new LL1Checker(grammar, "S");
        checker.printFirstSets();
        checker.printFollowSets();
        checker.printPredictTable();
        if (checker.isLL1()) {
            System.out.println("\nA gramatica e LL(1)");
        } else {
            System.out.println("\nA gramatica nao e LL(1)");
        }
    }
}