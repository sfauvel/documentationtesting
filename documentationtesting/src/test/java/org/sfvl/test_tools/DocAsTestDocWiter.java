package org.sfvl.test_tools;

import org.sfvl.codeextraction.ClassFinder;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.writer.ClassDocumentation;
import org.sfvl.doctesting.writer.DocWriter;
import org.sfvl.howto.HowTo;

import java.lang.reflect.Method;
import java.nio.file.Paths;

public class DocAsTestDocWiter extends DocWriter<DocFormatter> {
    private final boolean generateHtmlPage;

    public DocAsTestDocWiter() {
        this(false);
    }
    public DocAsTestDocWiter(boolean generateHtmlPage) {
        super(new DocFormatter());
        this.generateHtmlPage = generateHtmlPage;
    }

    public String linkToClass(Class<?> clazz) {
        final String title = new ClassDocumentation(null).getTestClassTitle(clazz);

        return linkToClass(clazz, title);
    }

    public String linkToClass(Class<?> clazz, String title) {
        return linkTo(clazz, null, title);
    }

    public String linkToMethod(Method method, String title) {
        final Class<?> clazz = new ClassFinder().getMainFileClass(method.getDeclaringClass());
        return linkTo(clazz, titleId(method), title);
    }

    public String linkTo(Class clazz, String anchor, String title) {
        final DocPath docPath = new DocPath(clazz);
        // TODO do we generate page here ? It's not really the role of link formatting.
        generatePage(clazz);

        final String address = DocPath.toAsciiDoc(Paths.get("{" + Config.DOC_PATH_TAG + "}").resolve(docPath.html().path()));
        return getFormatter().linkToPage(address, anchor, title);
    }

    private void generatePage(Class<?> clazz) {
        if (generateHtmlPage) {
            new IntermediateHtmlPage().generate(clazz);
        }
    }
}
