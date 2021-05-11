package org.sfvl.doctesting.utils;

import java.nio.file.Paths;

public class DocPath {

    private final Class<?> clazz;

    public DocPath(Class<?> clazz) {
        this.clazz = clazz;
    }

    public OnePath approved() {
        return new OnePath(clazz, Config.DOC_PATH, ".approved.adoc");
    }

    public OnePath received() {
        return new OnePath(clazz, Config.DOC_PATH, ".received.adoc");
    }

    public OnePath test() {
        return new OnePath(clazz, Config.TEST_PATH, ".java");
    }

    public OnePath doc() {
        return new OnePath(clazz, Paths.get(""), ".html");
    }
}
