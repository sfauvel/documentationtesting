package org.sfvl.doctesting;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.IndexDiff;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
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
        return Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .getParent().getParent().getParent();
    }

    private Path getDocPath() {
        return Paths.get(this.getClass().getClassLoader().getResource("").getPath())
                .getParent().getParent()
                .resolve(Paths.get("src", "test", "docs"));
    }

    /**
     * Write a text to the output.
     * @param text
     */
    protected void write(String... texts) {
        sb.append(Arrays.stream(texts).collect(Collectors.joining("\n")));
    }

    @AfterEach
    public void approvedAfterTest(TestInfo testInfo) throws IOException, GitAPIException {

        final Path docRootPathGit = getDocPath();
        final DocumentationNamer documentationNamer = new DocumentationNamer(docRootPathGit, testInfo);

        writeContent(testInfo, documentationNamer);

        assertNoModification(documentationNamer);
    }

    private void writeContent(TestInfo testInfo, DocumentationNamer documentationNamer) throws IOException {
        final Path filePath = documentationNamer.getFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile().toString()))) {
            final String content = String.join("\n\n",
                    "= " + formatTitle(testInfo),
                    getComment(testInfo.getTestClass().get(), testInfo.getTestMethod().get().getName()),
                    sb.toString());

            writer.write(content);
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
     * Give path where docs are generated.
     * @return
     */
    protected Path getDocPath() {
        return pathBuidler.getProjectPath().resolve(Paths.get( "src", "test", "docs"));
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
