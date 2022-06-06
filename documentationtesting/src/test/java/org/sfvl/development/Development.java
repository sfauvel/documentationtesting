package org.sfvl.development;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractionPackage;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.NoTitle;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.test_tools.DocAsTestDocWiter;

public class Development extends PackagePage {
    private static final DocAsTestDocWiter docAsTestDocWiter = new DocAsTestDocWiter();
    @RegisterExtension
    static ApprovalsExtension doc = new ApprovalsExtension(docAsTestDocWiter);

    @Test
    public void introduction() {
        doc.write("This section gives some information for developers who work on this library.",
                "We describe architecture, development tools, how we organize tests and documentation, ...",
                "",
                "We follow some general principals to develop this tool:",
                "",
                "* Everything is test using the `doc as test` approach.",
                "* We avoid to write a value as String when we can extract it from code.");
    }

    @Nested
    class Principles {
        @Test
        public void extract_from_code() {
            // >>>extract_name
            final String className = Development.class.getSimpleName();
            final String methodName = MethodReference.getName(Development::introduction);
            String text = String .format("The method `%s` is a method of the class `%s`", methodName, className);
            // <<<extract_name

            doc.write("We try to extract the most things from the code.",
                    "In consequence, when refering to a class or a method, ",
                    "we don't write directly it's name in the text but we use the java class or the method reference.",
                    " We ensure that we can't have a name that not exist in the code.",
                    "",
                    ".Extraction of class name and method name",
                    doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("extract_name")),
                    "",
                    "For more detail to get method name with reference, see: " + docAsTestDocWiter.linkToClass(CodeExtractionPackage.class, MethodReference.class.getSimpleName() + " documentation")
//                    "You can see  " + CodeExtractor.
                    );
        }
    }

    @Test
    @NoTitle
    public void classes_to_include() {
        final Classes classesBuilder = new Classes(doc.getFormatter());
        doc.write(build_classes_to_include(classesBuilder));
    }

}
