package org.sfvl.doctesting.junitinheritance;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.test_tools.FastApprovalsExtension;

class DocAsTestBaseTest {

    @RegisterExtension
    static ApprovalsExtension extension = new FastApprovalsExtension();

    DocAsTestBase docAsTest = new DocAsTestBase() {
        @Override
        protected void approvalAfterTestSpecific(String content, DocPath docPath) throws Exception {
        }
    };

}