import org.junit.Test;

import static org.junit.Assert.*;

public class ApprovalFileTest {

    @Test
    public void value_of_a_received_file() {
        ApprovalFile filename = ApprovalFile.valueOf("test.received.adoc").get();
        assertTrue(filename.isReceived());
        assertFalse(filename.isApproved());
        assertEquals("test.received.adoc", filename.getName());
    }

    @Test
    public void build_a_received_file() {
        ApprovalFile filename = new ApprovalFile("test", ApprovalFile.Status.RECEIVED, "adoc");
        assertTrue(filename.isReceived());
        assertFalse(filename.isApproved());
        assertEquals("test.received.adoc", filename.getName());
    }

    @Test
    public void value_of_an_approved_file() {
        ApprovalFile filename = ApprovalFile.valueOf("test.approved.adoc").get();
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("test.approved.adoc", filename.getName());
    }

    @Test
    public void build_an_approved_file() {
        ApprovalFile filename = new ApprovalFile("test", ApprovalFile.Status.APPROVED, "adoc");
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("test.approved.adoc", filename.getName());
    }

    @Test
    public void approval_filename_could_have_any_extension() {
        assertEquals("test.approved.txt", new ApprovalFile("test", ApprovalFile.Status.APPROVED, "txt").getName());
        assertEquals("test.received.txt", new ApprovalFile("test", ApprovalFile.Status.RECEIVED, "txt").getName());
        assertEquals("test.approved.txt", ApprovalFile.valueOf("test.approved.txt").get().getName());
    }

    @Test
    public void value_of_an_approved_file_with_dot_in_name() {
        ApprovalFile filename = ApprovalFile.valueOf("test.multi.part.approved.adoc").get();
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("test.multi.part.approved.adoc", filename.getName());
    }

    @Test
    public void valueOf_an_invalid_filename_shoulr_return_empty() {
        assertFalse(ApprovalFile.valueOf("test.adoc").isPresent());
        assertFalse(ApprovalFile.valueOf("test.invalid.adoc").isPresent());
    }

    @Test
    public void approve_a_received_file() {
        ApprovalFile filename = ApprovalFile.valueOf("test.received.adoc").get().to(ApprovalFile.Status.APPROVED);
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("test.approved.adoc", filename.getName());
    }

    private void assertThrow(Class<? extends Exception> expectedException, Runnable function) {
        try {
            function.run();
        } catch(Exception e) {
            if (expectedException.isAssignableFrom(e.getClass())) {
                return;
            }
            throw e;
        }
        fail("No exception was thrown, " + expectedException.getName() + " expected");

    }


}