import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class LexicalAnalysis {
    private static final Map<String, String> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("uepa", "IF");
        KEYWORDS.put("ui", "ELSE");
        KEYWORDS.put("shi", "THEN");
        KEYWORDS.put("ratinho", "WHILE");
        KEYWORDS.put("pare", "BREAK");
        KEYWORDS.put("cavalo", "CONTINUE");
        KEYWORDS.put("e", "AND");
        KEYWORDS.put("ou", "OR");
        KEYWORDS.put("n", "NOT");
        KEYWORDS.put("letra", "CHAR");
        KEYWORDS.put("rapaz", "FUN");
        KEYWORDS.put("xaropinho", "INT");
        KEYWORDS.put("xarope", "FLOAT");
    }

    private static final String[][] TOKEN_TYPES = {
            { "REALNUMBER", "\\b\\d+\\.\\d+\\b" },
            { "NUMBER", "\\b\\d+\\b" },
            { "IDENTIFIER", "\\b[a-zA-Z_]\\w*\\b" },
            { "ATTRIBUTION", "=" },
            { "OPERATOR", "(>=|<=|==|!=|>|<|[+\\-*/=])" },
            { "DELIMITER", "[(){};,]" },
            { "WHITESPACE", "[ \\t]+" },
            { "NEWLINE", "\\n" },
            { "UNKNOWN", "." }
    };

    private static Pattern buildRegex() {
        StringBuilder regex = new StringBuilder();
        for (String[] tokenType : TOKEN_TYPES) {
            if (regex.length() > 0)
                regex.append("|");
            regex.append(String.format("(?<%s>%s)", tokenType[0], tokenType[1]));
        }
        return Pattern.compile(regex.toString());
    }

    public static List<String> convertTokensForParser(List<Map<String, Object>> tokens) {
        List<String> result = new ArrayList<>();
        for (Map<String, Object> token : tokens) {
            String type = (String) token.get("type");
            switch (type) {
                case "INT":
                case "FLOAT":
                case "CHAR":
                case "FUN":
                case "IF":
                case "ELSE":
                case "WHILE":
                case "BREAK":
                case "CONTINUE":
                case "AND":
                case "OR":
                case "NOT":
                case "DELIMITER":
                case "OPERATOR":
                case "ATTRIBUTION":
                    result.add((String) token.get("value")); // Usa o lexema literal (ex: "+", ";")
                    break;
                case "NUMBER":
                case "REALNUMBER":
                case "IDENTIFIER":
                    result.add(type); // Usa o nome do token como terminal da gramática
                    break;
                default:
                    System.err.println("Token inesperado: " + token);
            }
        }
        result.add("$"); // fim da entrada
        return result;
    }

    private static Map<String, List<List<String>>> buildGrammar() {
        Map<String, List<List<String>>> grammar = new HashMap<>();

        grammar.put("S", Arrays.asList(
                Arrays.asList("statement", "S"),
                Arrays.asList("ε")));

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
                Arrays.asList("=", "expression", ";"),
                Arrays.asList("(", "args_opt", ")", ";")));

        grammar.put("declaration", Arrays.asList(
                Arrays.asList("type", "IDENTIFIER", "declaration_tail")));

        grammar.put("declaration_tail", Arrays.asList(
                Arrays.asList("=", "expression", ";"),
                Arrays.asList(";")));

        grammar.put("type", Arrays.asList(
                Arrays.asList("xaropinho"),
                Arrays.asList("xarope"),
                Arrays.asList("letra")));

        grammar.put("expression", Arrays.asList(
                Arrays.asList("logical_or")));

        grammar.put("logical_or", Arrays.asList(
                Arrays.asList("logical_and", "logical_or_tail")));

        grammar.put("logical_or_tail", Arrays.asList(
                Arrays.asList("ou", "logical_and", "logical_or_tail"),
                Arrays.asList("ε")));

        grammar.put("logical_and", Arrays.asList(
                Arrays.asList("equality", "logical_and_tail")));

        grammar.put("logical_and_tail", Arrays.asList(
                Arrays.asList("e", "equality", "logical_and_tail"),
                Arrays.asList("ε")));

        grammar.put("equality", Arrays.asList(
                Arrays.asList("comparison", "equality_tail")));

        grammar.put("equality_tail", Arrays.asList(
                Arrays.asList("==", "comparison", "equality_tail"),
                Arrays.asList("!=", "comparison", "equality_tail"),
                Arrays.asList("ε")));

        grammar.put("comparison", Arrays.asList(
                Arrays.asList("term", "comparison_tail")));

        grammar.put("comparison_tail", Arrays.asList(
                Arrays.asList(">", "term", "comparison_tail"),
                Arrays.asList(">=", "term", "comparison_tail"),
                Arrays.asList("<", "term", "comparison_tail"),
                Arrays.asList("<=", "term", "comparison_tail"),
                Arrays.asList("ε")));

        grammar.put("term", Arrays.asList(
                Arrays.asList("factor", "term_tail")));

        grammar.put("term_tail", Arrays.asList(
                Arrays.asList("+", "factor", "term_tail"),
                Arrays.asList("-", "factor", "term_tail"),
                Arrays.asList("ε")));

        grammar.put("factor", Arrays.asList(
                Arrays.asList("unary", "factor_tail")));

        grammar.put("factor_tail", Arrays.asList(
                Arrays.asList("*", "unary", "factor_tail"),
                Arrays.asList("/", "unary", "factor_tail"),
                Arrays.asList("%", "unary", "factor_tail"),
                Arrays.asList("ε")));

        grammar.put("unary", Arrays.asList(
                Arrays.asList("danca", "unary"),
                Arrays.asList("-", "unary"),
                Arrays.asList("primary")));

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

        grammar.put("args", Arrays.asList(
                Arrays.asList("expression", "args_tail")));

        grammar.put("args_tail", Arrays.asList(
                Arrays.asList(",", "expression", "args_tail"),
                Arrays.asList("ε")));

        grammar.put("if_statement", Arrays.asList(
                Arrays.asList("uepa", "(", "expression", ")", "shi", "block", "if_tail")));

        grammar.put("if_tail", Arrays.asList(
                Arrays.asList("ui", "statement"),
                Arrays.asList("ε")));

        grammar.put("while_loop", Arrays.asList(
                Arrays.asList("ratinho", "(", "expression", ")", "block")));

        grammar.put("break_stmt", Arrays.asList(
                Arrays.asList("pare", ";")));

        grammar.put("continue_stmt", Arrays.asList(
                Arrays.asList("cavalo", ";")));

        grammar.put("function_decl", Arrays.asList(
                Arrays.asList("rapaz", "IDENTIFIER", "(", "params_opt", ")", "block")));

        grammar.put("params_opt", Arrays.asList(
                Arrays.asList("params"),
                Arrays.asList("ε")));

        grammar.put("params", Arrays.asList(
                Arrays.asList("param", "params_tail")));

        grammar.put("params_tail", Arrays.asList(
                Arrays.asList(",", "param", "params_tail"),
                Arrays.asList("ε")));

        grammar.put("param", Arrays.asList(
                Arrays.asList("type", "IDENTIFIER")));

        grammar.put("block", Arrays.asList(
                Arrays.asList("{", "statements", "}")));

        grammar.put("statements", Arrays.asList(
                Arrays.asList("statement", "statements"),
                Arrays.asList("ε")));

        grammar.put("return_stmt", Arrays.asList(
                Arrays.asList("volta", "expression_opt", ";")));

        grammar.put("expression_opt", Arrays.asList(
                Arrays.asList("expression"),
                Arrays.asList("ε")));

        return grammar;
    }

    public static List<Map<String, Object>> lexicalAnalysis(String sourceCode) {
        List<Map<String, Object>> tokens = new ArrayList<>();
        Pattern tokenPattern = buildRegex();
        Matcher matcher = tokenPattern.matcher(sourceCode);

        int lineNumber = 1;
        int colNumber = 1;

        while (matcher.find()) {
            String tokenType = null;
            String lexeme = null;

            for (String[] tokenTypeDef : TOKEN_TYPES) {
                tokenType = tokenTypeDef[0];
                lexeme = matcher.group(tokenType);
                if (lexeme != null)
                    break;
            }

            int startCol = colNumber;
            colNumber += lexeme.length();

            if ("NEWLINE".equals(tokenType)) {
                lineNumber++;
                colNumber = 1;
                continue;
            } else if ("WHITESPACE".equals(tokenType)) {
                continue;
            } else if ("UNKNOWN".equals(tokenType)) {
                System.err.printf("[Erro em analise lexica] Char invalido '%s' na linha %d, coluna %d%n", lexeme,
                        lineNumber,
                        startCol);
                continue;
            }

            // Verifica se é palavra-chave
            if ("IDENTIFIER".equals(tokenType) && KEYWORDS.containsKey(lexeme)) {
                tokenType = KEYWORDS.get(lexeme);
            }

            Map<String, Object> token = new HashMap<>();
            token.put("type", tokenType);
            token.put("value", lexeme);
            token.put("line", lineNumber);
            token.put("column", startCol);

            tokens.add(token);
        }

        return tokens;
    }

    public static String readFile(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)), "UTF-8");
    }

    public static void main(String[] args) {
        try {
            String sourceCode = readFile("lexicalAnalysis\\text.txt");
            List<Map<String, Object>> tokens = lexicalAnalysis(sourceCode);

            // for (Map<String, Object> token : tokens) {
            //     System.out.println(token);
            // }

            Map<String, List<List<String>>> grammar = buildGrammar();
            List<String> entradaParaParser = convertTokensForParser(tokens);

            PredictSetCalculator predictor = new PredictSetCalculator(grammar, "S");
            LL1Checker parser = new LL1Checker(grammar, "S");

            boolean aceito = parser.parse(entradaParaParser);
            System.out.println("Resultado: " + (aceito ? "Aceito" : "Rejeitado"));

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
