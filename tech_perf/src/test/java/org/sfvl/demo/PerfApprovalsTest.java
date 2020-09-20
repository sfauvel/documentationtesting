package org.sfvl.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sfvl.doctesting.ApprovalsBase;

/**
 * Demo of a simple usage to generate documentation.
 */
@DisplayName("Verify documentation with _Approvals_ library.")
public class PerfApprovalsTest extends ApprovalsBase {

    @Test
    public void should_give_person_information() {
        BasicDocumentation.generateTestDocumentation(this);
    }

}
