package org.sfvl.doctesting.junitextension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class ApprovalsExtensionTest {

    private final DocWriter docWriter = new DocWriter();
    @RegisterExtension
    ApprovalsExtension extension = new ApprovalsExtension(docWriter);

    private void write(String... texts) {
        docWriter.write(texts);
    }

    public static class MyTest {
        private final DocWriter docWriter = new DocWriter();
        @RegisterExtension
        ApprovalsExtension extension = new ApprovalsExtension(docWriter);

        @Test
        public void testA() {
            docWriter.write("In my test");
        }
    }

    SummaryGeneratingListener listener = new SummaryGeneratingListener();

    @Test
    public void using_extension() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(MyTest.class))
                .build();
        Launcher launcher = LauncherFactory.create();
//        TestPlan testPlan = launcher.discover(request);
//        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        docWriter.write("include::" + MyTest.class.getSimpleName() + ".testA.approved.adoc[]");

        docWriter.write();
    }

}