package org.sfvl.doctesting.junitinheritance;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocWriter;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;

class DocAsTestBaseTest {

    @RegisterExtension
    static ApprovalsExtension extension = new SimpleApprovalsExtension();

    DocAsTestBase docAsTest = new DocAsTestBase() {
        @Override
        protected void approvalAfterTestSpecific(String content, DocumentationNamer documentationNamer) throws Exception {
        }
    };

}