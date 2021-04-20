package org.sfvl.doctesting.writer;

import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.utils.DocumentationNamer;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Classes {
    private final Formatter formatter;

    public Classes(Formatter formatter) {
        this.formatter = formatter;
    }

    public String includeClasses(Path location, List<Class<?>> classesToInclude) {

        return classesToInclude.stream()
                .map(c -> getRelativeFilePath(location, c))
                .map(path -> formatter.include(path.toString()).trim())
                .collect(Collectors.joining("\n\n", "\n", "\n"));
    }

    private Path getRelativeFilePath(Path docPath, Class<?> clazz) {
        final Path classPath = DocumentationNamer.toPath(clazz, "", ".approved.adoc");

        return docPath.relativize(classPath);
    }
}
