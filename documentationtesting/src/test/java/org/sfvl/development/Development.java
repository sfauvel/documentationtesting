package org.sfvl.development;

import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.utils.NoTitle;

public class Development extends PackagePage {

    @Test
    public void introduction() {
        doc.write("This section gives some information for developers who work on this library.",
                "We describe architecture, development tools, how we organize tests and documentation, ...",
                "",
                "We follow some general principals to develop this tool:",
                "",
                "* Everything is test using the `doc as test` approach.",
                "* We avoid to write a value as String when we can extract it.");
    }

    @Test
    @NoTitle
    public void classes_to_include() {
        super.classes_to_include();
    }

}
