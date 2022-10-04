package doc;

import org.sfvl.docformatter.asciidoc.AsciidocFormatter;

public class Links {

    private static AsciidocFormatter formatter = new AsciidocFormatter();

    public static String VideoDocAsTest() {
        return VideoDocAsTest("video (10mn in french)");
    }

    public static String VideoDocAsTest(String text) {
        return new AsciidocFormatter().linkToPage("https://www.youtube.com/watch?v=1slYI-dBMcc", text);
    }

    public static String PythonDocAsTest() {
        return PythonDocAsTest("doc_as_test_pytest");
    }

    public static String PythonDocAsTest(String text) {
        return new AsciidocFormatter().linkToPage("https://github.com/sfauvel/doc_as_test_pytest", text);
    }

    public static String JavaDocAsTest() {
        return JavaDocAsTest("DocumentationTesting");
    }

    public static String JavaDocAsTest(String text) {
        return formatter.linkToPage("{github-pages}/documentationtesting", text);
    }

}
