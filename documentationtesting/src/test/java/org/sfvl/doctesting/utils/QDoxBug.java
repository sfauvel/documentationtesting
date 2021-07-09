package org.sfvl.doctesting.utils;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class QDoxBug {
    /**
     * Problem is on com.thoughtworks.qdox.library.AbstractClassLibrary.getJavaClass()
     * this.resolveJavaClass(name); sometimes return null
     *
     * SourceLibrary.resolveJavaClass call this.context.removeClassByName(name)
     *
     * In context, there is a space at the end of the name
     * "org.sfvl.doctesting.utils.CodeExtractorTest$ExtractCode$ExtractPartOfCode "
     *
     * ModelBuilder.beginClass(ClassDef def)
     * => def.getName() == "ExtractPartOfCode "
     *
     * com.thoughtworks.qdox.parser.impl.Parser => this.cls.setName(this.val_peek(0).sval);  // Contain a space
     * cls is a ClassDef.
     * Possible Fix is to make a trim in ClassDef.setName
     */
    @Test
    public void testCodeExtractor() {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        final PathProvider pathProvider = new PathProvider();
        builder.addSourceTree(pathProvider.getProjectPath().resolve(Config.TEST_PATH)
                .resolve(Paths.get("org", "sfvl", "doctesting", "utils")).toFile());


//        Class<?> clazz = testMethod.getDeclaringClass();
//        String methodName = testMethod.getName();

        Class<?> clazz = CodeExtractorTest.ExtractCode.ExtractPartOfCode.class;
        //Class<?> clazz = CodeExtractorTest.class;
        String methodName = "tag_with_same_beginning_of_another_tag";


        String name = clazz.getName();
        JavaClass javaClass = builder.getClassByName(name);

        assertFalse(javaClass.getMethods().isEmpty());
        assertFalse(javaClass.getAnnotations().isEmpty());

//        Method testMethod = Arrays.stream(clazz.getMethods()).filter(m -> m.getName().equals(methodName))
//                .findFirst().get();
//        List< JavaType > argumentList  = Arrays.stream(testMethod.getParameterTypes())
//                .map(_clazz -> builder.getClassByName(_clazz.getCanonicalName()))
//                .collect(Collectors.toList());
//        javaClass.getMethods().stream().forEach(
//                m -> System.out.println("- " + m.getName())
//        );
//
//        JavaMethod method = javaClass.getMethod(methodName, argumentList, false);
//        while (method == null && javaClass.getSuperJavaClass() != null) {
//            javaClass = javaClass.getSuperJavaClass();
//            method = javaClass.getMethod(methodName, argumentList, false);
//        }
//        System.out.println("Method:" + method.getName());
//        System.out.println("Comment:" + method.getComment());
    }
}
