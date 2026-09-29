import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JackTokenizer {
    String path;
    String tokenType;
    String content;
    String currentToken;
    private int pointer = 0;

    private static final Set<String> KEYWORDS = new HashSet<>(
            Arrays.asList("class", "constructor", "function", "method", "field", "static", "var", "int", "char",
                    "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"));

    private static final Set<Character> SYMBOLS = new HashSet<>(
            Arrays.asList('(', ')', '{', '}', '[', ']', '.', ',', ';', '+', '-', '*',
                    '/', '&', '|', '<', '>', '=', '~'));

    public JackTokenizer(String path) throws IOException {
        this.path = path;

        String rawContent = Files.readString(Paths.get(path));
        this.content = removeComments(rawContent);

    }

    public boolean hasMoreTokens() {
        while (pointer < content.length() && Character.isWhitespace(content.charAt(pointer))) {
            pointer++;
        }
        return pointer < content.length();
    }

    // gets the next token from the input and makes it the current token
    public void advance() {
        while (pointer < content.length() && Character.isWhitespace(content.charAt(pointer))) {
            pointer++;
        }

        if (pointer >= content.length()) {
            return;
        }

        char c = content.charAt(pointer);

        if (SYMBOLS.contains(c)) { // c is a symvol
            this.tokenType = "SYMBOL";
            currentToken = c + "";
            pointer++;
        }

        else if (c == '"') {
            tokenType = "STRING_CONST";
            pointer++;
            int start = pointer;
            while (pointer < content.length() && content.charAt(pointer) != '"') {
                pointer++;
            }
            currentToken = content.substring(start, pointer); // string inside "..."
            pointer++;
        }

        else if (Character.isDigit(c)) {
            tokenType = "INT_CONST";
            int start = pointer;
            while (pointer < content.length() && Character.isDigit(content.charAt(pointer))) {
                pointer++;
            }
            currentToken = content.substring(start, pointer);

        } else if (Character.isLetter(c) || c == '_') {
            int start = pointer;
            while (pointer < content.length()
                    && (Character.isLetterOrDigit(content.charAt(pointer)) || content.charAt(pointer) == '_')) {
                pointer++;
            }
            String word = content.substring(start, pointer);

            if (KEYWORDS.contains(word)) {
                tokenType = "KEYWORD";
            } else {
                tokenType = "IDENTIFIER";
            }
            currentToken = word;
        }

    }

    public String tokenType() {
        return this.tokenType;
    }

    public String keyword() {
        return currentToken;
    }

    public char symbol() {
        return currentToken.charAt(0);
    }

    public String identifier() {
        return currentToken;
    }

    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    public String stringVal() {
        return currentToken;
    }

    private String removeComments(String content) {
        String res = content.replaceAll("/\\*\\*?[\\s\\S]*?\\*/", "");
        res = res.replaceAll("//.*", "");
        return res;
    }

    public String getToken() {
        return currentToken;
    }

}
