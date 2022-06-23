package tools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FileHelper {

    private CodeInsightTestFixture myFixture;

    public FileHelper(CodeInsightTestFixture myFixture) {
        this.myFixture = myFixture;
    }

    public static enum CaretOn {
        NONE,
        IMPORT,
        CLASS,
        METHOD,
        INNER_CLASS,
        INNER_METHOD;
    }

    public PsiFile addTestClassFile(final String className, CaretOn caretOn) {
        final String code = generateCode(className, caretOn);
        return addTestClassFile(className, code);
    }

    @NotNull
    public PsiFile addTestClassFile(String fileName, String code) {
        return myFixture.configureByText(fileName + ".java", code);
    }

    public PsiFile addTestClassFile(final Path packagePath, final String className, CaretOn caretOn) {
        final String code = generateCode(packagePath, className, caretOn);
        return addTestClassFile(packagePath, className, code);
    }

    @NotNull
    public PsiFile addTestClassFile(final Path packagePath, String fileName, String code) {
        final PsiFile psiFile = myFixture.configureByText(fileName + ".java", code);
        try {
            myFixture.getTempDirFixture().findOrCreateDir(packagePath.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        myFixture.moveFile(psiFile.getName(), packagePath.toString());
        return psiFile;
    }

    public String generateCode(Path packagePath, String className, CaretOn caretOn) {
        return new CodeGenerator()
                .withPackage(packagePath.toString().replace(java.io.File.separatorChar, '.'))
                .withClass(className)
                .generate(caretOn);
    }

    public String generateCode(Path packagePath, CaretOn caretOn) {
        return String.format("package %s;\n%s",
                packagePath.toString().replace(java.io.File.separatorChar, '.'),
                generateCode(caretOn));
    }

    public String generateCode(String className, CaretOn caretOn) {
        return new CodeGenerator()
                        .withClass(className)
                        .generate(caretOn);
    }


    public static class CodeGenerator {
        private String className = "MyClass";
        private String method = "myMethod";
        private String fullPackage = null;

        String generate(CaretOn caretOn) {
            return String.format("%simport %sorg.demo; class %s%s { public void %s%s() {} class %sInnerClass{ public void %sinnerMethod() {} } }",
//                    CaretOn.NONE.equals(caretOn) ? "<caret>" : "",
                    this.fullPackage != null ? "package " + fullPackage + "; " : "",
                    CaretOn.IMPORT.equals(caretOn) ? "<caret>" : "",
                    CaretOn.CLASS.equals(caretOn) ? "<caret>" : "",
                    this.className,
                    CaretOn.METHOD.equals(caretOn) ? "<caret>" : "",
                    this.method,
                    CaretOn.INNER_CLASS.equals(caretOn) ? "<caret>" : "",
                    CaretOn.INNER_METHOD.equals(caretOn) ? "<caret>" : "");
        }

        public CodeGenerator withClass(String className) {
            this.className = className;
            return this;
        }
        public CodeGenerator withMethod(String method) {
            this.method = method;
            return this;
        }
        public CodeGenerator withPackage(String fullPackage) {
            this.fullPackage = fullPackage;
            return this;
        }
    }
    public String generateCode(CaretOn caretOn) {
        return String.format("import %sorg.demo; class %sMyClass { public void %smyMethod() {} class %sInnerClass{ public void %sinnerMethod() {} } }",
                CaretOn.IMPORT.equals(caretOn) ? "<caret>" : "",
                CaretOn.CLASS.equals(caretOn) ? "<caret>" : "",
                CaretOn.METHOD.equals(caretOn) ? "<caret>" : "",
                CaretOn.INNER_CLASS.equals(caretOn) ? "<caret>" : "",
                CaretOn.INNER_METHOD.equals(caretOn) ? "<caret>" : "");
    }

    public VirtualFile createFile(String path) {
        return myFixture.getTempDirFixture().createFile(path);
    }

    public Map<String, PsiDirectory> initFolders(String... filepaths) {
        Map<String, PsiDirectory> files = new HashMap<>();
        for (String filepath : filepaths) {
            // Force to add a mandatory file to the folder
            myFixture.addFileToProject(filepath + "/dummy.txt", "dummy file for " + filepath);

            final VirtualFile fileInTempDir = myFixture.findFileInTempDir(filepath);
            final PsiDirectory directory = myFixture.getPsiManager().findDirectory(fileInTempDir);
            files.put(filepath, directory);
        }
        System.out.println("FileHelper.initFolders:\n"
                + files.values().stream()
                .map(file -> "  - " + file.getName())
                .collect(Collectors.joining("\n")));
        return files;
    }

    public Map<String, PsiFile> initFiles(String... filepaths) {
        final Project project = myFixture.getProject();
        final List<String> allFilenamesInIndexBeforeInit = Arrays.asList(FilenameIndex.getAllFilenames(project));
        final List<VirtualFile> allVirtualFileInIndexBeforeInit = allFilenamesInIndexBeforeInit.stream()
                .flatMap(name -> FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.allScope(project)).stream())
                .collect(Collectors.toList());


        Map<String, PsiFile> files = new HashMap<>();
        for (String filepath : filepaths) {
            files.put(filepath, myFixture.addFileToProject(filepath, getFileContent(filepath)));
        }

        System.out.println("FileHelper.initFiles:\n"
                +files.values().stream()
                .map(file -> "  - " + file.getName())
                .collect(Collectors.joining("\n")));


        final List<String> allFilenamesInIndexAfterInit = Arrays.asList(FilenameIndex.getAllFilenames(project));
        final List<VirtualFile> allVirtualFileInIndexAfterInit = allFilenamesInIndexAfterInit.stream()
                .flatMap(name -> FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.allScope(project)).stream())
                .collect(Collectors.toList());

        final List<String> allFilenamesAddedInIndexAfterInit = new ArrayList<String>(allFilenamesInIndexAfterInit);
        assert allFilenamesAddedInIndexAfterInit.removeAll(allFilenamesInIndexBeforeInit);

        System.out.println("FileHelper.initFiles files name added in Index: ");
        allFilenamesAddedInIndexAfterInit.stream()
                .map(file -> "  - " + file)
                .forEach(System.out::println);


        final List<VirtualFile> allVirtualFileAddedInIndexAfterInit = new ArrayList<VirtualFile>(allVirtualFileInIndexAfterInit);
        assert allVirtualFileAddedInIndexAfterInit.removeAll(allVirtualFileInIndexBeforeInit);

        System.out.println("FileHelper.initFiles virtual files added in Index: ");
        allVirtualFileAddedInIndexAfterInit.stream()
                .map(file -> "  - " + file.getPath())
                .forEach(System.out::println);

        return files;
    }

    public String getFileContent(String filepath) {
        return "some text for " + filepath;
    }

    public Map<String, PsiFile> initAllFiles(FieldAutoNaming fieldAutoNaming) {
        return initFiles(fieldAutoNaming.getAllFileNames().toArray(String[]::new));
    }
}
