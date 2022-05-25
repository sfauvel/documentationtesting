package org.sfvl.test_tools;

import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.OnePath;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.Classes;
import org.sfvl.howto.HowTo;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;

public class DocFormatter extends AsciidocFormatter {
    private final boolean generateHtmlPage;

    public DocFormatter() {
        this(false);
    }
    public DocFormatter(boolean generateHtmlPage) {
        this.generateHtmlPage = generateHtmlPage;
    }

    public String getInclude(Class aClass, int offset) {
        return new Classes(this).includeClasses(CodePath.toPath(aClass.getPackage()), Arrays.asList(aClass), offset).trim();
    }

    public <T> String getInclude(MethodReference.SerializableConsumer<T> methodToInclude, int offset) {
        final Method method = MethodReference.getMethod(methodToInclude);

        final OnePath approvedPath = new DocPath(method).approved();
        return include(approvedPath.from(new DocPath(this.getClass()).approved()).toString(), offset);
    }
//
//    public String linkToClass(Class<?> clazz) {
//        final String title = new ClassDocumentation(null).getTestClassTitle(clazz);
//
//        return linkToClass(clazz, title);
//    }
//
//    public String linkToClass(Class<?> clazz, String title) {
//        return linkTo(clazz, null, title);
//    }
//
//    public String linkToMethod(Method method, String title) {
//        final Class<?> clazz = new ClassFinder().getMainFileClass(method.getDeclaringClass());
//        return linkTo(clazz, HowTo.doc.getDocWriter().titleId(method), title);
//    }
//
//    public String linkTo(Class clazz, String anchor, String title) {
//        final DocPath docPath = new DocPath(clazz);
//        // TODO do we generate page here ? It's not really the role of link formatting.
//        generatePage(clazz);
//
//        final String address = DocPath.toAsciiDoc(Paths.get("{" + Config.DOC_PATH_TAG + "}").resolve(docPath.html().path()));
//        return linkToPage(address, anchor, title);
//    }
//
//    private void generatePage(Class<?> clazz) {
//        if (generateHtmlPage) {
//            new IntermediateHtmlPage().generate(clazz);
//        }
//    }
}
