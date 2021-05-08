package org.sfvl.howto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.writer.Options;

public class InstallingLibrary {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    Formatter formatter = new AsciidocFormatter();

    public String build() {
        return formatter.paragraphSuite(
                new Options(formatter).withCode(),
                formatter.title(1, "Installing Documentation testing"),
                getContent()
        );
    }

    @Test
    public void Installing_documentation_testing() {
        doc.write(getContent());
    }

    protected String getContent() {
        final String dependency = "        <dependency>\n" +
                "            <groupId>org.sfvl</groupId>\n" +
                "            <artifactId>documentationtesting</artifactId>\n" +
                "            <version>${documentationtesting.version}</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>";

        return "We use the library link:https://github.com/sfauvel/documentationtesting[documentationtesting]\n" +
                "which provides what we need to get started.\n" +
                "For a complete description of how to use it go to the link:https://sfauvel.github.io/documentationtesting/documentationtesting[documentation site]\n" +
                "\n" +
                "The library is not yet on a public repository.\n" +
                "To use it, you need to download and install it.\n" +
                "\n" +
                "You have to download `.jar` and `.pom` from\n" +
                "link:https://github.com/sfauvel/documentationtesting/packages/538792[last Documentation Testing release]\n" +
                "\n" +
                "Run this maven command to install it:\n" +
                "----\n" +
                "mvn install:install-file -Dfile=<path-to-file>.jar -DpomFile=<path-to-pomfile>.pom\n" +
                "----\n" +
                "\n" +
                "\n" +
                "Then, you can add the dependency to your `pom.xml` to use it in your project" +
                "\n" +
                formatter.sourceCodeBuilder("xml")
                        .indent(0)
                        .source(dependency)
                        .build();
    }


}
