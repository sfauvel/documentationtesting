package org.sfvl.doctesting.sample;

public class SimpleClassB {
    public int methodWithCodeToExtract(int value) {
        // >>>Part1
        int square = value * value;
        // <<<Part1

        // >>>Part2
        final String text = String.format(
                "square(%d)=%d",
                value,
                square);
        System.out.println(text);
        // <<<Part2

        // >>>Part1
        return square;
        // <<<Part1
    }
}
