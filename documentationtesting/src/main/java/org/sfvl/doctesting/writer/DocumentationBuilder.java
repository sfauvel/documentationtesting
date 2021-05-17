package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.doctesting.utils.DocPath;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * You can use the DocumentationBuilder class to generate a documentation.
 * You can aggregate other documentations and in particular, those generated from test classes.
 *
 * @deprecated This class was used to create a dynamic builder but it does not simplify the code
 * so we prefer to not use it.
 */
@Deprecated
public class DocumentationBuilder implements DocumentProducer {


    @Override
    public void produce() throws IOException {
        new Document(this.build()).saveAs(new DocPath(this.getClass()).page());
    }

    static private class DocBuilder<T> {

        public final List<Function<T, String>> docStructure = new ArrayList<>();

        public String build(T obj) {
            return docStructure.stream()
                    .map(f -> f.apply(obj))
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.joining("\n"));
        }

        DocBuilder<T> insert(Function<T, String> fn) {
            docStructure.add(fn);
            return this;
        }

    }

    protected final String documentationTitle;
    protected final Formatter formatter;
    private Path location = Paths.get("");

    private List<Class<?>> classesToInclude = Collections.emptyList();

    private DocBuilder docBuilder;

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

        withStructureBuilder((Class<DocumentationBuilder>) this.getClass(),
                b -> b.getDocumentOptions(),
                b -> "= " + b.getDocumentTitle(),
                b -> b.getContent(),
                b -> b.getFooter());
    }

    protected String getContent() {
        return includeClasses();
    }

    protected String getFooter() {
        return "";
    }

    public DocumentationBuilder withLocation(Package packageLocation) {
        return withLocation(DocPath.toPath(packageLocation));
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

    /**
     * There is no verification at compile time between parameter clazz and class of the instance.
     * Instance must be a parent of parameter clazz.
     *
     * @param clazz
     * @param structure
     * @param <T>
     * @return
     */
    public <T> DocumentationBuilder withStructureBuilder(Class<T> clazz, Function<T, String>... structure) {
        if (!clazz.isAssignableFrom(this.getClass())) {
            throw new RuntimeException(
                    String.format("Wrong type: %s is not a super class of %s", this.getClass().getSimpleName(), clazz.getSimpleName()));
        }
        docBuilder = new DocBuilder<T>();
        Arrays.stream(structure)
                .forEachOrdered(fn -> docBuilder.insert(fn));
        return this;
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

    protected String getDocumentTitle() {
        return this.documentationTitle;
    }

    public String build() {
        return docBuilder.build(this);
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
        return new DocPath(clazz).page().from(Config.DOC_PATH.resolve(docPath));
    }

}


