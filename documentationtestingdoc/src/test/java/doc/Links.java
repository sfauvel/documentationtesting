package doc;

import org.sfvl.docformatter.asciidoc.AsciidocFormatter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Links {

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface VideoLinkAnnotation {
    }

    public static class VideoLink {

        private final String link;
        private final String description;

        public VideoLink(String link, String description) {
            this.link = link;
            this.description = description;
        }

        public String getLink() {
            return link;
        }

        public String getDescription() {
            return description;
        }
    }

    public static VideoLink humanTalks = new VideoLink(
            "https://www.youtube.com/watch?v=1slYI-dBMcc",
            "Human Talks Nantes 2022 (10mn in french)");

    public static VideoLink bdxIO = new VideoLink(
            "https://youtu.be/AQDILnknTJ0",
            "Bdx I/O 2022 (43mn in french)");

    private static AsciidocFormatter formatter = new AsciidocFormatter();

    public static String VideoDocAsTest() {
        return VideoDocAsTest("video (10mn in french)");
    }

    public static String VideoDocAsTest(String text) {
        return new AsciidocFormatter().linkToPage(humanTalks.getLink(), text);
    }

    public static String VideoBdxIO() {
        return VideoBdxIO("Video BDX I/O (43mn in french)");
    }

    public static String VideoBdxIO(String text) {
        return new AsciidocFormatter().linkToPage(bdxIO.getLink(), text);
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
