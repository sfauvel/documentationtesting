package org.sfvl.doctesting.utils;

import org.sfvl.docformatter.Formatter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    public static final String DEFAULT_CONFIGURATION_FILE = "docAsTest.properties";
    public static final String DOC_PATH_TAG = "ROOT_PATH";

    static enum Key {
        /** Path of source code. */
        SOURCE_PATH,
        /** Path of test code. */
        TEST_PATH,
        /** Path where documentation is generated. */
        DOC_PATH,
        /** Path of resources. */
        RESOURCE_PATH,
        /** Full qualified name of the formatter class. */
        FORMATTER;
    }

    private static Config instance = new Config();

    public static final Path SOURCE_PATH = instance.getSourcePath();
    public static final Path TEST_PATH = instance.getTestPath();
    public static final Path DOC_PATH = instance.getDocPath();
    public static final Path RESOURCE_PATH = instance.getResourcePath();
    public static final Formatter FORMATTER = instance.getFormatter();

    private Properties prop = new Properties();

    public Config() {
        this(DEFAULT_CONFIGURATION_FILE);
    }

    public Config(String configFile) {
        setDefaultProperties();
        loadProperties(configFile);
    }

    private Path getSourcePath() {
        return getPath(Key.SOURCE_PATH);
    }

    private Path getTestPath() {
        return getPath(Key.TEST_PATH);
    }

    private Path getDocPath() {
        return getPath(Key.DOC_PATH);
    }

    private Path getResourcePath() {
        return getPath(Key.RESOURCE_PATH);
    }

    private Formatter getFormatter() {
        final String property = getProperty(Key.FORMATTER);
        try {
            final Class<?> classFormatter = Class.forName(property);
            return (Formatter) classFormatter.getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Unable to instantiate formatter from `̀%s`", property), e);
        }
    }

    Path getPath(Key key) {
        return Paths.get(getProperty(key));
    }

    String getProperty(Key key) {
        return prop.getProperty(key.name());
    }

    private void setDefaultProperties() {
        prop.setProperty(Key.SOURCE_PATH.name(), Paths.get("src", "main", "java").toString());
        prop.setProperty(Key.TEST_PATH.name(), Paths.get("src", "test", "java").toString());
        prop.setProperty(Key.DOC_PATH.name(), Paths.get("src", "test", "docs").toString());
        prop.setProperty(Key.RESOURCE_PATH.name(), Paths.get("src", "test", "resources").toString());
        prop.setProperty(Key.FORMATTER.name(), "org.sfvl.docformatter.asciidoc.AsciidocFormatter");
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
