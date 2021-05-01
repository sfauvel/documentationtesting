package org.sfvl.howto;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtensionTest;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.doctesting.writer.Document;
import org.sfvl.doctesting.writer.DocumentProducer;
import org.sfvl.doctesting.writer.Options;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class HowToDocumentation implements DocumentProducer {

    protected final Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "How to"),
                getIncludes()
        );
    }

    public String getIncludes() {
        return formatter.paragraphSuite(
                getInclude(ApprovalsExtensionTest::using_extension),
                getInclude(CreateADocument.class),
                getInclude(UseYourOwnStyle.class));

    }

    public String getInclude(Class aClass) {
        return new Classes(formatter).includeClasses(DocumentationNamer.toPath(aClass.getPackage()), Arrays.asList(aClass)).trim();
    }

    public String getInclude(FindLambdaMethod.SerializableConsumer<ApprovalsExtensionTest> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);
        final DocumentationNamer documentationNamer = new DocumentationNamer(Config.DOC_PATH, method);
        return formatter.include(
                documentationNamer.getApprovedPath(Config.DOC_PATH.resolve(DocumentationNamer.toPath(this.getClass().getPackage()))).toString());

    }

    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(this.getClass());
    }

    public static void main(String... args) throws IOException {
        new HowToDocumentation().produce();
    }

}