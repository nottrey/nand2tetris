package Translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

// understand vm code

public class Parser {
    String path;
    List<String> commands;
    int currentLine;
    String currentCommand;

    public Parser(String path) {
        this.path = path;
        commands = new ArrayList<>();
        currentLine = 0;

        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            lines.forEach(line -> {
                commands.add(line);
            });
        } catch (IOException e) {
            throw new RuntimeException("Fatal Error: Could not find or read file " + path);
        }
    }

    public boolean hasMoreLines() {
        return (currentLine < commands.size());
    }

    public void advance() {
        currentCommand = "";
        while (hasMoreLines() && currentCommand.isEmpty()) {
            String line = commands.get(currentLine);
            line = line.replaceAll("//.*", "").trim();

            if (!line.isEmpty()) {
                currentCommand = line;
            }
            currentLine++;
        }
    }

    // arithmetic / push / pop label / goto / if / function / return / call
    //
    public String commandType() {
        String[] parts = currentCommand.trim().split("\\s+");
        if (parts.length == 0 || currentCommand.isEmpty()) {
            return null;
        }

        String op = parts[0];

        if (op.equals("push"))
            return "C_PUSH";
        if (op.equals("pop"))
            return "C_POP";
        if (op.equals("if-goto"))
            return "C_IF";
        if (op.equals("goto"))
            return "C_GOTO";
        if (op.equals("label"))
            return "C_LABEL";
        if (op.equals("gt") || op.equals("lt") || op.equals("eq"))
            return "C_ARITHMETIC";
        if (op.equals("add") || op.equals("sub") || op.equals("neg")
                || op.equals("and") || op.equals("or") || op.equals("not")) {
            return "C_ARITHMETIC";
        }
        if (op.equals("function"))
            return "C_FUNCTION";
        if (op.equals("call"))
            return "C_CALL";
        if (op.equals("return"))
            return "C_RETURN";

        return null;
    }

    // push local x

    public String arg1() {
        if (!currentCommand.isEmpty()) {
            String type = this.commandType();
            if (type == null || type.equals("C_RETURN"))
                return null;
            String[] parts = currentCommand.trim().split("\\s+");
            if (type.equals("C_ARITHMETIC"))
                return parts[0]; // add, sub ...
            return parts[1];
        }
        return null;
    }

    public int arg2() {
        if (!currentCommand.isEmpty()) {
            String type = this.commandType();
            if (type == null)
                return -1;
            if (type.equals("C_POP") || type.equals("C_PUSH") || type.equals("C_CALL") || type.equals("C_FUNCTION")) {
                String[] parts = currentCommand.trim().split("\\s+");
                return Integer.parseInt(parts[2]);
            }
        }
        return -1;
    }

}