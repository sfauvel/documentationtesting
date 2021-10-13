package org.sfvl.samples.generateNestedHtml;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.doctesting.NotIncludeToDoc;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.test_tools.OnlyRunProgrammatically;

@NotIncludeToDoc
@OnlyRunProgrammatically
@ExtendWith(HtmlPageExtension.class)
public class HtmlNestedTest {
    @RegisterExtension
    static final ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void test_A() {
        doc.write("In my *test*");
    }

    @Nested
    public class HtmlNestedClassTest {
        @Test
        public void test_in_nested_class() {
            doc.write("In my *test* in nested class");
        }
    }

}
