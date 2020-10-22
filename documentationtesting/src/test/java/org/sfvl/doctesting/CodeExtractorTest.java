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
    public void extract_code_from_class() {


        docWriter.write("To extract code of a class",
                includeSourceWithTag("CodeExtractor_getCode"), "", "");


        docWriter.write(".Source code from file",
                includeSourceWithTag("classToExtract"),
                "", "");

        // tag::CodeExtractor_getCode[]
        String code = CodeExtractor.getCode(SimpleClass.class);
        // end::CodeExtractor_getCode[]

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


    public String includeSourceWithTag(String tag) {
        return String.join("\n",
                "[source, java, indent=0]",
                "----",
                String.format("include::../../../../java/%s.java[tag=%s]",
                        getClass().getName().replace(".", "/"),
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

// tag::classToExtract[]
class SimpleClass {
    public int simpleMethod() {
        return 0;
    }
}
// end::classToExtract[]
