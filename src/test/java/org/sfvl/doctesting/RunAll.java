package org.sfvl.doctesting;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class RunAll {

    public static void main(String... args) throws IOException, InterruptedException {
        final RunAll runAll = new RunAll();
        runAll.runAllTest()
                .generateFullDoc()
//        .procesExec("/bin/bash convertAdoc.sh");
                .startProcessBuilder("/bin/bash convertAdoc.sh");
    }

    public RunAll runAllTest() {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("org.sfvl"))
                .filters(includeClassNamePatterns(".*Test"))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        final TestExecutionSummary summary = listener.getSummary();
        System.out.println("=========================");
        System.out.println("Nb tests:" + summary.getTestsFoundCount());
        System.out.println("Succes:" + summary.getTestsSucceededCount());
        System.out.println("Echec:" + summary.getTestsFailedCount());
        System.out.println("=========================");

        return this;
    }

    public RunAll generateFullDoc() throws IOException {
        MainDocumentation.main();
        return this;
    }

    public RunAll procesExec(String command) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");


        String homeDirectory = System.getProperty("user.home");
        Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec(String.format("cmd.exe /c dir %s", homeDirectory));
        } else {
            process = Runtime.getRuntime()
                    .exec(String.format(command));
        }
        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;

        return this;
    }

    public RunAll startProcessBuilder(String command) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase()
                .startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "dir");
        } else {
            builder.command("bash", "-c", command);
        }

        builder.directory(new java.io.File(System.getProperty("user.dir")));
        Process process = builder.start();

        StreamGobbler streamGobbler =
                new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        //    assert exitCode == 0;
        System.exit(0);

        return this;
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

}
