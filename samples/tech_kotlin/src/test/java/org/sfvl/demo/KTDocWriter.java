package org.sfvl.demo;

import org.junit.jupiter.api.Test;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassToDocument;
import org.sfvl.doctesting.utils.ClassesOrder;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.DocWriter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This object is used to store the text that need to be written to the final document.
 */

public class KTDocWriter<F extends Formatter> extends DocWriter<Formatter> {

    public KTDocWriter() {
        super();
    }

    public KTDocWriter(F formatter) {
        super(formatter);
    }

    @Override
    protected Optional<String> getComment(Class<?> classFile, Method testMethod) {
        return Optional.empty();
    }

    public String formatOutput(Class<?> clazz) {
        final ClassDocumentation classDocumentation = new KTClassDocumentation(getFormatter(), this::titleId);

        return getFormatter().paragraph(
                defineDocPath(clazz),
                "",
                classDocumentation.getClassDocumentation(clazz)
        );
    }


    public static class KTClassDocumentation extends ClassDocumentation {
        private Function<Class, String> titleId;

        public KTClassDocumentation(Formatter formatter, Function<Class, String> titleId) {
            super(formatter);
            this.titleId = titleId;
        }

        @Override
        protected Optional<String> relatedClassDescription(Class<?> fromClass) {
            return Optional.empty();
        }

        @Override
        protected String getComment(Class<?> classToDocument) {
            return "";
        }

        @Override
        public String getTitle(Class<?> clazz, int depth) {
            return String.join("\n",
                    formatter.blockId(titleId.apply(clazz)),
                    super.getTitle(clazz, depth));
        }

        @Override
        public String getClassDocumentation(Class<?> clazz, int depth) {
            final Stream<ClassesOrder.EncapsulateDeclaredMethod> declaredInOrder = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .map(m -> new ClassesOrder.EncapsulateDeclaredMethod(m));
            return this.getClassDocumentation(clazz, depth, (List) declaredInOrder.collect(Collectors.toList()));
        }

    }

}
