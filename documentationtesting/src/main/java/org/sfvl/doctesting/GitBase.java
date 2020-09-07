package org.sfvl.doctesting;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class GitBase extends DocAsTestBase {

    /**
     * Return git root path.
     * This method is protected to allow a project to create a subclass and redefine git root path.
     *
     */
    protected Path getGitRootPath() {

        final Path originalPath = pathBuidler.getProjectPath();
        Path path = originalPath;
        while (!path.resolve(".git").toFile().exists()) {
            path = path.getParent();
            if (path == null) {
                throw new RuntimeException("No git repository found from parents of " + originalPath.toString());
            }
        }
        return path;
    }

    @Override
    protected void approvalAfterTestSpecific(final String content, final DocumentationNamer documentationNamer) throws Exception {

        writeContent(documentationNamer, content);

        // When property 'no-assert' is present, we just generate documents without checking.
        if (System.getProperty("no-assert") == null) {
            assertNoModification(documentationNamer);
        }
    }

    private void writeContent(DocumentationNamer documentationNamer, String content) throws IOException {
        final Path filePath = documentationNamer.getFilePath();
        createDirIfNotExists(Paths.get(documentationNamer.getSourceFilePath()));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile().toString()))) {
            writer.write(content);
        }
    }

    public void createDirIfNotExists(Path path) {
        if (!path.toFile().exists()) {
            if (!path.toFile().mkdirs()) {
                throw new RuntimeException("Error creating path: " + path.toString());
            }
        }
    }

    private void assertNoModification(DocumentationNamer documentationNamer) throws Exception {
        final Path gitRoot = getGitRootPath();

        final Path subPath = gitRoot.relativize(documentationNamer.getFilePath());
        if (isModify(gitRoot, subPath.toString())) {
            fail("File was modified:" + documentationNamer.getApprovalName() + ".adoc");
        } else {
            System.out.println("Success:" + documentationNamer.getApprovalName() + ".adoc");

        }
    }

    public boolean isModify(Path root, String path) throws IOException, GitAPIException {
        File rootGit = root
                .resolve(".git")
                .toFile();

        Repository repository = new FileRepositoryBuilder()
                .setGitDir(rootGit)
                .setMustExist(true)
                .build();

        try (Git git = new Git(repository)) {
            Status status = git.status().addPath(path).call();
            return !(status.getModified().isEmpty()
                    && status.getUntracked().isEmpty()
                    && status.getMissing().isEmpty());
        }
    }

}
