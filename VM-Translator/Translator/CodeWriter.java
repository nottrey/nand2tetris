package Translator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CodeWriter {

    String outputPath;
    private int callCounter = 0;
    Path filePath;
    BufferedWriter writer;
    int sp = 256;
    int ram[];
    private int labelCounter = 0;
    private int staticIndex = 16;
    private String fileName;
    private String currentFunction = "Sys.init";

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public CodeWriter(String path) {
        this.outputPath = path;
        this.filePath = Path.of(outputPath);
        ram = new int[16384]; // RAM16k of fixed size
        try {
            this.writer = new BufferedWriter(new FileWriter(outputPath));
        } catch (IOException e) {
            throw new RuntimeException("Fatal Error: Could not open output file: " + outputPath, e);
        }
    }

    public void writeArithmetic(String command) {
        // add , sub, neg, lt, eq, gt and, or, not
        try {
            writer.write("// " + command);
            writer.newLine();

            // add (x+y)
            if (command.equals("add")) {
                // pop stack y
                // pop stack x
                // push x+y
                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("M=D+M");
                writer.newLine();

            }

            // sub(x-y)
            else if (command.equals("sub")) {

                // pop stack y
                // pop stack x
                // push x-y
                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("M=M-D");
                writer.newLine();

            }

            // neg (-x)
            else if (command.equals("neg")) {
                // pop stack x
                // push -x
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M-1");
                writer.newLine();
                writer.write("M=-M"); // d=x
                writer.newLine();
            }

            // lt (x<y)
            else if (command.equals("lt")) {
                // pop stack x
                // pop stack y
                // write x<y (boolean)

                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("D=M-D"); // x=M, y=D //x-y lt is true if d is negative
                writer.newLine();
                writer.write("M=-1");
                writer.newLine();
                writer.write("@JLT_END" + labelCounter);
                writer.newLine();
                writer.write("D;JLT");
                writer.newLine();
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M-1");
                writer.newLine();
                writer.write("M=0");
                writer.newLine();
                writer.write("(JLT_END" + labelCounter + ")");
                writer.newLine();
                labelCounter++;
            }

            else if (command.equals("eq")) {
                // pop stack x
                // pop stack y
                // write x==y (boolean)

                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("D=D-M"); // x=M, y=D //x-y lt is true if d is 0
                writer.newLine();
                writer.write("M=-1"); // true
                writer.newLine();
                writer.write("@JEQ_END" + labelCounter);
                writer.newLine();
                writer.write("D;JEQ");
                writer.newLine();
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M-1");
                writer.newLine();
                writer.write("M=0");
                writer.newLine();
                writer.write("(JEQ_END" + labelCounter + ")");
                writer.newLine();
                labelCounter++;
            }

            else if (command.equals("gt")) {
                // pop y
                // pop x
                // push (x>y)
                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("D=M-D");
                writer.newLine();
                writer.write("M=-1");
                writer.newLine();
                writer.write("@JGT_END" + labelCounter);
                writer.newLine();
                writer.write("D;JGT");
                writer.newLine();
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M-1");
                writer.newLine();
                writer.write("M=0");
                writer.newLine();
                writer.write("(JGT_END" + labelCounter + ")");
                writer.newLine();
                labelCounter++;
            }

            else if (command.equals("and")) {
                // pop y
                // pop x
                // push (x and y)
                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("M=D&M");
                writer.newLine();
            }

            else if (command.equals("or")) {
                // pop y
                // pop x
                // push (x or y)
                writer.write("@SP");
                writer.newLine();
                writer.write("AM=M-1");
                writer.newLine();
                writer.write("D=M");
                writer.newLine();
                writer.write("A=A-1");
                writer.newLine();
                writer.write("M=D|M");
                writer.newLine();
            }

            else if (command.equals("not")) {
                // pop x
                // push (not x)
                // push (x or y)
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M-1");
                writer.newLine();
                writer.write("M=!M"); // !x
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing arithmetic command: " + command, e);
        }

    }

    // push segment index, pop
    public void writePushPop(String command, String segment, int index) {
        try {
            writer.write("// " + command + " " + segment + " " + index);
            writer.newLine();
            ///

            String base = "";

            if (segment.equals("local"))
                base = "LCL";
            else if (segment.equals("argument"))
                base = "ARG";
            else if (segment.equals("this"))
                base = "THIS";
            else if (segment.equals("that"))
                base = "THAT";
            else if (segment.equals("temp"))
                base = "5";
            else if (segment.equals("pointer")) {
                if (index == 0)
                    base = "THIS";
                else
                    base = "THAT";
            }

            // pointer 0 -> access THIS
            // pointer 1 -> access THAT

            if (command.equals("C_PUSH")) {
                // push RAM[RAM[segment] + index]
                // for local, argument , this, that, temp
                if (List.of("local", "argument", "this", "that").contains(segment)) {
                    writer.write("@" + index);
                    writer.newLine();
                    writer.write("D=A");
                    writer.newLine();
                    writer.write("@" + base);
                    writer.newLine();
                    writer.write("A=D+M");
                    writer.newLine();
                    writer.write("D=M"); // D = RAM[LCL] + index
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("M=M+1");
                    writer.newLine();

                } else if (segment.equals("temp")) {
                    int temp = index + 5;
                    writer.write("@" + temp);
                    writer.newLine();
                    writer.write("D=M");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("M=M+1");
                    writer.newLine();

                } else if (segment.equals("pointer")) {
                    // push onto the stack this/that
                    writer.write("@" + base);
                    writer.newLine();
                    writer.write("D=M");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("M=M+1");
                    writer.newLine();

                } else if (segment.equals("constant")) {
                    // push constant on stack
                    writer.write("@" + index);
                    writer.newLine();
                    writer.write("D=A");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("M=M+1");
                    writer.newLine();

                } else if (segment.equals("static")) {
                    // static
                    writer.write("@" + fileName + "." + index);
                    writer.newLine();
                    writer.write("D=M");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    writer.write("@SP");
                    writer.newLine();
                    writer.write("M=M+1");
                    writer.newLine();
                }
            }

            if (command.equals("C_POP")) {
                if (List.of("local", "argument", "this", "that").contains(segment)) {
                    // pop base+index -> set ram[base+index] equal to popped stack value
                    writer.write("@" + index);
                    writer.newLine();
                    writer.write("D=A");
                    writer.newLine();
                    writer.write("@" + base);
                    writer.newLine();
                    writer.write("D=D+M");
                    writer.newLine();
                    writer.write("@R13");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                    // calculate (index+base) -> store at register 13

                    popFromStack(); // D = popped value

                    writer.write("@R13");
                    writer.newLine();
                    writer.write("A=M"); // access RAM[base+index]
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();

                } else if (segment.equals("temp")) {
                    // pop the stack's top value in RAM[5+index]
                    int temp = 5 + index;
                    writer.write("@" + temp);
                    writer.newLine();
                    writer.write("D=A");
                    writer.newLine();
                    writer.write("@R13");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();

                    popFromStack(); // D = popped value

                    writer.write("@R13");
                    writer.newLine();
                    writer.write("A=M");
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();
                }

                else if (segment.equals("pointer")) {
                    // pop the stack's top value in this/that
                    popFromStack(); // D = popped value
                    writer.write("@" + base);
                    writer.newLine();
                    writer.write("M=D"); // update THIS/THAT pointer
                    writer.newLine();

                } else if (segment.equals("static")) {
                    popFromStack(); // D = popped value
                    writer.write("@" + fileName + "." + index);
                    writer.newLine();
                    writer.write("M=D");
                    writer.newLine();

                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing arithmetic command: " + command, e);
        }

    }

    private String symbolFromLabel(String label) {
        return currentFunction + "$" + label;
    }

    // label label
    public void writeLabel(String label) {
        try {
            writer.write("//label  " + label);
            writer.newLine();
            writer.write("(" + this.symbolFromLabel(label) + ")");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error writing label command: " + e);
        }
    }

    // goto label -> unconditional jump
    public void writeGoto(String label) {
        try {
            writer.write("//goto  " + label);
            writer.newLine();
            String asmSymbol = this.symbolFromLabel(label);
            writer.write("@" + asmSymbol);
            writer.newLine();
            writer.write("0;JMP");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error writing goto command: " + e);
        }

    }

    public void writeInit() {
        try {
            writer.write("@256");
            writer.newLine();
            writer.write("D=A");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            writeCall("Sys.init", 0);

        } catch (IOException e) {
            throw new RuntimeException("Error writing bootstrap initialization: " + e);
        }
    }

    // if-goto label (the stack's topmost value is popped and if it isn't zero the
    // jump is
    // executed)
    public void writeIf(String label) {
        try {
            writer.write("//if-goto  " + label);
            writer.newLine();
            String asmSymbol = this.symbolFromLabel(label);

            writer.write("@SP");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M"); // topmost val
            writer.newLine();
            writer.write("@" + asmSymbol);
            writer.newLine();
            writer.write("D;JNE"); // if zero -> false
            writer.newLine();
            //

        } catch (IOException e) {
            throw new RuntimeException("Error writing goto command: " + e);
        }
    }

    // function functionName nVars
    public void writeFunction(String functionName, int nVars) {
        // push 0 nVars times
        currentFunction = functionName;
        try {
            writer.write("//function " + functionName + " " + nVars);
            writer.newLine();
            writer.write("(" + functionName + ")");
            writer.newLine();
            // loop
            for (int i = 0; i < nVars; i++) {
                writer.write("@SP");
                writer.newLine();
                writer.write("A=M");
                writer.newLine();
                writer.write("M=0");
                writer.newLine();
                writer.write("@SP");
                writer.newLine();
                writer.write("M=M+1");
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error writing function commmand: " + e);
        }

    }

    // call functionName nArgs
    // caler frame:
    // ---------------
    // |return adress|
    // --------------
    // | saved LCL |
    // --------------
    // | saved ARG |
    // --------------
    // | saved THIS |
    // --------------
    // | saved THAT |

    public void writeCall(String functionName, int nArgs) {
        try {
            writer.write("//call " + functionName);
            writer.newLine();
            String uniqueRet = currentFunction + "$ret" + callCounter;
            callCounter++;

            // push ret address
            pushValOnStack(uniqueRet);
            // push LCL, ARG ...
            pushRegOnStack("LCL");
            pushRegOnStack("ARG");
            pushRegOnStack("THIS");
            pushRegOnStack("THAT");

            // arguments
            // ARG = SP - 5 - nArgs -> point to the arguments that were pushed on the stack
            // before calling the function

            int temp = 5 + nArgs;

            writer.write("@" + temp);
            writer.newLine();
            writer.write("D=A");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("D=M-D"); // D stores SP - (5+nArgs)
            writer.newLine();
            writer.write("@ARG");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // LCL = SP
            writer.write("@SP");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@LCL");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // return
            writer.write("@" + functionName);
            writer.newLine();
            writer.write("0;JMP");
            writer.newLine();
            writer.write("(" + uniqueRet + ")");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error writing call commmand: " + e);
        }
    }

    // return
    // get return adress from the stack and execute an unconditional jump
    public void writeReturn() {
        try {
            writer.write("//return");
            writer.newLine();

            writer.write("@LCL");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@R13");
            writer.newLine();
            writer.write("M=D"); // r13 stores *LCL
            writer.newLine();
            writer.write("@5");
            writer.newLine();
            writer.write("A=D-A");
            writer.newLine();
            writer.write("D=M"); // *LCL - 5
            writer.newLine();
            writer.write("@R14");
            writer.newLine();
            writer.write("M=D"); // r14 stores *LCL - 5
            writer.newLine();

            popFromStack(); // D = popped value
            writer.write("@ARG");
            writer.newLine();
            writer.write("A=M"); // *ARG = pop()
            writer.newLine();
            writer.write("M=D"); // *ARG = pop()
            writer.newLine();

            writer.write("@ARG");
            writer.newLine();
            writer.write("D=M+1");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("M=D"); // SP = ARG + 1
            writer.newLine();

            // THAT
            writer.write("@R13");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@THAT");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // THIS
            writer.write("@R13");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@THIS");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // ARG
            writer.write("@R13");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@ARG");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // LCL
            writer.write("@R13");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@LCL");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();

            // jump to return address
            writer.write("@R14");
            writer.newLine();
            writer.write("A=M");
            writer.newLine();
            writer.write("0;JMP");
            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("Error writing function commmand: " + e);
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

    // saves the popped value in D
    private void popFromStack() {
        try {
            writer.write("@SP");
            writer.newLine();
            writer.write("AM=M-1");
            writer.newLine();
            writer.write("D=M"); // popped value
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error closing file", e);
        }
    }

    private void pushRegOnStack(String reg) {
        try {
            writer.write("//push " + reg);
            writer.newLine();
            writer.write("@" + reg);
            writer.newLine();
            writer.write("D=M");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("A=M");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("M=M+1");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error pushing: " + e);
        }
    }

    private void pushValOnStack(String val) {
        try {
            writer.write("//push " + val);
            writer.newLine();
            writer.write("@" + val);
            writer.newLine();
            writer.write("D=A");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("A=M");
            writer.newLine();
            writer.write("M=D");
            writer.newLine();
            writer.write("@SP");
            writer.newLine();
            writer.write("M=M+1");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Error pushing: " + e);
        }
    }
}