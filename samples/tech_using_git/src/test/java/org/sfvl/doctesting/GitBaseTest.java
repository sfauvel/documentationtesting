package org.sfvl.doctesting;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GitBaseTest {

    private static final String subfolder = "docs";
    final GitBase gitBase = new GitBase();
    private Path gitPath;
    private Repository repository;

    @BeforeEach
    private void initRepo(@TempDir Path tempDir) throws IOException, GitAPIException, IllegalAccessException, InvocationTargetException {
        this.gitPath = tempDir;

        repository = FileRepositoryBuilder.create(this.gitPath.resolve(".git").toFile());
        repository.create();

        Files.createDirectory(this.gitPath.resolve(subfolder));

        createFile(this.gitPath.resolve(Paths.get(subfolder).resolve("dummy.txt")), "Hello");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(Paths.get(subfolder).resolve("dummy.txt").toString()).call();
            git.commit().setMessage("Add dummy file").call();
        }
    }


    @Test
    public void should_not_modified_when_no_file(@TempDir Path tempDir) throws Exception {
        assertEquals(false, gitBase.isModify(tempDir, subfolder));
    }

    @Test
    public void should_modified_when_new_file_not_already_staged() throws Exception {
        createFileInRepo(Paths.get(subfolder, "filename.txt"), "Hello");
        assertEquals(true, gitBase.isModify(gitPath, subfolder));
    }

    @Test
    public void should_not_modified_when_new_file_staged() throws Exception {
        final Path filePath = Paths.get(subfolder).resolve("filename.txt");
        createFileInRepo(filePath, "Hello");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(filePath.toString()).call();
        }
        assertEquals(false, gitBase.isModify(gitPath, subfolder));
    }

    @Test
    public void should_modified_when_file_staged_and_then_modified() throws Exception {
        final Path filePath = Paths.get(subfolder).resolve("filename.txt");
        createFileInRepo(filePath, "Hello");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(filePath.toString()).call();
        }
        createFileInRepo(filePath, " world!");

        assertEquals(true, gitBase.isModify(gitPath, subfolder));
    }

    @Test
    public void should_modified_when_file_removed_from_disk() throws Exception {
        final Path filePath = Paths.get(subfolder).resolve("filename.txt");
        createFileInRepo(filePath, "Hello");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(filePath.toString()).call();
            git.commit().setMessage("Add file").call();
        }
        Files.delete(gitPath.resolve(filePath));

        assertEquals(true, gitBase.isModify(gitPath, subfolder));
    }

    @Test
    public void should_not_modified_when_file_removed_from_git_and_from_disk() throws Exception {
        final Path filePath = Paths.get(subfolder).resolve("filename.txt");
        createFileInRepo(filePath, "Hello");
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(filePath.toString()).call();
            git.commit().setMessage("Add file").call();
            git.rm().addFilepattern(filePath.toString()).call();
        }
        assertFalse(Files.exists(filePath));

        assertEquals(false, gitBase.isModify(gitPath, subfolder));
    }

    @Test
    public void should_modified_when_file_removed_from_git() throws Exception {
        final Path filePath = Paths.get(subfolder).resolve("filename.txt");
        createFileInRepo(filePath, "Hello");

        try (Git git = new Git(repository)) {
            git.add().addFilepattern(filePath.toString()).call();
            git.commit().setMessage("Add file").call();
            git.rm().addFilepattern(filePath.toString()).call();
        }
        assertFalse(Files.exists(filePath));
        createFileInRepo(filePath, "Hello");

        assertEquals(true, gitBase.isModify(gitPath, subfolder));
    }

    private void createFileInRepo(Path resolve, String content) throws IOException {
        createFile(this.gitPath.resolve(resolve), content);

    }

    private void createFile(Path resolve, String content) throws IOException {
        try (FileWriter myWriter = new FileWriter(resolve.toFile())) {
            myWriter.write(content);
        }
    }

    private void showGetterValues(Repository repository) throws GitAPIException, IllegalAccessException, InvocationTargetException {
        System.out.println("=======================");
        try (Git git = new Git(repository)) {
            Status status = git.status().call();
            final List<Method> getters = Arrays.stream(Status.class.getMethods())
                    .filter(m -> m.getName().startsWith("get"))
                    .collect(Collectors.toList());

            for (Method getter : getters) {
                System.out.println(getter.getName() + ": " + getter.invoke(status));
            }

        }
    }

}