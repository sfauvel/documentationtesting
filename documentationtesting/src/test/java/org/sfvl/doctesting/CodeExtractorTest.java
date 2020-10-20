package org.sfvl.doctesting;

import com.thoughtworks.qdox.model.JavaClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.DocWriter;

import java.util.Arrays;

/**
 * Extract information from code.
 */
class CodeExtractorTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void extract_class_comment() {
        docWriter.write(
                "[source, java]",
                "----",
                "include::../../../../java/org/sfvl/doctesting/CodeExtractorTest.java[tag=classWithComment]",
                "----", "", "");

        format_comment_extracted(".Comment extracted from class",
                CodeExtractor.getComment(ClassWithCommentToExtract.class));

    }

    @Test
    public void extract_method_comment() {
        docWriter.write(
                "[source, java]",
                "----",
                "include::../../../../java/org/sfvl/doctesting/CodeExtractorTest.java[tag=classWithComment]",
                "----", "", "");

        format_comment_extracted("From method without arguments.",
                CodeExtractor.getComment(ClassWithCommentToExtract.class, "methodWithoutParameters"));

        final JavaClass intJavaClass = CodeExtractor.getBuilder().getClassByName(int.class.getCanonicalName());
        final JavaClass stringJavaClass = CodeExtractor.getBuilder().getClassByName(String.class.getCanonicalName());
        format_comment_extracted("From method with parameters.",
                CodeExtractor.getComment(ClassWithCommentToExtract.class, "methodWithParameters",
                        Arrays.asList(intJavaClass, stringJavaClass)));
    }

    public void format_comment_extracted(String description, String commentExtracted) {
        docWriter.write(
                "." + description,
                "----",
                commentExtracted,
                "----", "", "");
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