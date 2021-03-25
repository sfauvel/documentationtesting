package org.sfvl.doctesting;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * You can use this class to generate a main documentation that aggregate other documentations
 * and in particular, those generated from test classes.
 */
public class BuilderDocumentation {

    protected final String documentationTitle;
    protected final Formatter formatter;
    private Path location = Paths.get("");

    public BuilderDocumentation withLocation(Package packageLocation) {
        return withLocation(DocumentationNamer.toPath(packageLocation));
    }

    public BuilderDocumentation withLocation(Path location) {
        this.location = location;
        return this;
    }

    public BuilderDocumentation withClassesToInclude(Class<?>... classesToInclude) {
        return withClassesToInclude(Arrays.asList(classesToInclude));
    }

    public BuilderDocumentation withClassesToInclude(List<Class<?>> classesToInclude) {
        this.classesToInclude = classesToInclude;
        return this;
    }

    private List<Class<?>> classesToInclude;

    private final List<Function<BuilderDocumentation, String>> docStructure = new ArrayList<>();

    public BuilderDocumentation() {
        this("Documentation");
    }

    public BuilderDocumentation(String documentationTitle) {
        this(documentationTitle, new AsciidocFormatter());
    }

    public BuilderDocumentation(String documentationTitle,
                                Formatter formatter) {
        this.documentationTitle = documentationTitle;
        this.formatter = formatter;

        withStructure(builder -> builder.getDocumentOptions(),
                builder -> "= " + getDocumentTitle(),
                builder -> builder.includeClasses());
    }

    public BuilderDocumentation withStructure(Function<BuilderDocumentation, String>... structure) {
        return withStructure(Arrays.asList(structure));
    }

    public BuilderDocumentation withStructure(List<Function<BuilderDocumentation, String>> structure) {
        docStructure.clear();
        docStructure.addAll(structure);
        return this;
    }

    private String apply(Function<BuilderDocumentation, String> f) {
        return f.apply(this);
    }
    public static class Option {
        String key;
        String value;

        public Option(String key) {
            this(key, "");
        }

        public Option(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String format() {
            return String.format(":%s: %s", key, value).trim();
        }

    }

    private final List<Option> options = new ArrayList<Option>() {{
        add(new Option("toc", "left"));
        add(new Option("nofooter"));
        add(new Option("stem"));
    }};

    protected String getDocumentOptions() {
        return options.stream()
                .map(Option::format)
                .collect(Collectors.joining("\n"));
    }

////////////////////////////////////


    private String getDocumentTitle() {
        return this.documentationTitle;
    }

    public String getDoc() {
        return docStructure.stream()
                .map(this::apply)
                .collect(Collectors.joining("\n"));
    }

    public String includeClasses() {
        return includeClasses(location, classesToInclude);
    }
    private String includeClasses(Path location, List<Class<?>> classesToInclude) {

        return classesToInclude.stream()
                .map(c -> getRelativeFilePath(location, c))
                .map(path -> formatter.include(path.toString()).trim())
                .collect(Collectors.joining("\n\n", "\n", "\n"));
    }

    private Path getRelativeFilePath(Path docPath, Class<?> clazz) {
        final Path classPath = DocumentationNamer.toPath(clazz, "", ".adoc");

        return docPath.relativize(classPath);
    }
}
