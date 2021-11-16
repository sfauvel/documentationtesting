package org.sfvl.demo;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.PathProvider;
import org.sfvl.doctesting.writer.Options;

import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(DemBaseClass.HtmlIndex.class)
public class DemBaseClass {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    public static class HtmlIndex extends HtmlPageExtension {
        final AsciidocFormatter formatter = new AsciidocFormatter();
        private final String TITLE = "Using Approvals";
        private final String filename = "index.adoc";

        protected String getHeader() {
            final Path readmePath = new PathProvider().getProjectPath().resolve(Paths.get("readme.adoc"));
            return "\n" + (readmePath.toFile().exists()
                    ? "include::../../../readme.adoc[leveloffset=+1]"
                    : formatter.title(1, TITLE)) + "\n";
        }

        private String generalNote(Path projectFolderPath) {
            return String.join("\n",
                    "NOTE: The examples shown here are generated from the source code.",
                    "They therefore represent the behavior of the application at any times.",
                    "Non regression is ensured by checking the absence of change in this document.",
                    "Learn more here link:{github-pages}[]",
                    "",
                    "View source of project on link:{github-repo}/" + projectFolderPath.toString() + "[Github]");
        }

        @Override
        public String content(Class<?> clazz) {
            final PathProvider pathProvider = new PathProvider();
            final Path projectFolderPath = pathProvider.getGitRootPath().relativize(pathProvider.getProjectPath());
            return formatter.paragraphSuite(
                    new Options(formatter).withCode(),
                    getHeader(),
                    generalNote(projectFolderPath),
                    String.format("include::%s[leveloffset=+1]", new DocPath(clazz).approved().from(getFilePath(clazz).getParent()))
            );
        }

        @Override
        public Path getFilePath(Class<?> clazz) {
            return Config.DOC_PATH.resolve(Paths.get(filename));
        }
    }
}
