package Translator;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Translator.Main <source-file-or-directory>");
            System.exit(1);
        }

        try {
            VMTranslator translator = new VMTranslator(args[0]);
            translator.translateToAsm();
            System.out.println("Translation completed successfully!");
        } catch (Exception e) {
            System.err.println("Translation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}