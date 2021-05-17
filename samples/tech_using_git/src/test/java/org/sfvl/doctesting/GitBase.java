package org.sfvl.doctesting;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.sfvl.doctesting.junitinheritance.DocAsTestBase;
import org.sfvl.doctesting.utils.DocPath;
import org.sfvl.doctesting.utils.DocumentationNamer;
import org.sfvl.doctesting.utils.OnePath;

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

    @Override
    protected void approvalAfterTestSpecific(String content, DocPath docPath) throws Exception {
        writeContent(docPath, content);

        // When property 'no-assert' is present, we just generate documents without checking.
        if (System.getProperty("no-assert") == null) {
            assertNoModification(docPath);
        }
    }

    private void writeContent(DocPath docPath, String content) throws IOException {
        final OnePath filePath = docPath.approved();
        createDirIfNotExists(filePath.folder());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.path().toFile().toString()))) {
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

    private void assertNoModification(DocPath docPath) throws Exception {
        final Path gitRoot = pathBuidler.getGitRootPath();

        final Path approvedPath = docPath.approved().path();
        final Path absoluteApprovedPath = pathBuidler.getProjectPath().resolve(approvedPath);
        final Path subPath = gitRoot.relativize(absoluteApprovedPath);
        if (isModify(gitRoot, subPath.toString())) {
            fail("File was modified:" + docPath.approved().path().toString());
        } else {
            System.out.println("Success:" + docPath.approved().path().toString());

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
