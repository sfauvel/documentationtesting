package com.adaptionsoft.games.trivia;

import com.adaptionsoft.games.uglytrivia.Game;
import org.approvaltests.Approvals;
import org.approvaltests.namer.ApprovalNamer;
import org.approvaltests.writers.ApprovalTextWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.sfvl.doctesting.ApprovalsBase;
import org.sfvl.doctesting.DocumentationNamer;
import org.sfvl.doctesting.PathProvider;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertPathBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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


        Approvals.verify(new ApprovalTextWriter(outputStream.toString(), "adoc"),
                getApprovalNamer(testInfo),
                Approvals.getReporter());

    }

    private ApprovalNamer getApprovalNamer(TestInfo testInfo) {
        final PathProvider pathProvider = new PathProvider();
        final Path docRootPath = pathProvider.getProjectPath().resolve(Paths.get("src", "test", "docs"));

        final DocumentationNamer documentationNamer = new DocumentationNamer(docRootPath, testInfo);
        ApprovalNamer approvalNamer = new ApprovalNamer() {
            public String getApprovalName() {
                return documentationNamer.getApprovalName();
            }

            public String getSourceFilePath() {
                return documentationNamer.getSourceFilePath();
            }
        };
        return approvalNamer;
    }
}
