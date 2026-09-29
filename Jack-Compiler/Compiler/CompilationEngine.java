// recursive top-down parser
// input source code -> XML tags

import java.io.IOException;

public class CompilationEngine {
    JackTokenizer tokenizer;
    VMWriter vmWriter;
    SymbolTable symbolTable;
    int labelCount = 0;
    String currentClassName;

    public CompilationEngine(JackTokenizer tokenizer, VMWriter vmWriter, SymbolTable table) throws IOException {

        this.vmWriter = vmWriter;
        this.tokenizer = tokenizer;
        this.symbolTable = table;

    }

    // class className {classVarDec* subroutineDec*}
    public void compileClass() {

        process("class");
        String varName = tokenizer.identifier();
        process(varName);
        currentClassName = varName;

        process("{");

        while (tokenizer.tokenType().equals("KEYWORD")
                && (tokenizer.keyword().equals("static") || tokenizer.keyword().equals("field"))) {
            compileClassVarDec();
        }

        while (tokenizer.tokenType().equals("KEYWORD")
                && (tokenizer.keyword().equals("constructor") || tokenizer.keyword().equals("function")
                        || tokenizer.keyword().equals("method"))) {
            compileSubroutine();
        }

        process("}");

    }

    // (static | field) type varName (, varName)* ;
    public void compileClassVarDec() {
        String type, varName, kind;
        SymbolTable.Kind kind1 = SymbolTable.Kind.STATIC;

        kind = tokenizer.keyword(); // static or field
        process(kind);

        if (kind.equals("field"))
            kind1 = SymbolTable.Kind.FIELD;

        if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("int")
                || tokenizer.keyword().equals("char") || tokenizer.keyword().equals("boolean"))) {
            type = tokenizer.keyword(); // type (int/ char etc ...)
        } else {
            type = tokenizer.identifier();
        }

        process(type);
        varName = tokenizer.identifier();
        process(varName); // varName

        symbolTable.define(varName, type, kind1);

        while (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
            process(",");
            varName = tokenizer.identifier();
            process(varName);
            symbolTable.define(varName, type, kind1);
        }

        process(";");

    }

    // (method | function | constructor) (void int ...) subroutineName(type varName,
    // type
    // varName) {}
    public void compileSubroutine() {

        // method -> this
        // function
        // constructor -> create new object (+allocate memory)

        String keyword, returnType, subroutineName;

        symbolTable.reset();
        keyword = tokenizer.keyword();
        process(keyword);

        if (tokenizer.tokenType().equals("KEYWORD")) {
            returnType = tokenizer.keyword();
            // type (int/ char etc ...)
        } else {
            returnType = tokenizer.identifier();
            // an object
        }

        process(returnType);

        subroutineName = tokenizer.identifier();
        process(subroutineName);

        if (keyword.equals("method")) {
            symbolTable.define("this", currentClassName, SymbolTable.Kind.ARG);
        }

        process("(");
        compileParameterList();
        process(")");
        compileSubroutineBody(keyword, currentClassName + "." + subroutineName);

    }

    // type varName, type varName ...
    public void compileParameterList() {

        // add parameters to symbolTable

        String type, varName;

        if (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ')')) {
            if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("int")
                    || tokenizer.keyword().equals("char") || tokenizer.keyword().equals("boolean"))) {
                type = tokenizer.keyword();
                // type (int/ char etc ...)

            } else {
                type = tokenizer.identifier();
            }

            process(type);
            varName = tokenizer.identifier();
            symbolTable.define(varName, type, SymbolTable.Kind.ARG);
            process(varName);

            while (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
                process(",");
                if (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("int")
                        || tokenizer.keyword().equals("char") || tokenizer.keyword().equals("boolean"))) {
                    type = tokenizer.keyword();
                    // type (int/ char etc ...)
                } else {
                    type = tokenizer.identifier();
                }

                process(type);
                varName = tokenizer.identifier();
                symbolTable.define(varName, type, SymbolTable.Kind.ARG);
                process(varName);
            }
        }

    }

    public void compileSubroutineBody(String kind, String subName) {

        // Header:
        // function ClassName.subroutineName nVars
        int nVars = 0;

        process("{");
        // varDec*
        while (tokenizer.tokenType().equals("KEYWORD") && (tokenizer.keyword().equals("var"))) {
            nVars += compileVarDec();
        }

        vmWriter.writeFunction(subName, nVars);

        if (kind.equals("method")) { // operate on the currentClass, push it's base adress on stack
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0); // sp = base adress of the class
        } else if (kind.equals("constructor")) { // create new class and allocate memory
            int nFields = symbolTable.varCount(SymbolTable.Kind.FIELD);
            vmWriter.writePush("constant", nFields);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0); // sp = base adress of the constructor
        }

        compileStatements();
        process("}");

    }

    // var type varName
    public int compileVarDec() {

        // SymbolTable.define(name, type, VAR);
        // no writing to VM
        int count = 0;

        process("var");
        String type = "";
        if (tokenizer.tokenType().equals("KEYWORD")) {
            type = tokenizer.keyword();
        } else if (tokenizer.tokenType().equals("IDENTIFIER")) {
            type = tokenizer.identifier();
        }
        process(type);
        String name = tokenizer.identifier();
        process(name);
        symbolTable.define(name, type, SymbolTable.Kind.VAR);
        count++;

        while (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
            process(",");
            name = tokenizer.identifier();
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
            process(name);
            count++;
        }

        process(";");
        return count;

    }

    //
    public void compileStatements() {

        while (tokenizer.tokenType().equals("KEYWORD")) {
            String token = tokenizer.keyword();
            if (token.equals("let"))
                compileLet();
            else if (token.equals("if"))
                compileIf();
            else if (token.equals("while"))
                compileWhile();
            else if (token.equals("do"))
                compileDo();
            else if (token.equals("return"))
                compileReturn();
            else
                break;
        }

    }

    public void compileLet() {
        // let varName = expression;
        // (1) remember varName
        // (2) call compileExpression
        // (3) pop varName (SymbolTable mapping of varName)

        process("let");

        String name = tokenizer.identifier();
        String segment = symbolTable.kindOf(name);
        int index = symbolTable.indexOf(name);

        process(name); // varName

        boolean isArray = false;

        // if Array[i]
        if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '[') {
            isArray = true;
            vmWriter.writePush(segment, index); // push base arr address
            process("[");
            compileExpression();
            process("]");
            vmWriter.writeArithmetic("add"); // calculate (arr + i) and push on top of stack
        }

        process("=");
        compileExpression();

        // if Array[i]
        if (isArray) {
            vmWriter.writePop("temp", 0); // temp = value
            vmWriter.writePop("pointer", 1); // that = arr + i
            vmWriter.writePush("temp", 0); // push value on top of stack
            vmWriter.writePop("that", 0); // RAM[arr+i] = (popped) value
        } else {
            vmWriter.writePop(segment, index);
        }

        process(";");

    }

    // if (expression) { ... }
    public void compileIf() {

        String label1 = "L" + labelCount;
        labelCount++;
        String label2 = "L" + labelCount;
        labelCount++;

        process("if");
        process("(");

        compileExpression();
        process(")");

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(label2); // if (false) -> goto the else condition

        process("{");

        compileStatements();
        process("}");

        vmWriter.writeGoto(label1);

        vmWriter.writeLabel(label2);

        if (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyword().equals("else")) {
            process("else");

            process("{");
            compileStatements();
            process("}");
        }

        vmWriter.writeLabel(label1);

    }

    // while (expression) { ... }
    public void compileWhile() {

        String label1 = "L" + labelCount;
        labelCount++;
        String label2 = "L" + labelCount;
        labelCount++;

        process("while");

        vmWriter.writeLabel(label1);

        process("(");
        compileExpression();
        process(")");

        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(label2);

        process("{");
        compileStatements();

        vmWriter.writeGoto(label1);

        process("}");

        vmWriter.writeLabel(label2);

    }

    // do functionName(args);
    public void compileDo() {
        // call compile expression
        // pop temp 0 (to get rid of the return value)
        String functionName;
        int nArgs = 0;
        process("do");
        String name = tokenizer.identifier();
        process(name);

        // ex: Output.println()
        if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '.') {
            process(".");
            String subName = tokenizer.identifier();
            process(subName);
            if (!symbolTable.kindOf(name).equals("NONE")) { // object.method()
                vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name)); // push class base adress
                String type = symbolTable.typeOf(name);
                functionName = type + "." + subName;
                nArgs = 1;
            } else { // static function or constructor
                functionName = name + "." + subName;
            }
        } else { // method call on this
            vmWriter.writePush("pointer", 0); // base adress of this current class
            functionName = currentClassName + "." + name;
            nArgs = 1;
        }

        process("(");
        nArgs += compileExpressionList();
        process(")");
        process(";");

        vmWriter.writeCall(functionName, nArgs);
        vmWriter.writePop("temp", 0);

    }

    // return varName; | return;
    public void compileReturn() {
        // pop result from the stack

        process("return");

        if (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ';')) {
            compileExpression();
        } else {
            vmWriter.writePush("constant", 0); // return void
        }

        process(";");
        vmWriter.writeReturn();

    }

    // expression (x+y-b)
    public void compileExpression() {
        char op;
        compileTerm();
        while (tokenizer.tokenType().equals("SYMBOL") && isOp(tokenizer.symbol())) {
            op = tokenizer.symbol();
            process(op + "");
            compileTerm();

            switch (op) {
                case '+':
                    vmWriter.writeArithmetic("add");
                    break;
                case '-':
                    vmWriter.writeArithmetic("sub");
                    break;
                case '*':
                    vmWriter.writeCall("Math.multiply", 2);
                    break;
                case '/':
                    vmWriter.writeCall("Math.divide", 2);
                    break;
                case '&':
                    vmWriter.writeArithmetic("and");
                    break;
                case '|':
                    vmWriter.writeArithmetic("or");
                    break;
                case '<':
                    vmWriter.writeArithmetic("lt");
                    break;
                case '>':
                    vmWriter.writeArithmetic("gt");
                    break;
                case '=':
                    vmWriter.writeArithmetic("eq");
                    break;

                default:
                    break;
            }
        }

    }

    private boolean isOp(char ch) {
        return (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '&' || ch == '|' || ch == '<' || ch == '>'
                || ch == '=');
    }

    // intConst | stringConst | keyword (true/false/null/this) | -/~term |
    // (expression) varName | varName[expression] | (varName.)subroutineName(args)
    public void compileTerm() {

        String tokenType = tokenizer.tokenType();

        if (tokenType.equals("INT_CONST")) {
            int val = tokenizer.intVal();
            process(String.valueOf(val));
            vmWriter.writePush("constant", val);
        } else if (tokenType.equals("STRING_CONST")) {
            String val = tokenizer.stringVal();
            process(val);
            vmWriter.writePush("constant", val.length());
            vmWriter.writeCall("String.new", 1);
            for (int i = 0; i < val.length(); i++) {
                vmWriter.writePush("constant", (int) val.charAt(i));
                vmWriter.writeCall("String.appendChar", 2);
            }
        } else if (tokenType.equals("KEYWORD")) { // (true/false/null/this)
            String keyword = tokenizer.keyword();
            process(keyword);
            if (keyword.equals("false") || keyword.equals("null")) {
                vmWriter.writePush("constant", 0);
            } else if (keyword.equals("true")) {
                vmWriter.writePush("constant", 0);
                vmWriter.writeArithmetic("not");
            } else { // this
                vmWriter.writePush("pointer", 0);
            }
        } else if (tokenType.equals("SYMBOL")) {
            char ch = tokenizer.symbol();

            if (ch == '(') {
                process("(");
                compileExpression();
                process(")");

            } else if (ch == '-') {
                process(ch + "");
                compileTerm();
                vmWriter.writeArithmetic("neg");

            } else if (ch == '~') {
                process(ch + "");
                compileTerm();
                vmWriter.writeArithmetic("not");
            }

        } else if (tokenType.equals("IDENTIFIER")) {

            String prev = tokenizer.identifier();
            process(prev);
            if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '[') { // arr[i] // push ram(arr + i)
                vmWriter.writePush(symbolTable.kindOf(prev), symbolTable.indexOf(prev));
                process("[");
                compileExpression(); // pushes i on stack
                process("]");
                vmWriter.writeArithmetic("add");
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0); // RAM[arr+i]

            } else if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '(') { // subroutineName(args)
                vmWriter.writePush("pointer", 0);
                process("(");
                int nArgs = compileExpressionList();
                vmWriter.writeCall(currentClassName + "." + prev, nArgs + 1);
                process(")");
            } else if (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '.') {
                String functionName;
                int nArgs = 0;
                process(".");
                String subName = tokenizer.identifier();
                process(subName);
                if (!symbolTable.kindOf(prev).equals("NONE")) { // object.method()
                    vmWriter.writePush(symbolTable.kindOf(prev), symbolTable.indexOf(prev)); // push class base adress
                    String type = symbolTable.typeOf(prev);
                    functionName = type + "." + subName;
                    nArgs = 1;
                } else { // static function or constructor
                    functionName = prev + "." + subName;
                }
                process("(");
                nArgs += compileExpressionList();
                process(")");

                vmWriter.writeCall(functionName, nArgs);

            } else {
                vmWriter.writePush(symbolTable.kindOf(prev), symbolTable.indexOf(prev));
            }
        }

    }

    public int compileExpressionList() {

        int count = 0;

        if (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ')')) {
            compileExpression();
            count++;

            while (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ',') {
                process(",");
                compileExpression();
                count++;
            }
        }

        return count;

    }

    private void process(String expected) {
        if (tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.getToken();

            if (currentToken.equals(expected)) {
                tokenizer.advance();
            }

            else {
                throw new RuntimeException("Syntax error");
            }
        }
    }

}
