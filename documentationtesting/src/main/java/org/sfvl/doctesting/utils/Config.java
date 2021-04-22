package org.sfvl.doctesting.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static final Path SOURCE_PATH = Paths.get("src", "main", "java");
    public static final Path TEST_PATH = Paths.get("src", "test", "java");
    public static final Path DOC_PATH = Paths.get("src", "test", "docs");
}
