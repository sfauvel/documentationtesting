package org.sfvl.doctesting.test_tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class InterceptorStream extends PrintStream {
    public final List<String> text = new ArrayList<>();

    InterceptorStream(PrintStream o) {
        super(o, true);
    }

    @Override
    public void print(String s) {
        text.add(s);
    }

    public void write(int b) {
    }

    public void write(byte buf[], int off, int len) {
    }

    @Override
    public String toString() {
        return text.stream()
                .collect(Collectors.joining("\n"));
    }
}
