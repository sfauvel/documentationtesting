package org.sfvl.test_tools;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.writer.DocWriter;

import java.lang.reflect.Method;
import java.util.Optional;

public class FastDocWriter<F extends Formatter> extends DocWriter<F> {

    public FastDocWriter(F formatter) {
        super(formatter);
    }

    /**
     * Skip reading comment because it's time consuming.
     * We can add comment by a doc.write instruction at the beginning of the test.
     * Most of the time, we need to add reference in description and we already use doc.write instead of the Javadoc.
     */
    protected Optional<String> getComment(Class<?> classFile, Method testMethod) {
        return Optional.empty();
    }


}
