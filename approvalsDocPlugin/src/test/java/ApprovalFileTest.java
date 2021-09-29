import org.demo.DemoTest;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ApprovalFileTest {

    @Test
    public void open_test_file_from_java_method() throws NoSuchMethodException {
        final Method testMethod = DemoTest.class.getMethod("a_simple_test");
        ApprovalFile filename = ApprovalFile.valueOf(testMethod);
        assertEquals("org/demo/_DemoTest.a_simple_test.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getName());
    }

    @Test
    public void open_test_file_from_java_class() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromClass("org.demo", "DemoTest");
        assertEquals("org/demo/_DemoTest.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getName());
        assertEquals("_DemoTest.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getFileName());
    }

    @Test
    public void open_test_file_from_java_class_without_package() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromClass("", "DemoTest");
        assertEquals("_DemoTest.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getName());
    }

    @Test
    public void get_received_file_from_java_method_with_string() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromMethod("org.demo", "DemoTest", "a_simple_test");
        assertEquals("org/demo/_DemoTest.a_simple_test.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getName());
        assertEquals("_DemoTest.a_simple_test.received.adoc", filename.to(ApprovalFile.Status.RECEIVED).getFileName());
    }

    @Test
    public void get_approved_file_from_java_method_with_string() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromMethod("org.demo", "DemoTest", "a_simple_test");
        assertEquals("org/demo/_DemoTest.a_simple_test.approved.adoc", filename.to(ApprovalFile.Status.APPROVED).getName());
    }

    @Test
    public void get_approved_file_from_java_method_without_package() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromMethod("", "DemoTest", "a_simple_test");
        assertEquals("_DemoTest.a_simple_test.approved.adoc", filename.to(ApprovalFile.Status.APPROVED).getName());
    }

    @Test
    public void open_test_file() {
        ApprovalFile filename = ApprovalFile.valueOf("test.received.adoc").get();
        assertEquals("Test.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path() {
        ApprovalFile filename = ApprovalFile.valueOf("src/test/docs/org/demo/test.received.adoc").get();
        assertEquals("src/test/java/org/demo/Test.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path_and_underscore() {
        ApprovalFile filename = ApprovalFile.valueOf("src/test/docs/org/demo/_test_underscore.received.adoc").get();
        assertEquals("src/test/java/org/demo/Test_underscore.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path_and_method() {
        ApprovalFile filename = ApprovalFile.valueOf("src/test/docs/org/demo/test.mytestcase.received.adoc").get();
        assertEquals("src/test/java/org/demo/Test.java", filename.getTestFile());
    }

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

    @Test
    public void approve_a_received_file_with_multi_parts_name() {
        ApprovalFile filename = ApprovalFile.valueOf("test.file.multi_part.received.adoc").get().to(ApprovalFile.Status.APPROVED);
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("test.file.multi_part.approved.adoc", filename.getName());
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