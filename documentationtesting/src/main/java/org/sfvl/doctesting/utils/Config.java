package org.sfvl.doctesting.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    public static final String DEFAULT_CONFIGURATION_FILE = "docAsTest.properties";
    public static final String DOC_PATH_TAG = "ROOT_PATH";

    enum Key {
        SOURCE_PATH, TEST_PATH, DOC_PATH, RESOURCE_PATH;
    }
    private static Config instance = new Config();

    public static final Path SOURCE_PATH = instance.getSourcePath();
    public static final Path TEST_PATH = instance.getTestPath();
    public static final Path DOC_PATH = instance.getDocPath();
    public static final Path RESOURCE_PATH = instance.getResourcePath();

    private Properties prop = new Properties();

    public Config() {
        this(DEFAULT_CONFIGURATION_FILE);
    }

    public Config(String configFile) {
        setDefaultProperties();
        loadProperties(configFile);
    }

    public Path getSourcePath() {
        return getPath(Key.SOURCE_PATH);
    }

    public Path getTestPath() {
        return getPath(Key.TEST_PATH);
    }

    public Path getDocPath() {
        return getPath(Key.DOC_PATH);
    }

    public Path getResourcePath() {
        return getPath(Key.RESOURCE_PATH);
    }

    public Path getPath(Key key) {
        return Paths.get(prop.getProperty(key.name()));
    }

    private void setDefaultProperties() {
        prop.setProperty(Key.SOURCE_PATH.name(), Paths.get("src", "main", "java").toString());
        prop.setProperty(Key.TEST_PATH.name(), Paths.get("src", "test", "java").toString());
        prop.setProperty(Key.DOC_PATH.name(), Paths.get("src", "test", "docs").toString());
        prop.setProperty(Key.RESOURCE_PATH.name(), Paths.get("src", "test", "resources").toString());
    }

    private void loadProperties(String name) {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(name)) {
            if (input != null) {
                prop.load(input);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading resource file " + name, ex);
        }
    }


}
