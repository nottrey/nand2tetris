
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class HackAssembler {

    Code code;
    Parser parser;
    SymbolTable table;
    String path;
    int availableRam = 16;
    int romAddress = 0;
    String outputPath;

    public HackAssembler(String path) {
        this.path = path;
        code = new Code();
        parser = new Parser(path);
        table = new SymbolTable();

        translateToHack();

    }

    public void translateToHack() {

        // first pass -> set SymbolTable

        while (parser.hasMoreCommands()) {

            parser.advance();

            // check if it's A-instruction, C-instruction or L-instruction
            String instruction = parser.instructionType();

            // C-instruction encoding : dest=comp;jump
            if (instruction.equals("C-INSTRUCTION")) {
                romAddress++;
            }

            // A-instruction : @value
            else if (instruction.equals("A-INSTRUCTION")) {
                romAddress++;
            }

            // L-instruction : (TAG)
            else if (instruction.equals("L-INSTRUCTION")) {
                String symbol = parser.symbol();
                table.addEntry(symbol, romAddress);

            }
        } // end of first pass

        // second pass

        String outputPath = path.substring(0, path.lastIndexOf('.')) + ".hack";
        parser = new Parser(path);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)))) {

            while (parser.hasMoreCommands()) {
                String binaryLine = null;
                parser.advance();

                String instruction = parser.instructionType();

                if (instruction.equals("C-INSTRUCTION")) {
                    String destStr = parser.dest();
                    String compStr = parser.comp();
                    String jumpStr = parser.jump();

                    String binaryDest = code.dest(destStr);
                    String binaryComp = code.comp(compStr);
                    String binaryJump = code.jump(jumpStr);

                    binaryLine = "111" + binaryComp + binaryDest + binaryJump;
                }

                else if (instruction.equals("A-INSTRUCTION")) {
                    int symbolCode = 0;
                    String symbol = parser.symbol();

                    if (symbol.matches("\\d+")) { // Numeric constant [cite: 56]
                        symbolCode = Integer.parseInt(symbol);
                    } else {
                        if (table.contains(symbol)) {
                            symbolCode = table.getAddress(symbol);
                        } else {
                            // First time seeing this variable! Register it now [cite: 151]
                            table.addEntry(symbol, availableRam);
                            symbolCode = availableRam;
                            availableRam++;
                        }
                    }

                    String binarySymbolStr = String.format("%15s", Integer.toBinaryString(symbolCode)).replace(' ',
                            '0');
                    binaryLine = "0" + binarySymbolStr;
                }

                if (binaryLine != null) {
                    writer.println(binaryLine);
                }
            }
            System.out.println("Assembled successfully: " + outputPath);

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java HackAssembler <path-to-asm-file>");
            System.exit(1);
        }
        new HackAssembler(args[0]);
    }

}