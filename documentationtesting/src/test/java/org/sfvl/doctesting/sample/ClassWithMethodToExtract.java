package org.sfvl.doctesting.sample;

public class ClassWithMethodToExtract {
    public void methodWithOnePartToExtract() {
        int i = 0;
        // >>>
        int j = i;
        // <<<
        int k = i + j;
    }

    public int methodWithSeveralPartsToExtract(int value) {
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
