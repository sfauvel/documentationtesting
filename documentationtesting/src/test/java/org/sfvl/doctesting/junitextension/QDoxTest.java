package org.sfvl.doctesting.junitextension;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.NotIncludeToDoc;


import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QDoxTest {
    class NestedClass {
        /**
         * Method comment in an inner class.
         */
        @Test
        public void methodInSubClass() {
            System.out.println("My method");
        }
    }

    /**
     * My comment.
     */
    @Test
    public void ttt() {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(Paths.get("src", "main", "java").toFile());
        builder.addSourceTree(Paths.get("src", "test", "java").toFile());

        JavaClass javaClass = builder.getClassByName("org.sfvl.doctesting.junitextension.QDoxTest");


        List<String> methods = javaClass.getMethods().stream()
                .map(m -> m.getName())
                .collect(Collectors.toList());

        System.out.println(methods);

        for (JavaMethod method : javaClass.getMethods()) {
            System.out.println("Method:" + method.getName());
            System.out.println("Comment: " + method.getComment());
        }

        JavaMethod tttMethod = javaClass.getMethods().stream()
                .filter(m -> m.getName().equals("ttt"))
                .findFirst().get();


        assertEquals("My comment.", tttMethod.getComment());

        /*
        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
        while (method == null && javaClass.getSuperJavaClass() != null) {
            javaClass = javaClass.getSuperJavaClass();
            method = javaClass.getMethod(methodName, argumentList, false);
        }
        return Optional.ofNullable(method).map(c -> c.getComment());
*/
    }
}


// tag::classNestedWithCommentToExtract[]
@NotIncludeToDoc
/**
 * Comment of the class.
 */
class ClassNestedWithCommentToExtract {

    /**
     * Comment of the subclass.
     */
    class SubClassNestedWithCommentToExtract {
        /**
         * Method comment in an inner class.
         */
        @Test
        public void methodInSubClass() {
            System.out.println("My method");
        }
    }
}
// end::classNestedWithCommentToExtract[]