import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    List<String> commands;
    int currentLine;
    String currentCommand;
    String type;
    String comp, dest, jump;

    public Parser(String path) {
        commands = new ArrayList<>();
        currentLine = 0;
        currentCommand = "";

        // read assembly input line by line and save in the command list
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            lines.forEach(line -> {
                commands.add(line);
            });
        } catch (IOException e) {
            // Throw a RuntimeException to halt the assembler immediately if the file is
            // missing!
            throw new RuntimeException("Fatal Error: Could not find or read file " + path);
        }

    }

    public boolean hasMoreCommands() {
        return currentLine < commands.size();
    }

    public void advance() {
        currentCommand = "";
        while (hasMoreCommands() && currentCommand.isEmpty()) {
            String line = commands.get(currentLine);
            line = line.replaceAll("//.*", ""); // Remove comments
            line = line.replaceAll("\\s+", ""); // Remove all spaces and tabs
            if (!line.isEmpty()) {
                currentCommand = line;
            }
            currentLine++;
        }
    }

    public String instructionType() {
        if (currentCommand.contains(";") || currentCommand.contains("=")) {
            return "C-INSTRUCTION";
        } else if (currentCommand.contains("@")) {
            return "A-INSTRUCTION";
        } else if (currentCommand.contains("(") && currentCommand.contains(")")) {
            return "L-INSTRUCTION";
        }
        return null;
        // "A-INSTRUCTION"
        // "B-INSTRUCTION"
        // "L-INSTRUCTION"

    }

    // for A-INSTRUCTION or L-INSTRUCTION return Xxx (either @Xxx or Xxx
    // instruction
    public String symbol() {
        if (currentCommand.matches("\\(.+\\)")) {
            return currentCommand.substring(1, currentCommand.length() - 1);
        }
        return currentCommand.replaceAll("@", "");
    }

    public String dest() {
        // split and retun destination (where to store the computed value)
        String[] parts;
        if (!currentCommand.contains("=")) { // comp;jump
            return null;
        }

        parts = currentCommand.split("="); // dest=comp;jump
        return parts[0];
    }

    public String comp() {
        // split and return comp

        String parts[];
        if (currentCommand.contains("=")) {
            parts = currentCommand.split("="); // dest and comp;jump
            parts = parts[1].split(";");
        } else {
            parts = currentCommand.split(";");
        }
        return parts[0];

    }

    public String jump() {
        if (!currentCommand.contains(";")) {
            return null;
        }
        String[] parts = currentCommand.split(";");
        return parts[1];
    }

}