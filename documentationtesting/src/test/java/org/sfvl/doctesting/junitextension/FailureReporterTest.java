package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.test_tools.FastApprovalsExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@DisplayName("Failure report")
public class FailureReporterTest {
    @RegisterExtension
    static ApprovalsExtension doc = new FastApprovalsExtension();

    @TempDir
    Path tempDir;

    private final Formatter formatter = new AsciidocFormatter();

    @Test
    public void report_when_files_are_differents() throws IOException {
        doc.write(
                "When the received file is not the same as the approved file,",
                "first different line is displayed on report.",
                "",""
        );

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

    @Test
    public void report_when_files_are_identicals() throws IOException {
        doc.write(
                "When the received file is identical to the approved file,",
                "it's indicated it.",
                "But this report is normally not displayed when files are identical.",
                "",""
        );
        final String approved = String.join("\n",
                "= Title",
                "Description");

        final String received = String.join("\n",
                "= Title",
                "Description");

        documentReportBetween(approved, received);
    }

    @Test
    public void received_file_shorter_than_approved_file() throws IOException {
        doc.write(
                "When the received file is shorter than the approved file,",
                "We indicate that there is no more lines.",
                "",""
        );

        final String approved = String.join("\n",
                "= Title",
                "Description",
                "Footer");

        final String received = String.join("\n",
                "= Title",
                "Description");

        documentReportBetween(approved, received);
    }

    @Test
    public void received_file_longer_than_approved_file() throws IOException {
        doc.write(
                "When the received file is longer than the approved file,",
                "We indicate that there is no more lines.",
                "",""
        );

        final String approved = String.join("\n",
                "= Title",
                "Description");

        final String received = String.join("\n",
                "= Title",
                "Description",
                "Footer");

        documentReportBetween(approved, received);
    }

    @Test
    public void approved_file_does_not_exist() throws IOException {
        doc.write("We indicate a specific message when approved file does not exist.", "", "");
        final String received = String.join("\n",
                "= Title",
                "Description",
                "Footer");

        documentReportBetween(null, received);
    }

    private void documentReportBetween(String approved, String received) throws IOException {
        final Path files = Files.createDirectory(tempDir.resolve("files"));

        final String approvedFile = files.resolve("approved.adoc").toFile().toString();
        if (approved != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(approvedFile))) {
                writer.write(approved);
            }
        }
        final String receivedFile = files.resolve("received.adoc").toFile().toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receivedFile))) {
            writer.write(received);
        }

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final FailureReporter reporter = new FailureReporter(new PrintStream(os));
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

        if (approved != null) {
            doc.write(formatter.blockBuilder(Formatter.Block.LITERAL)
                            .title("Approved text")
                            .content(approved)
                            .build(),
                    "");
        }

        String report = os.toString("UTF8");

        doc.write(formatter.blockBuilder(Formatter.Block.LITERAL)
                .title("Report")
                .content(report.replace(tempDir.resolve("files") + File.separator, "[TEMPORARY FOLDER]/files/").trim())
                .build());
    }

}
