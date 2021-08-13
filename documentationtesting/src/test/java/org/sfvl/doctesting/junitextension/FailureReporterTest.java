package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.DocWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@DisplayName("Failure report")
public class FailureReporterTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @TempDir
    Path tempDir;

    private final Formatter formatter = new AsciidocFormatter();

    /**
     * When the received file is not the same as the approved file,
     * first different line is displayed on report.
     */
    @Test
    public void report_when_files_are_differents() throws IOException {
        final String approved = String.join("\n",
                "= Title",
                "Description",
                "Line from approved text",
                "Footer approved");

        final String received = String.join("\n",
                "= Title",
                "Description",
                "Line from received text",
                "Footer received");

        documentReportBetween(approved, received);
    }

    /**
     * When the received file is identical to the approved file,
     * it's indicated it.
     * But this report is normally not displayed when files are identical.
     */
    @Test
    public void report_when_files_are_identicals() throws IOException {
        final String approved = String.join("\n",
                "= Title",
                "Description");

        final String received = String.join("\n",
                "= Title",
                "Description");

        documentReportBetween(approved, received);
    }

    /**
     * When the received file is shorter than the approved file,
     * We indicate that there is no more lines.
     */
    @Test
    public void received_file_shorter_than_approved_file() throws IOException {

        final String approved = String.join("\n",
                "= Title",
                "Description",
                "Footer");

        final String received = String.join("\n",
                "= Title",
                "Description");

        documentReportBetween(approved, received);
    }

    /**
     * When the received file is longer than the approved file,
     * We indicate that there is no more lines.
     */
    @Test
    public void received_file_longer_than_approved_file() throws IOException {

        final String approved = String.join("\n",
                "= Title",
                "Description");

        final String received = String.join("\n",
                "= Title",
                "Description",
                "Footer");

        documentReportBetween(approved, received);
    }

    private void documentReportBetween(String approved, String received) throws IOException {
        final Path files = Files.createDirectory(tempDir.resolve("files"));

        final String approvedFile = files.resolve("approved.adoc").toFile().toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(approvedFile))) {
            writer.write(approved);
        }
        final String receivedFile = files.resolve("received.adoc").toFile().toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receivedFile))) {
            writer.write(received);
        }

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final ApprovalsExtension.FailureReporter reporter = new ApprovalsExtension.FailureReporter(new PrintStream(os));
        final PrintStream out = System.out;
        // We redirect System.out to nothing to avoid a output to console.
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            reporter.report(receivedFile, approvedFile);
        } finally {
            System.setOut(out);
        }

        doc.write(formatter.blockBuilder(Formatter.Block.LITERAL)
                        .title("Received text")
                        .content(received)
                        .build(),
                "");

        doc.write(formatter.blockBuilder(Formatter.Block.LITERAL)
                        .title("Approved text")
                        .content(approved)
                        .build(),
                "");

        String report = os.toString("UTF8");

        doc.write(formatter.blockBuilder(Formatter.Block.LITERAL)
                .title("Report")
                .content(report.replace(tempDir.resolve("files") + File.separator, "[TEMPORARY FOLDER]/files/").trim())
                .build());
    }

    @AfterEach
    public void clean() {
        deleteDirectory(tempDir.toFile());
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        System.out.println(Arrays.toString(allContents));
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
