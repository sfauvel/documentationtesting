package org.sfvl.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocWriter;

@NotIncludeToDoc
@org.sfvl.doctesting.test_tools.OnlyRunProgrammatically
public
// tag::MyTest[]
class MyTest {
    private static final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    static final ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    @Test
    public void test_A() {
        docWriter.write("In my *test*");
    }

}
// end::MyTest[]
