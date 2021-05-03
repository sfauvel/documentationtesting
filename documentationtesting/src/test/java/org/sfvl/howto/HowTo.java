package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.*;
import org.sfvl.doctesting.utils.*;
import org.sfvl.doctesting.writer.Classes;

import java.lang.reflect.Method;
import java.util.Arrays;

public class HowTo {

    private static final DocWriter doc = new DocWriter() {
        public String formatOutput(String displayName, Method testMethod) {
            return ":toc: left\n" +
                    ":nofooter:\n" +
                    ":stem:\n" +
                    ":source-highlighter: rouge\n" +
                    super.formatOutput(displayName, testMethod);
        }
    };

    @RegisterExtension
    static ApprovalsExtension extension = new ApprovalsExtension(doc) {
        @Override
        public void afterAll(ExtensionContext extensionContext) throws Exception {

        }
    };

    private final AsciidocFormatter formatter = new AsciidocFormatter();

    @Test
    public void how_to() {
        doc.write(formatter.paragraphSuite(
                getInclude(ApprovalsExtensionTest::using_extension),
                getInclude(CreateADocument.class),
                getInclude(UseYourOwnStyle.class)));
    }

    public String getInclude(Class aClass) {
        return new Classes(formatter).includeClasses(DocumentationNamer.toPath(aClass.getPackage()), Arrays.asList(aClass)).trim();
    }

    public <T> String getInclude(FindLambdaMethod.SerializableConsumer<T> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);
        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, method);
        return formatter.include(
                documentationNamer.getApprovedPath(Config.DOC_PATH.resolve(DocumentationNamer.toPath(this.getClass().getPackage()))).toString());

    }
}