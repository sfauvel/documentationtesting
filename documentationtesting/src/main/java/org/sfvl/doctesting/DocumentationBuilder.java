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
public class DocumentationBuilder {

    protected final String documentationTitle;
    protected final Formatter formatter;
    private Path location = Paths.get("");

    private List<Class<?>> classesToInclude = Collections.emptyList();

    private final List<Function<DocumentationBuilder, String>> docStructure = new ArrayList<>();

    private final List<Option> options = new ArrayList<Option>() {{
        add(new Option("toc", "left"));
        add(new Option("nofooter"));
        add(new Option("stem"));
    }};

    public DocumentationBuilder() {
        this("Documentation");
    }

    public DocumentationBuilder(String documentationTitle) {
        this(documentationTitle, new AsciidocFormatter());
    }

    public DocumentationBuilder(String documentationTitle,
                                Formatter formatter) {
        this.documentationTitle = documentationTitle;
        this.formatter = formatter;

        withStructure(builder -> builder.getDocumentOptions(),
                builder -> "= " + getDocumentTitle(),
                builder -> builder.includeClasses());
    }

    public DocumentationBuilder withLocation(Package packageLocation) {
        return withLocation(DocumentationNamer.toPath(packageLocation));
    }

    public DocumentationBuilder withLocation(Path location) {
        this.location = location;
        return this;
    }

    public DocumentationBuilder withClassesToInclude(Class<?>... classesToInclude) {
        return withClassesToInclude(Arrays.asList(classesToInclude));
    }

    public DocumentationBuilder withClassesToInclude(List<Class<?>> classesToInclude) {
        this.classesToInclude = classesToInclude;
        return this;
    }

    public DocumentationBuilder withStructure(Function<DocumentationBuilder, String>... structure) {
        return withStructure(Arrays.asList(structure));
    }

    public DocumentationBuilder withStructure(List<Function<DocumentationBuilder, String>> structure) {
        docStructure.clear();
        docStructure.addAll(structure);
        return this;
    }

    public static class Option {
        final String key;
        final String value;

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

    public DocumentationBuilder withOptionAdded(String key, String value) {
        options.add(new Option(key, value));
        return this;
    }

    public DocumentationBuilder withOptionAdded(String key) {
        options.add(new Option(key));
        return this;
    }

    public DocumentationBuilder withOptionRemoved(String key) {
        options.removeIf(o -> o.key.equals(key));
        return this;
    }

    protected String getDocumentOptions() {
        return options.stream()
                .map(Option::format)
                .collect(Collectors.joining("\n"));
    }

    private String getDocumentTitle() {
        return this.documentationTitle;
    }

    public String build() {
        return docStructure.stream()
                .map(f -> f.apply(this))
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
