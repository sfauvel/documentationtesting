package tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import docAsTest.DocAsTestAction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MockActionOnFileEventTest extends DocAsTestPlatformTest {
    MockActionOnFileEvent actionEvent;
    FileHelper fileHelper;

    public void setUp() throws Exception {
        super.setUp();
        fileHelper = new FileHelper(myFixture);
        actionEvent = new MockActionOnFileEvent(myFixture);
    }

    public void test_perform_update_without_files() {
        PsiFile psiFile = myFixture.addFileToProject("file.txt", "content");

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_without_file(actionEvent.getDataContext()),
                actionEvent -> assert_first_update_call_without_file(actionEvent.getDataContext()));

        actionEvent.performUpdate(action, Arrays.asList());

        assertEquals(2, action.getCalls());
    }


    public void test_perform_update_call_update_twice_with_different_mock() {
        PsiFile psiFile = myFixture.addFileToProject("file.txt", "content");

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call(actionEvent.getDataContext()),
                actionEvent -> assert_second_update_call(actionEvent.getDataContext()));

        actionEvent.performUpdate(action, psiFile);

        assertEquals(2, action.getCalls());
    }


    public void test_perform_update_call_update_twice_with_different_mock_on_multi_files() {
        PsiFile psiFileA = myFixture.addFileToProject("fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("fileB.txt", "content");
        PsiFile psiFileC = myFixture.addFileToProject("fileC.txt", "content");

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_multi_files(actionEvent.getDataContext(), 3),
                actionEvent -> assert_second_update_call_multi_files(actionEvent.getDataContext(), 3));

        actionEvent.performUpdate(action, Arrays.asList(psiFileA, psiFileB, psiFileC));

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_folder() {

        PsiFile psiFileA = myFixture.addFileToProject("folder/fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("folder/fileB.txt", "content");
        PsiFile psiFileC = myFixture.addFileToProject("folder/fileC.txt", "content");
        PsiDirectory folder = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folder"));

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_one_folder(actionEvent.getDataContext(), "folder"),
                actionEvent -> assert_second_update_call_one_folder(actionEvent.getDataContext(), "folder"));

        actionEvent.performUpdate(action, folder);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_multi_folders() {

        PsiFile psiFileA = myFixture.addFileToProject("folderA/fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("folderB/fileB.txt", "content");
        PsiDirectory folderA = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderA"));
        PsiDirectory folderB = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderB"));

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_multi_folders(actionEvent.getDataContext(), "folderA", "folderB"),
                actionEvent -> assert_second_update_call_multi_folders(actionEvent.getDataContext(), "folderA", "folderB"));

        actionEvent.performUpdate(action, Arrays.asList(folderA, folderB));

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_mixte_files_folders_file_first() {

        PsiFile psiFileA = myFixture.addFileToProject("folderA/fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("folderB/fileB.txt", "content");
        PsiFile psiFileX = myFixture.addFileToProject("folderX/filex.txt", "content");
        PsiDirectory folderA = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderA"));
        PsiDirectory folderB = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderB"));

        final List<PsiFileSystemItem> psiItems = Arrays.asList(psiFileB, folderA, psiFileX);
        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_mixte_files_folders(actionEvent.getDataContext(), psiItems),
                actionEvent -> assert_second_update_call_mixte_files_folders_file_first(actionEvent.getDataContext(), psiItems));

        actionEvent.performUpdate(action, psiItems);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_mixte_files_folders_folder_first() {

        PsiFile psiFileA = myFixture.addFileToProject("folderA/fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("folderB/fileB.txt", "content");
        PsiFile psiFileX = myFixture.addFileToProject("folderX/filex.txt", "content");
        PsiDirectory folderA = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderA"));
        PsiDirectory folderB = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderB"));

        final List<PsiFileSystemItem> psiItems = Arrays.asList(folderA, psiFileB, psiFileX);
        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_mixte_files_folders(actionEvent.getDataContext(), psiItems),
                actionEvent -> assert_second_update_call_mixte_files_folders(actionEvent.getDataContext(), psiItems));

        actionEvent.performUpdate(action, psiItems);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_editor_outside_class() {
        final String selectedClassName = "SelectedClass";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, FileHelper.CaretOn.NONE);

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_outside_class_on_editor(actionEvent.getDataContext(), psiFile),
                actionEvent -> assert_second_update_call_outside_class_on_editor(actionEvent.getDataContext(), psiFile));

        actionEvent.performUpdateOnEditor(action, myFixture, psiFile);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_editor_on_class() {
        final String selectedClassName = "SelectedClass";
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, FileHelper.CaretOn.CLASS);

       final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_on_class_on_editor(actionEvent.getDataContext(), psiFile),
                actionEvent -> assert_second_update_call_on_class_on_editor(actionEvent.getDataContext(), psiFile, selectedClassName));

        actionEvent.performUpdateOnEditor(action, myFixture, psiFile);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_update_call_update_twice_with_different_mock_on_editor_on_method() {
        final String selectedClassName = "MyClass";
        final String selectedMethod = "SelectedMethod";
        final String code = new FileHelper.CodeGenerator()
                .withMethod("SelectedMethod")
                .generate(FileHelper.CaretOn.METHOD);
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, code);

        final CheckApproveFileAction action = new CheckApproveFileAction(
                actionEvent -> assert_first_update_call_on_method_on_editor(actionEvent.getDataContext(), psiFile),
                actionEvent -> assert_second_update_call_on_method_on_editor(actionEvent.getDataContext(), psiFile, selectedMethod));

        actionEvent.performUpdateOnEditor(action, myFixture, psiFile);

        assertEquals(2, action.getCalls());
    }
    public void test_perform_update_should_not_call_slow_operation() {
        PsiFile psiFileA = myFixture.addFileToProject("folderA/fileA.txt", "content");

        final DocAsTestAction action = new DocAsTestAction() {

            @Override
            public void update(@NotNull AnActionEvent e) {
                // Call a slow operation
                getFilesByName(e.getProject(), "");
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
            }
        };

        try {
            actionEvent.performUpdate(action, Arrays.asList(psiFileA));
        } catch (RuntimeException e) {
            final StackTraceElement[] stackTrace = e.getStackTrace();
            assertEquals("run", stackTrace[0].getMethodName());
            assertTrue(stackTrace[0].getClassName().contains("DocAsTestPlatformTest"));
            return;
        }
        fail("An exception should be thrown");

    }

    public void test_perform_action() {

        PsiFile psiFileA = myFixture.addFileToProject("folderA/fileA.txt", "content");
        PsiFile psiFileB = myFixture.addFileToProject("folderB/fileB.txt", "content");
        PsiFile psiFileX = myFixture.addFileToProject("folderX/filex.txt", "content");
        PsiDirectory folderA = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderA"));
        PsiDirectory folderB = myFixture.getPsiManager().findDirectory(myFixture.findFileInTempDir("folderB"));

        final List<PsiFileSystemItem> psiItems = Arrays.asList(folderA, psiFileB, psiFileX);
        final CheckApproveFileActionActionPerform action = new CheckApproveFileActionActionPerform(
                actionEvent -> assert_second_update_call_mixte_files_folders(actionEvent.getDataContext(), psiItems));

        actionEvent.performAction(action, psiItems);

        assertEquals(2, action.getCalls());
    }

    public void test_perform_action_on_editor() {
        final String selectedClassName = "MyClass";
        final String selectedMethod = "SelectedMethod";

        final String code = new FileHelper.CodeGenerator()
                .withMethod("SelectedMethod")
                .generate(FileHelper.CaretOn.METHOD);
        final PsiFile psiFile = fileHelper.addTestClassFile(selectedClassName, code);

        final CheckApproveFileActionActionPerform action = new CheckApproveFileActionActionPerform(
                actionEvent -> assert_second_update_call_on_method_on_editor(actionEvent.getDataContext(), psiFile, selectedMethod));

        actionEvent.performActionOnEditor(action, myFixture, psiFile);

        assertEquals(2, action.getCalls());
    }

    private void assert_first_update_call_without_file(@NotNull DataContext dataContext) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call_without_file(@NotNull DataContext dataContext) {
        assert_first_update_call_without_file(dataContext);
    }

    private void assert_first_update_call(@NotNull DataContext dataContext) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiFile);
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]);
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call(@NotNull DataContext dataContext) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_FILE) instanceof PsiFile);
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiFile);
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE) instanceof PsiFile);
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]);
        assertEquals("file.txt", dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(1, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
    }

    private void assert_first_update_call_multi_files(@NotNull DataContext dataContext, int nbFiles) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(nbFiles, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < nbFiles; i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call_multi_files(@NotNull DataContext dataContext, int nbFiles) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_FILE) instanceof PsiFile);
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(nbFiles, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < nbFiles; i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertEquals("fileA.txt", dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(nbFiles, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
    }

    private void assert_first_update_call_one_folder(@NotNull DataContext dataContext, String folder) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiDirectory);
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]);
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call_one_folder(@NotNull DataContext dataContext, String folder) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiDirectory);
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE) instanceof PsiDirectory);
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]);
        assertEquals(folder, dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(1, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        assertNotNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[0]);
    }

    private void assert_first_update_call_multi_folders(@NotNull DataContext dataContext, String... folders) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(folders.length, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < folders.length; i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call_multi_folders(@NotNull DataContext dataContext, String... folders) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(folders.length, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < folders.length; i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertEquals(folders[0], dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(folders.length, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        for (int i = 0; i < folders.length; i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[i]);
        }
    }

    private void assert_first_update_call_mixte_files_folders(@NotNull DataContext dataContext, List<PsiFileSystemItem> files) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(files.size(), dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < files.size(); i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY));
    }

    private void assert_second_update_call_mixte_files_folders(@NotNull DataContext dataContext, List<PsiFileSystemItem> files) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertNull(dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(files.size(), dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < files.size(); i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertEquals(files.get(0).getName(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(files.size(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        for (int i = 0; i < files.size(); i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[i]);
        }
    }

    private void assert_second_update_call_mixte_files_folders_file_first(@NotNull DataContext dataContext, List<PsiFileSystemItem> files) {
        assertNull(dataContext.getData(CommonDataKeys.EDITOR));
        assertEquals(files.get(0), dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertEquals(files.size(), dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        for (int i = 0; i < files.size(); i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[i]);
        }
        assertEquals(files.get(0).getName(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE).getName());
        assertEquals(files.size(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        for (int i = 0; i < files.size(); i++) {
            assertNotNull(dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[i]);
        }
    }

    private void assert_first_update_call_outside_class_on_editor(@NotNull DataContext dataContext, PsiFile file) {
        assertEquals(file.getName(), ((EditorImpl)dataContext.getData(CommonDataKeys.EDITOR)).getVirtualFile().getName());
        assertEquals(file, dataContext.getData(CommonDataKeys.PSI_FILE));
        assertNull(dataContext.getData(CommonDataKeys.PSI_ELEMENT));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE));
        assertNull(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY));
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertEquals(1, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[0]);
    }

    private void assert_second_update_call_outside_class_on_editor(@NotNull DataContext dataContext, PsiFile file) {
        assert_first_update_call_outside_class_on_editor(dataContext, file);
    }

    private void assert_first_update_call_on_class_on_editor(@NotNull DataContext dataContext, PsiFile file) {
        assert_first_update_call_outside_class_on_editor(dataContext, file);
    }

    private void assert_second_update_call_on_class_on_editor(@NotNull DataContext dataContext, PsiFile file, String className) {
        assertEquals(file.getName(), ((EditorImpl)dataContext.getData(CommonDataKeys.EDITOR)).getVirtualFile().getName());
        assertEquals(file, dataContext.getData(CommonDataKeys.PSI_FILE));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiIdentifier);
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT).getParent() instanceof PsiClass);
        assertEquals(className, ((PsiClass)dataContext.getData(CommonDataKeys.PSI_ELEMENT).getParent()).getName());
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE) instanceof PsiIdentifier);
        assertTrue(((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE)).getParent() instanceof PsiClass);
        assertEquals(className, ((PsiClass)((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE)).getParent()).getName());
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0] instanceof PsiIdentifier);
        assertTrue(((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]).getParent() instanceof PsiClass);
        assertEquals(className, ((PsiClass)((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]).getParent()).getName());
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertEquals(1, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[0]);
    }

    private void assert_first_update_call_on_method_on_editor(@NotNull DataContext dataContext, PsiFile file) {
        assert_first_update_call_outside_class_on_editor(dataContext, file);
    }

    private void assert_second_update_call_on_method_on_editor(@NotNull DataContext dataContext, PsiFile file, String methodName) {
        assertEquals(file.getName(), ((EditorImpl)dataContext.getData(CommonDataKeys.EDITOR)).getVirtualFile().getName());
        assertEquals(file, dataContext.getData(CommonDataKeys.PSI_FILE));
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiIdentifier);
        assertTrue(dataContext.getData(CommonDataKeys.PSI_ELEMENT).getParent() instanceof PsiMethod);
        assertEquals(methodName, ((PsiMethod)dataContext.getData(CommonDataKeys.PSI_ELEMENT).getParent()).getName());
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE) instanceof PsiIdentifier);
        assertTrue(((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE)).getParent() instanceof PsiMethod);
        assertEquals(methodName, ((PsiMethod)((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE)).getParent()).getName());
        assertEquals(1, dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY).length);
        assertTrue(dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0] instanceof PsiIdentifier);
        assertTrue(((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]).getParent() instanceof PsiMethod);
        assertEquals(methodName, ((PsiMethod)((PsiIdentifier)dataContext.getData(CommonDataKeys.NAVIGATABLE_ARRAY)[0]).getParent()).getName());
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE));
        assertEquals(1, dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).length);
        assertEquals(file.getVirtualFile(), dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)[0]);
    }

    private class CheckApproveFileAction extends DocAsTestAction {
        private final AtomicInteger atomicInteger;
        private final Consumer<AnActionEvent> assertFirstCall;
        private final Consumer<AnActionEvent> assertSecondCall;

        public CheckApproveFileAction(Consumer<AnActionEvent> assertFirstCall, Consumer<AnActionEvent> assertSecondCall) {
            this.atomicInteger = new AtomicInteger();
            this.assertFirstCall = assertFirstCall;
            this.assertSecondCall = assertSecondCall;
        }

        @Override
        public void update(AnActionEvent actionEvent) {

            final int calls = atomicInteger.incrementAndGet();
            if (calls == 1) assertFirstCall.accept(actionEvent);
            else if (calls == 2) assertSecondCall.accept(actionEvent);
            else fail("Must not be called more than 2 times");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
        }

        public int getCalls() {
            return atomicInteger.get();
        }
    }

    private class CheckApproveFileActionActionPerform extends DocAsTestAction {
        private final AtomicInteger atomicInteger;
        private final Consumer<AnActionEvent> assertFirstCall;

        public CheckApproveFileActionActionPerform(Consumer<AnActionEvent> assertFirstCall) {
            this.atomicInteger = new AtomicInteger();
            this.assertFirstCall = assertFirstCall;
        }

        @Override
        public void update(AnActionEvent actionEvent) {
            final int calls = atomicInteger.incrementAndGet();
            if (calls == 1) assertFirstCall.accept(actionEvent);
            else fail("Must not be called more than 1 times");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent actionEvent) {
            final int calls = atomicInteger.incrementAndGet();
            if (calls == 1) fail("Must not be called the first one");
            else if (calls == 2) assertFirstCall.accept(actionEvent);
            else fail("Must not be called more than 1 times");
        }

        public int getCalls() {
            return atomicInteger.get();
        }
    }
}