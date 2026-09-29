package Translator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// _.vm -> _.asm

public class VMTranslator {

    String path, outputPath;
    Parser parser;
    CodeWriter codeWriter;
    private List<File> vmFiles;

    public VMTranslator(String path) {
        File source = new File(path);
        this.vmFiles = new ArrayList<>();

        if (source.isFile()) {
            if (path.endsWith(".vm")) {
                this.vmFiles.add(source);
                this.outputPath = path.substring(0, path.lastIndexOf('.')) + ".asm";
            } else {
                throw new IllegalArgumentException("File must end with .vm");
            }
        } else if (source.isDirectory()) {
            File[] files = source.listFiles((dir, name) -> name.endsWith(".vm"));
            if (files != null) {
                for (File f : files) {
                    this.vmFiles.add(f);
                }
            }
            String dirName = source.getName();
            File output = new File(source, dirName + ".asm"); // Consolidated output [cite: 435]
            this.outputPath = output.getAbsolutePath();
        } else {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        this.codeWriter = new CodeWriter(outputPath);
    }

    public void translateToAsm() {
        codeWriter.writeInit();
        for (File vmFile : vmFiles) {

            Parser parser = new Parser(vmFile.getAbsolutePath());
            String vmFileName = vmFile.getName();
            String nameWithoutExtension = vmFileName.substring(0, vmFileName.lastIndexOf('.'));
            codeWriter.setFileName(nameWithoutExtension);

            while (parser.hasMoreLines()) {
                parser.advance();
                String command = parser.commandType();
                String arg1 = parser.arg1();
                int arg2 = parser.arg2();

                if (command.equals("C_ARITHMETIC")) {
                    codeWriter.writeArithmetic(arg1);
                } else if (command.equals("C_PUSH") || command.equals("C_POP")) {
                    codeWriter.writePushPop(command, arg1, arg2);
                } else if (command.equals("C_IF")) {
                    // if go-to label
                    codeWriter.writeIf(arg1);

                } else if (command.equals("C_GOTO")) {
                    // goto label
                    codeWriter.writeGoto(arg1);

                } else if (command.equals("C_LABEL")) {
                    codeWriter.writeLabel(arg1);

                } else if (command.equals("C_FUNCTION")) {
                    codeWriter.writeFunction(arg1, arg2);

                } else if (command.equals("C_CALL")) {
                    codeWriter.writeCall(arg1, arg2);

                } else if (command.equals("C_RETURN")) {
                    codeWriter.writeReturn();

                }

            }
        }
        codeWriter.close();
    }

}
