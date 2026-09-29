// writes VM code

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    String path;

    BufferedWriter writer;

    public VMWriter(String outputPath) {
        this.path = outputPath;
        try {
            this.writer = new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            throw new RuntimeException("Fatal Error: Could not open output file: " + outputPath, e);
        }

    }

    public void writePush(String segment, int index) {
        try {
            writer.write("push " + getSegment(segment) + " " + index);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing push command: " + e);
        }

    }

    public void writePop(String segment, int index) {
        try {
            writer.write("pop " + getSegment(segment) + " " + index);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing pop command: " + e);
        }

    }

    private String getSegment(String kind) {
        switch (kind) {
            case "VAR":
                return "local";
            case "FIELD":
                return "this";
            case "ARG":
                return "argument";
            case "STATIC":
                return "static";
            default:
                return kind.toLowerCase();
        }
    }

    public void writeArithmetic(String command) {
        try {

            writer.write(command);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing arithmetic command: " + e);
        }
    }

    public void writeLabel(String label) {
        try {
            writer.write("label " + label);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing label: " + e);
        }
    }

    public void writeGoto(String label) {
        try {
            writer.write("goto " + label);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing goto command: " + e);
        }
    }

    public void writeIf(String label) {
        try {
            writer.write("if-goto " + label);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing if command: " + e);
        }
    }

    public void writeCall(String name, int nArgs) {
        try {
            writer.write("call " + name + " " + nArgs);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing call command: " + e);
        }
    }

    public void writeFunction(String name, int nArgs) {
        try {
            writer.write("function " + name + " " + nArgs);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing function: " + e);
        }

    }

    public void writeReturn() {
        try {
            writer.write("return");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error writing return command: " + e);
        }
    }

    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing file", e);
            }
        }
    }

}
