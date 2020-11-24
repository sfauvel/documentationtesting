package org.sfvl.doctesting.junitinheritance;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.DocWriter;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

class DocAsTestBaseTest {

    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(new DocWriter());

    DocAsTestBase docAsTest = new DocAsTestBase() {
        @Override
        protected void approvalAfterTestSpecific(String content, DocumentationNamer documentationNamer) throws Exception {
        }
    };

}