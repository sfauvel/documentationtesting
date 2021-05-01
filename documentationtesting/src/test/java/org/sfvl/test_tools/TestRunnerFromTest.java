package org.sfvl.doctesting.test_tools;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.PrintStream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunnerFromTest {
    public void runTestClass(Class<?> testClass) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
//        TestPlan testPlan = launcher.discover(request);
//        launcher.registerTestExecutionListeners(listener);
        OnlyRunProgrammaticallyCondition.enable();

        final PrintStream out = System.out;
        try {
            System.setOut(new InterceptorStream(out));
            launcher.execute(request);
        } finally {
            System.setOut(out);
        }

        OnlyRunProgrammaticallyCondition.disable();
    }
}
