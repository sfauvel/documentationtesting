package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Base class for test.
 *
 * It checks that everything written during test is identical to the approved content.
 */
public class ApprovalsBase {

    private static final PathProvider pathBuidler = new PathProvider();
    private StringBuffer sb = new StringBuffer();

    /**
     * Return git root path.
     * This method is protected to allow a project to create a subclass and redefine git root path.
     * @return
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

    /**
     * Give path where docs are generated.
     * @return
     */
    protected Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Paths.get( "src", "test", "docs"));
    }

    /**
     * Write a text to the output.
     */
    protected void write(String... texts) {
        sb.append(Arrays.stream(texts).collect(Collectors.joining("\n")));
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) throws IOException, GitAPIException {

        final Path docRootPath = getDocPath();
        String content = buildContent(testInfo);

        if ("approvals".equals(System.getProperty("approved_with"))) {
            approvedAfterTestWithApproval(content, new DocumentationNamer(docRootPath, testInfo));
        } else {
            approvalAfterTestWithGit(content, new DocumentationNamer(docRootPath, testInfo));
        }
    }

    private void approvalAfterTestWithGit(final String content, final DocumentationNamer documentationNamer) throws IOException, GitAPIException {

        writeContent(documentationNamer, content);

        // When property noassert is present, we just generate documents without checking.
        if (System.getProperty("noassert") == null) {
            assertNoModification(documentationNamer);
        }
    }

    private void approvedAfterTestWithApproval(final String content, final DocumentationNamer documentationNamer) {
        ApprovalNamer approvalNamer = new ApprovalNamer() {

            @Override
            public String getApprovalName() {
                return documentationNamer.getApprovalName();
            }

            @Override
            public String getSourceFilePath() {
                return documentationNamer.getSourceFilePath();
            }
        };

        Approvals.verify(
                new ApprovalTextWriter(content, "adoc"),
                approvalNamer,
                Approvals.getReporter());
    }

    private void writeContent(DocumentationNamer documentationNamer, String content) throws IOException {
        final Path filePath = documentationNamer.getFilePath();
        createDirIfNotExists(Paths.get(documentationNamer.getSourceFilePath()));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile().toString()))) {
            writer.write(content);
        }
    }

    private String buildContent(TestInfo testInfo) {
        return String.join("\n\n",
                "= " + formatTitle(testInfo),
                getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                sb.toString());
    }

    public void createDirIfNotExists(Path path) {
        if (!path.toFile().exists()) {
            if (!path.toFile().mkdirs()) {
                throw new RuntimeException("Error creating path: " + path.toString());
            }
        }
    }

    private void assertNoModification(DocumentationNamer documentationNamer) throws IOException, GitAPIException {
        final Path gitRoot = getGitRootPath();

        final Path subPath = gitRoot.relativize(documentationNamer.getFilePath());
        if (isModify(gitRoot, subPath.toString())) {
            fail("File was modified:" + documentationNamer.getApprovalName()+".adoc");
        } else {
            System.out.println("Success:" + documentationNamer.getApprovalName()+".adoc");

        }
    }

    public boolean isModify(Path root, String path) throws IOException, GitAPIException {
        System.out.println(path.toString());
        File rootGit = root
                .resolve(".git")
                .toFile();

        Repository repository = new FileRepositoryBuilder()
                .setGitDir(rootGit)
                .setMustExist(true)
                .build();

        try (Git git = new Git(repository)) {
            Status status = git.status().addPath(path).call();
            return !status.isClean();
        }
    }

    /**
     * Return name specified in DisplayName annotation.
     * If annotation is not present, this is the method name that will be returned
     * after some test formatting (remove '_', uppercase first letter).
     * @param testInfo
     * @return
     */
    private String formatTitle(TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();
        String methodName = testInfo.getTestMethod().get().getName();
        if (displayName.equals(methodName+"()")) {
            String title = methodName.replace("_", " ");
            return title.substring(0, 1).toUpperCase() + title.substring(1);
        } else {
            return displayName;
        }
    }

    private String getComment(Class<?> clazz, String methodName) {
        JavaProjectBuilder builder = new JavaProjectBuilder();
        builder.addSourceTree(new File("src/test/java"));

        JavaClass javaClass = builder.getClassByName(clazz.getCanonicalName());

        JavaMethod method = javaClass.getMethod(methodName, Collections.emptyList(), false);
        return Optional.ofNullable(method.getComment()).orElse("");
    }

}
