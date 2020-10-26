package org.sfvl.doctesting;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.DocWriter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Extract information from code.
 */
class CodeExtractorTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void extract_code_from_class() {
        docWriter.write("To extract code of a class",
                includeSourceWithTag("CodeExtractor_extract_code_from_class_getCode"), "", "");

        docWriter.write(".Source code from file",
                includeSourceWithTag("classToExtract", SimpleClass.class),
                "", "");

        // tag::CodeExtractor_extract_code_from_class_getCode[]
        String code = CodeExtractor.getCode(SimpleClass.class);
        // end::CodeExtractor_extract_code_from_class_getCode[]

        docWriter.write(".Source code extracted",
                "[source, java]",
                "----", "",
                code,
                "----","");
    }

    @Test
    public void extract_code_from_method() {
        docWriter.write("To extract code of a method",
                includeSourceWithTag("CodeExtractor_extract_code_from_method_getCode"), "", "");

        docWriter.write(".Source code from file",
                includeSourceWithTag("classToExtract", SimpleClass.class),
                "", "");

        // tag::CodeExtractor_extract_code_from_method_getCode[]
        String code = CodeExtractor.extractMethodBody(SimpleClass.class, "simpleMethod");
        // end::CodeExtractor_extract_code_from_method_getCode[]

        docWriter.write(".Source code extracted",
                "[source, java]",
                "----", "",
                code,
                "----","");
    }

    @Test
    public void extract_class_comment() {
        docWriter.write(includeSourceWithTag("classWithComment"), "", "");

        format_comment_extracted(".Comment extracted from class",
                CodeExtractor.getComment(ClassWithCommentToExtract.class));

    }

    @Test
    public void extract_method_comment() {
        docWriter.write(includeSourceWithTag("classWithComment"), "", "");

        {
            docWriter.write("To extract comment of a method",
                    includeSourceWithTag("CodeExtractor_extract_method_comment"), "", "");

            // tag::CodeExtractor_extract_method_comment[]
            final Optional<String> comment = CodeExtractor.getComment(ClassWithCommentToExtract.class, "methodWithoutParameters");
            // end::CodeExtractor_extract_method_comment[]

            format_comment_extracted("From method without arguments.",
                    comment.orElse(""));
        }
        {
            docWriter.write("To extract comment of a method with parameters",
                    includeSourceWithTag("CodeExtractor_extract_comment_from_method_with_parameters"), "", "");

            // tag::CodeExtractor_extract_comment_from_method_with_parameters[]
            final Optional<String> comment = CodeExtractor.getComment(ClassWithCommentToExtract.class, "methodWithParameters",
                    CodeExtractor.getJavaClasses(int.class, String.class));
            // end::CodeExtractor_extract_comment_from_method_with_parameters[]

            format_comment_extracted("From method with parameters.",
                    comment.orElse(""));
        }
    }

    @Test
    public void extract_method_comment_using_method_object() throws NoSuchMethodException {
        docWriter.write(includeSourceWithTag("classWithComment"), "", "");

        docWriter.write("To extract comment of a method using method object",
                includeSourceWithTag("CodeExtractor_extract_method_comment_using_method_object"), "", "");

        // tag::CodeExtractor_extract_method_comment_using_method_object[]
        final Method methodWithComment = ClassWithCommentToExtract.class.getMethod("methodWithoutParameters");
        final Optional<String> comment = CodeExtractor.getComment(methodWithComment);
        // end::CodeExtractor_extract_method_comment_using_method_object[]

        format_comment_extracted("From method.",
                comment.orElse(""));
    }

    public void format_comment_extracted(String description, String commentExtracted) {
        docWriter.write(
                "." + description,
                "----",
                commentExtracted,
                "----", "", "");
    }


    public String includeSourceWithTag(String tag) {
        final Class<? extends CodeExtractorTest> aClass = getClass();
        return includeSourceWithTag(tag, aClass);
    }

    public String includeSourceWithTag(String tag, Class<?> aClass) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::../../../../java/%s.java[tag=%s]",
                        aClass.getName().replace(".", "/"),
                        tag),
                "----");
    }

}

// tag::classWithComment[]
/**
 * Comment of the class.
 */
class ClassWithCommentToExtract {

    /**
     * Comment of the method without parameters.
     */
    public void methodWithoutParameters() {

    }

    /**
     * Comment from the method with parameters.
     */
    public void methodWithParameters(int id, String name) {

    }

}
// end::classWithComment[]
