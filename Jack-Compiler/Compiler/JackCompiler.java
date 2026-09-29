// main program
// JackTokenizer -> CompilationEngine -> SymbolTable -> VMWriter
// start with compileClass

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JackCompiler {

    public JackCompiler() {

    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("file Jack or directory");
            return;

        }
        String inputPath = args[0];
        File source = new File(inputPath);
        List<File> jackFiles = new ArrayList<>();

        if (source.isFile()) {
            if (inputPath.endsWith(".jack")) {
                jackFiles.add(source);
            } else {
                throw new IllegalArgumentException("File must end with .jack");
            }
        } else if (source.isDirectory()) {
            File[] files = source.listFiles((dir, name) -> name.endsWith(".jack"));
            if (files != null) {
                for (File f : files) {
                    jackFiles.add(f);
                }
            }
        } else {
            throw new IllegalArgumentException("Path doesn't exist");
        }

        // each jack file -> vm output file

        for (File jackFile : jackFiles) {
            String vmPath = jackFile.getAbsolutePath().replaceAll("\\.jack$", ".vm");

            try {
                JackTokenizer tokenizer = new JackTokenizer(jackFile.getAbsolutePath());
                if (tokenizer.hasMoreTokens()) { // class
                    tokenizer.advance();
                }
                VMWriter vmWriter = new VMWriter(vmPath);
                SymbolTable symbolTable = new SymbolTable();
                CompilationEngine compilationEngine = new CompilationEngine(tokenizer, vmWriter, symbolTable);
                compilationEngine.compileClass();
                vmWriter.close();
                System.out.println("Successfully compiled: " + jackFile.getName());
            } catch (Exception e) {
                System.err.println("Error compiling " + jackFile.getName() + " : " + e.getMessage());
            }
        }
    }

}
