package org.sfvl.test_tools;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunnerFromTest {
    public static class Results {
        Map<TestIdentifier, TestExecutionResult> results = new HashMap<>();

        public void put(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            results.put(testIdentifier, testExecutionResult);
        }

        public boolean sucess() {
            return !results.values().stream().map(TestExecutionResult::getStatus).collect(Collectors.toSet()).contains(TestExecutionResult.Status.FAILED);
        }
    }

    public Results runTestClass(Class<?> testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
//        TestPlan testPlan = launcher.discover(request);

        Results results = new Results();
        TestExecutionListener listener = new TestExecutionListener() {
            @Override
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                results.put(testIdentifier, testExecutionResult);

                TestExecutionListener.super.executionFinished(testIdentifier, testExecutionResult);
            }
        };
        launcher.registerTestExecutionListeners(listener);
        OnlyRunProgrammaticallyCondition.enable();

        final PrintStream out = System.out;
        try {
            System.setOut(new InterceptorStream(out));
            launcher.execute(request);
        } finally {
            System.setOut(out);
        }
        OnlyRunProgrammaticallyCondition.disable();
        return results;
    }
}
