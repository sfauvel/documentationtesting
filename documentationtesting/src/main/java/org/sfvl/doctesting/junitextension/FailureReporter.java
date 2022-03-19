package org.sfvl.doctesting.junitextension;

import org.approvaltests.Approvals;
import org.approvaltests.core.ApprovalFailureReporter;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FailureReporter implements ApprovalFailureReporter {
    ApprovalFailureReporter delegate = Approvals.getReporter();
    private final PrintStream out;

    public FailureReporter() {
        this(System.out);
    }

    public FailureReporter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void report(String received, String approved) {
        delegate.report(received, approved);
        if (Files.notExists(Paths.get(approved))) {
            out.println("No approved file yet");
            return;
        }
        try {
            final List<String> approvedLines = readFileLines(approved);
            final List<String> receivedLines = readFileLines(received);

            int lineNumber = 0;
            while (lineNumber < approvedLines.size()
                    && lineNumber < receivedLines.size()
                    && approvedLines.get(lineNumber).equals(receivedLines.get(lineNumber))) {
                lineNumber++;
            }

            if (lineNumber >= approvedLines.size() && lineNumber >= receivedLines.size()) {
                out.println(String.format("Files are identical:\n    Approved: %s\n    Received: %s",
                        approved,
                        received));
            } else {
                out.println(String.format("Differences between files:\n    Approved: %s\n    Received: %s",
                        approved,
                        received));
                out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                out.println(formatLine(approvedLines, lineNumber));
                out.println("=================================================================");
                out.println(formatLine(receivedLines, lineNumber));
                out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> readFileLines(String filename) throws IOException {
        final List<String> lines = new ArrayList<>();
        try (Stream<String> linesStream = Files.lines(Paths.get(filename))) {
            lines.addAll(linesStream.collect(Collectors.toList()));
        }
        return lines;
    }

    private String formatLine(List<String> lines, int lineNumber) {
        if (lineNumber < lines.size()) {
            return String.format("%d: %s", (lineNumber + 1), lines.get(lineNumber));
        } else {
            return String.format("%d (no line)", (lineNumber + 1));
        }
    }
}
