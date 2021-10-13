package fr.sfvl;

import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.FindLambdaMethod;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class MyFormatter extends AsciidocFormatter {
    public <T> List<T> $(T... xx) {
        return Arrays.asList(xx);
    }

    public String table(String title, List<List<String>> values) {

        Function<List<String>, String> formatLine = cells -> cells.stream()
                .map(cell -> "^.a| " + cell)
                .collect(Collectors.joining("\n"));

        final String allCells = values.stream()
                .map(formatLine)
                .collect(Collectors.joining("\n"));

        return paragraph("",
                "[cols=" + values.size() + "]",
                "[." + title + "]",
                "|====",
                allCells,
                "|====");
    }

    public String style(String content) {
        return paragraph(
                "++++",
                "<style>",
                content,
                "</style>",
                "++++");
    }

    public <T> String linkToMethod(FindLambdaMethod.SerializableConsumer<T> methodToInclude) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        String methodName = method.getName();
        String title = methodName.replace("_", " ");
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        return linkToMethod(method, title);
    }

    public <T> String linkToMethod(FindLambdaMethod.SerializableConsumer<T> methodToInclude, String title) {
        final Method method = FindLambdaMethod.getMethod(methodToInclude);

        return linkToMethod(method, title);
    }

    public <T> String linkToMethod(Method method, String title) {
        final DocPath docPath = new DocPath(method);
        try {
            generatePage(docPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return String.format("link:{%s}/%s[%s]\n",
                Config.DOC_PATH_TAG,
                new DocPath(method).html().path(),
                title);
    }

    private void generatePage(DocPath docPath) throws IOException {
        String includeContent = String.join("\n",
                "ifndef::ROOT_PATH[]",
                String.format(":ROOT_PATH: %s", docPath.html().folder().relativize(Paths.get(""))),
                "endif::[]",
                ":toc: left",
                ":nofooter:",
                ":stem:",
                ":source-highlighter: rouge",
                ":toclevels: 4",
                "",
                String.format("include::%s[]", docPath.approved().filename()));

        try (FileWriter fileWriter = new FileWriter(docPath.page().path().toFile())) {
            fileWriter.write(includeContent);
        }
    }
}
