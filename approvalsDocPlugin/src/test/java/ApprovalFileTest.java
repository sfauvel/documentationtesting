import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ApprovalFileTest {

    @Test
    public void create_approval_java_file_from_java_class() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromClass("org.demo", "DemoTest");
        assertEquals("org/demo/DemoTest.java", filename.getName());
        assertEquals("DemoTest.java", filename.getFileName());
    }

    @Test
    public void create_approval_java_file_from_java_method_with_string() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromMethod("org.demo", "DemoTest", "a_simple_test");
        assertEquals("org/demo/DemoTest.java", filename.getName());
        assertEquals("DemoTest.java", filename.getFileName());
    }

    @Test
    public void create_approval_java_file_from_java_class_without_package() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromClass("", "DemoTest");
        assertEquals("DemoTest.java", filename.getName());
    }

    @Test
    public void create_approval_java_file_from_java_inner_class_without_package() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromClass("", "DemoTest.InnerClass");
        assertEquals("DemoTest.java", filename.getFileName());
        assertEquals("DemoTest.java", filename.getName());
    }

    @Test
    public void convert_from_java_to_approved() throws NoSuchMethodException {
        ApprovalFile filename = ApprovalFile.fromMethod("org.demo", "DemoTest", "a_simple_test");
        final ApprovalFile approvedFile = filename.to(ApprovalFile.Status.APPROVED);
        assertEquals("org/demo/_DemoTest.a_simple_test.approved.adoc", approvedFile.getName());
        assertEquals("_DemoTest.a_simple_test.approved.adoc", approvedFile.getFileName());
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
        ApprovalFile filename = ApprovalFile.valueOf("_Test.received.adoc").get();
        assertEquals("Test.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path() {
        ApprovalFile filename = ApprovalFile.valueOf("org/demo/_Test.received.adoc").get();
        assertEquals("org/demo/Test.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path_and_underscore() {
        ApprovalFile filename = ApprovalFile.valueOf("org/demo/_Test_Underscore.received.adoc").get();
        assertEquals("org/demo/Test_Underscore.java", filename.getTestFile());
    }

    @Test
    public void open_test_file_with_path_and_method() {
        ApprovalFile filename = ApprovalFile.valueOf("org/demo/_Test.mytestcase.received.adoc").get();
        assertEquals("org/demo/Test.java", filename.getTestFile());
    }

    @Test
    public void value_of_a_received_file() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.received.adoc").get();
        assertTrue(filename.isReceived());
        assertFalse(filename.isApproved());
        assertEquals("_test.received.adoc", filename.getName());
    }

    @Test
    public void build_a_received_file() {
        ApprovalFile filename = new ApprovedFile("test", ApprovalFile.Status.RECEIVED);
        assertTrue(filename.isReceived());
        assertFalse(filename.isApproved());
        assertEquals("_test.received.adoc", filename.getName());
    }

    @Test
    public void value_of_an_approved_file() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.approved.adoc").get();
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("_test.approved.adoc", filename.getName());
    }

    @Test
    public void build_an_approved_file() {
        ApprovalFile filename = new ApprovedFile("test", ApprovalFile.Status.APPROVED);
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("_test.approved.adoc", filename.getName());
    }

    @Test
    public void value_of_an_approved_file_with_dot_in_name() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.multi.part.approved.adoc").get();
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("_test.multi.part.approved.adoc", filename.getName());
    }

    @Test
    public void valueOf_an_invalid_filename_should_return_empty() {
        final String NO_APPROVED_KEYWORD = "_test.adoc";
        assertFalse(ApprovalFile.valueOf(NO_APPROVED_KEYWORD).isPresent());
        final String NO_UNDERSCORE = "test.approved.adoc";
        assertFalse(ApprovalFile.valueOf(NO_UNDERSCORE).isPresent());
        final String INVALID_APPROVED_KEYWORD = "test.invalid.adoc";
        assertFalse(ApprovalFile.valueOf(INVALID_APPROVED_KEYWORD).isPresent());
    }

    @Test
    public void approve_a_received_file() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.received.adoc").get().to(ApprovalFile.Status.APPROVED);
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("_test.approved.adoc", filename.getName());
    }

    @Test
    public void approve_a_received_file_with_multi_parts_name() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.file.multi_part.received.adoc").get().to(ApprovalFile.Status.APPROVED);
        assertFalse(filename.isReceived());
        assertTrue(filename.isApproved());
        assertEquals("_test.file.multi_part.approved.adoc", filename.getName());
    }

    @Test
    public void extract_class_from_received_file() {
        ApprovalFile approvalFile = ApprovalFile.valueOf("_MyClass.received.adoc").get();
        assertEquals("MyClass", approvalFile.getClassName());
    }

    @Test
    public void extract_package_from_received_file() {
        ApprovalFile approvalFile = ApprovalFile.valueOf("org/demo/_MyClass.received.adoc").get();
        assertEquals("MyClass", approvalFile.getClassName());
        assertEquals(Paths.get("org", "demo"), approvalFile.getPath());
    }

    @Test
    public void extract_method_from_received_file() {
        ApprovalFile approvalFile = ApprovalFile.valueOf("_MyClass.test_method.received.adoc").get();
        assertEquals("MyClass", approvalFile.getClassName());
        assertEquals("test_method", approvalFile.getMethodName());
    }

    @Test
    public void extract_method_from_received_file_and_transform_to_java() {
        ApprovalFile approvalFile = ApprovalFile.valueOf("_MyClass.test_method.received.adoc").get().toJava();
        assertEquals("MyClass", approvalFile.getClassName());
        assertEquals("test_method", approvalFile.getMethodName());
    }

    @Test
    public void extract_class_from_received_file_with_multi_parts_name() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.file.multi_part.myMethod.received.adoc").get();
        assertEquals("test.file.multi_part", filename.getClassName());
    }

    @Test
    public void extract_class_from_java_file_with_multi_parts_name_from_received() {
        ApprovalFile filename = ApprovalFile.valueOf("_test.file.multi_part.myMethod.received.adoc").get().toJava();
        assertEquals("test.file.multi_part", filename.getClassName());
    }

    private void assertThrow(Class<? extends Exception> expectedException, Runnable function) {
        try {
            function.run();
        } catch (Exception e) {
            if (expectedException.isAssignableFrom(e.getClass())) {
                return;
            }
            throw e;
        }
        fail("No exception was thrown, " + expectedException.getName() + " expected");

    }


}