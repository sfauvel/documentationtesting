package org.sfvl.doctesting.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OnePath extends DocPath {
    /// extension of the file.
    protected final String suffix;
    /// Root path of the type of file.
    protected final Path rootPath;

    public OnePath(Path rootPath, Path folder, String rootName, String suffix) {
        super(folder, rootName);
        this.suffix = suffix;
        this.rootPath = rootPath;
    }

    public Path path() {
        return rootPath.resolve(packagePath()).resolve(filename());
    }

    public Path folder() {
        return rootPath.resolve(packagePath());
    }

    public String filename() {
        return name() + suffix;
    }

    public Path from(Path pathToRelativized) {
        return pathToRelativized.relativize(this.path());
    }
    public Path from(Class<?> classToRelativized) {
        return from(new DocPath(classToRelativized).approved().folder());
    }
    public Path from(OnePath classToRelativized) {
        return classToRelativized.folder().relativize(this.path());
    }
    public Path to(OnePath classToRelativized) {
        return this.folder().relativize(classToRelativized.path());
    }
}

class ApprovalPath extends OnePath {
    public ApprovalPath(DocPath docPath, String suffix) {
        super(Config.DOC_PATH, docPath.packagePath(), docPath.name(), suffix);
    }

    @Override
    public String filename() {
        return "_" + name() + suffix;
    }
}

class ApprovedPath extends ApprovalPath {
    public ApprovedPath(DocPath docPath) {
        super(docPath, ".approved.adoc");
    }
}

class ReceivedPath extends ApprovalPath {
    public ReceivedPath(DocPath docPath) {
        super(docPath, ".received.adoc");
    }
}

class TestPath extends OnePath {
    public TestPath(DocPath docPath) {
        super(Config.TEST_PATH, docPath.packagePath(), docPath.name(), ".java");
    }
}

class ResourcePath extends OnePath {
    public ResourcePath(DocPath docPath) {
        super(Config.RESOURCE_PATH, docPath.packagePath(), docPath.name(), ".adoc");
    }
}

class HtmlPath extends OnePath {
    public HtmlPath(DocPath docPath) {
        super(Paths.get(""), docPath.packagePath(), docPath.name(), ".html");
    }
}

class PagePath extends OnePath {
    public PagePath(DocPath docPath) {
        super(Config.DOC_PATH, docPath.packagePath(), docPath.name(), ".adoc");
    }
}
