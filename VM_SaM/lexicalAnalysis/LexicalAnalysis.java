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
                System.err.printf("[Erro em analise lexica] Char invalido '%s' na linha %d, coluna %d%n", lexeme, lineNumber,
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
            String sourceCode = readFile("text.txt");
            List<Map<String, Object>> tokens = lexicalAnalysis(sourceCode);

            for (Map<String, Object> token : tokens) {
                System.out.println(token);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }
}
