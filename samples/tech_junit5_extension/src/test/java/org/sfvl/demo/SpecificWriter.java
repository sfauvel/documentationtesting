package org.sfvl.demo;

import org.sfvl.doctesting.writer.DocWriter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Writer could be extended to provide specific methods.
 */
class SpecificWriter extends DocWriter {

    public void writeBold(String... texts) {
        write(Arrays.stream(texts).map(s -> "*"+s+"*").collect(Collectors.joining("\n")));
    }

}
