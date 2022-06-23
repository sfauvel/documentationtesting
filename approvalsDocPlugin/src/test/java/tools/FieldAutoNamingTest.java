package tools;

import junit.framework.TestCase;

public class FieldAutoNamingTest extends TestCase {

    public static class FileNames extends FieldAutoNaming {
        public String folder_fileA_approved_adoc;
        public String folder_fileA_multi_dot_approved_adoc;
        public String folder_subfolder_fileA_approved_adoc;
    }

    public FileNames files = new FileNames();

    public void test_naming_simple_file() {
        assertEquals("folder/_FileA.approved.adoc", files.folder_fileA_approved_adoc);
    }
    public void test_naming_file_with_multi_dot() {
        assertEquals("folder/_FileA.multi.dot.approved.adoc", files.folder_fileA_multi_dot_approved_adoc);
    }
    public void test_naming_file_in_subfolder() {
        assertEquals("folder/subfolder/_FileA.approved.adoc", files.folder_subfolder_fileA_approved_adoc);
    }

}