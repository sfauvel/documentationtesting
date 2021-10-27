package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.Formatter;
import org.sfvl.docformatter.asciidoc.AsciidocFormatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@DisplayName(value = "Configuration")
class ConfigTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void default_values() throws IOException {
        final String legend = "By default, values are:";
        displayConfig(legend, new Config());
    }

    @Test
    @DisplayName("Configuration file")
    public void load_path_from_config_file() throws IOException {

        {
            Formatter formatter = new AsciidocFormatter();
            final Path path = new DocPath(this.getClass()).page().folder();

            final String configFile = "testConfig.properties";
            doc.write("", "",
                    "You can change those folders by creating a properties file named `" + Config.DEFAULT_CONFIGURATION_FILE + "`" +
                            " and put it in a resource folder.",
                    "",
                    "When this file contains:",
                    "----",
                    formatter.include(path.relativize(Paths.get("src", "test", "resources")).resolve(configFile).toString()),
                    "----");

            final String legend = "Values are:";
            displayConfig(legend, new Config(configFile));
        }

        {
            final String configFile = "notFound.properties";

            Function<Config, String> format = c -> Arrays.stream(Config.Key.values()).
                    map(key -> String.format("* %s=%s", key.name(), c.getPath(key).toString()))
                    .collect(Collectors.joining("\n"));

            final Config config = new Config(configFile);
            if (format.apply(new Config()).equals(format.apply(config))) {
                doc.write("", "", "When config file does not exist, values are the default ones.");
            } else {
                final String legend = "When config file does not exist, values are:";
                displayConfig(legend, config);
            }
        }
    }

    private void displayConfig(String legend, Config config) {
        try {

            final List<Method> getterMethods = Arrays.stream(Config.class.getDeclaredMethods())
                    .filter(this::isGetter)
                    .sorted(Comparator.comparing(Method::getName))
                    .collect(Collectors.toList());

            doc.write("", "",
                    legend,
                    "");

            doc.write("[%header]",
                    "|====",
                    "| Method | Type | Value",
                    "");

            for (Method declaredMethod : getterMethods) {
                final Object value = declaredMethod.invoke(config);
                String textToDisplay = textToDisplay(value);
                doc.write(String.format("| %s | %s | %s", declaredMethod.getName(), declaredMethod.getReturnType().getSimpleName(), textToDisplay), "");
            }
            doc.write("|====", "");

        } catch (Exception e) {
            e.printStackTrace();
            doc.write("", "", "Exception: " + e.getMessage());
        }
    }

    private String textToDisplay(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Path) {
            return DocPath.toAsciiDoc((Path)value);
        }

        if (value.toString().startsWith(value.getClass().getName() + "@")) {
            return "instance of " + value.getClass().getSimpleName();
        }

        return value.toString();
    }

    private boolean isGetter(Method declaredMethod) {
        return declaredMethod.getName().startsWith("get")
                && Modifier.isPublic(declaredMethod.getModifiers())
                && !Modifier.isStatic(declaredMethod.getModifiers());
    }
}


