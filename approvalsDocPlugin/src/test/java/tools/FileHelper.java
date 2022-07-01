package tools;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        final String code = CodeGenerator.generateCode(className, caretOn);
        return addTestClassFile(className, code);
    }

    @NotNull
    public PsiFile addTestClassFile(String fileName, String code) {
        return myFixture.configureByText(fileName + ".java", code);
    }

    public PsiFile addTestClassFile(final Path packagePath, final String className, CaretOn caretOn) {
        final String code = CodeGenerator.generateCode(packagePath, className, caretOn);
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

    public VirtualFile createFile(String path) {
        return myFixture.getTempDirFixture().createFile(path);
    }

    public VirtualFile findOrCreate(PsiFile testFile, Path path) {
        return findOrCreate(testFile.getVirtualFile(), path);
    }

    public VirtualFile findOrCreate(String pathToCreate) throws IOException {
        return findOrCreate(Paths.get(pathToCreate));
    }

    public @NotNull VirtualFile main_source_path() throws IOException {
        return myFixture.getTempDirFixture().findOrCreateDir(".");
    }

    public VirtualFile findOrCreate(Path pathToCreate) throws IOException {
        final VirtualFile srcVirtualFile = main_source_path();
        final Path srcPathFromRoot = Paths.get("/")
                .relativize(Paths.get(srcVirtualFile.getPath()));

        final Path pathToCreateRelativeToSrc = srcPathFromRoot.relativize(pathToCreate);

        return findOrCreate(srcVirtualFile, pathToCreateRelativeToSrc);
    }

    public VirtualFile findOrCreate(final VirtualFile rootVirtualFile, Path path) {
        WriteAction.computeAndWait(() -> {
            try {
                VirtualFile currentVirtualFile = rootVirtualFile;
                for (Path folder : path) {
                    final VirtualFile existingVirtualFile = currentVirtualFile.findFileByRelativePath(folder.toString());
                    currentVirtualFile = existingVirtualFile != null
                            ? existingVirtualFile
                            : currentVirtualFile.createChildDirectory(this, folder.toString());
                    //System.out.println("SetupWithDescriptorFactoryTest.findOrCreate " + currentVirtualFile);
                }
                return currentVirtualFile;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return rootVirtualFile.findFileByRelativePath(path.toString());
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

//        for (String filepath : filepaths) {
//            final Path path = Paths.get(filepath);
//            final String fileName = path.getFileName().toString();
//            final String fileFolder = path.getParent().toString();
//
//
//            final PsiFile psiFile = myFixture.configureByText(fileName, path.toString() + " content");
////            try {
////                myFixture.moveFile(file.getName(), "..");
////                final VirtualFile file = myFixture.getTempDirFixture().createFile(fileName, path.toString() + " content");
////                final VirtualFile javaDir = myFixture.getTempDirFixture().findOrCreateDir(fileFolder);
////                myFixture.moveFile(file.getName(), fileFolder);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//        }

        System.out.println("FileHelper.initFiles:\n"
                + files.values().stream()
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
