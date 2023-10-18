package com.adaptionsoft.games.trivia;

import com.adaptionsoft.games.uglytrivia.Game;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.junitextension.FailureReporter;
import org.sfvl.doctesting.utils.DocPath;

import java.io.*;
import java.util.Random;


public class ClassivalApprovalsGameTest {

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    @Test
    public void classical_approvals_test(TestInfo testInfo) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        final Game aGame = new Game();
        aGame.add("Chet");
        aGame.add("Pat");
        aGame.add("Sue");

        Random rand = new Random(12345);

        boolean notAWinner;
        do {

            aGame.roll(rand.nextInt(5) + 1);

            if (rand.nextInt(9) == 7) {
                notAWinner = aGame.wrongAnswer();
            } else {
                notAWinner = aGame.wasCorrectlyAnswered();
            }

        } while (notAWinner);

        final Options options = new Options()
                .forFile().withExtension(".adoc")
                .withReporter(new FailureReporter());

        Approvals.verify(
                new ApprovalTextWriter(outputStream.toString(), options),
                getApprovalNamer(testInfo),
                options);

    }

    private ApprovalNamer getApprovalNamer(TestInfo testInfo) {
        final DocPath docPath = new DocPath(testInfo.getTestMethod().get());
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            @Override
            public String getApprovalName() {
                return docPath.name();
            }

            @Override
            public String getSourceFilePath() {
                return docPath.approved().folder().toString() + File.separator;
            }

            @Override
            public File getApprovedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".approved" + extensionWithDot);
            }

            @Override
            public File getReceivedFile(String extensionWithDot) {
                return new File(this.getSourceFilePath() + "/" + this.getApprovalName() + ".received" + extensionWithDot);
            }

            @Override
            public ApprovalNamer addAdditionalInformation(String s) {
                return null;
            }
        };
        return approvalNamer;
    }
}
