package org.sfvl.doctesting.sample;

public class SimpleClassA {
    public void methodWithCodeToExtract() {
        int i = 0;
        // >>>
        int j = i;
        // <<<
        int k = i + j;
    }
}
