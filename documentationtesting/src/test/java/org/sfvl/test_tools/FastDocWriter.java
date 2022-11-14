package org.sfvl.test_tools;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.ClassesOrder;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.DocWriter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FastDocWriter  extends DocWriter<Formatter> {

    public FastDocWriter(Formatter formatter) {
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

    public static class FastClassDocumentation extends ClassDocumentation {
        private Function<Class, String> titleId;

        public FastClassDocumentation(Formatter formatter, Function<Class, String> titleId) {
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
            final Stream<ClassesOrder.EncapsulateDeclared> declaredMethodInOrder = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Test.class))
                    .map(m -> new ClassesOrder.EncapsulateDeclaredMethod(m));


            final Stream<ClassesOrder.EncapsulateDeclared> declaredClassInOrder = Arrays.stream(clazz.getDeclaredClasses())
                    .filter(m -> m.isAnnotationPresent(Nested.class))
                    .map(m -> new ClassesOrder.EncapsulateDeclaredClass(m));

            final Stream<ClassesOrder.EncapsulateDeclared> declaredInOrder = Stream.concat(
                    declaredMethodInOrder,
                    declaredClassInOrder
            );
            return this.getClassDocumentation(clazz, depth, (List) declaredInOrder.collect(Collectors.toList()));
        }

    }

}
