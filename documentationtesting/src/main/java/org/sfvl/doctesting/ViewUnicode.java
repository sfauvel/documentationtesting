package org.sfvl.doctesting;

public class ViewUnicode {

    public static void show() {
        write("\n\n");

        int start = 20;
        for (int line = 0; line < 370; line++) {
            int nbByLine = 30;
            write((start + (line * nbByLine)) + ": ");
            for (int i = 0; i < nbByLine; i++) {
                int value = start + i + line * nbByLine;
                write("&#x" + value + "; ");
            }
            write(" +\n");
        }
    }
    public static void write(String text) {
        System.out.print(text);
    }

    public static void main(String... args) {
        show();
    }
}
