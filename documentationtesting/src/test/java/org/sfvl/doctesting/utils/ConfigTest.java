package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.docformatter.AsciidocFormatter;
import org.sfvl.docformatter.Formatter;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
            final Path path = Config.DOC_PATH.resolve(DocumentationNamer.toPath(this.getClass().getPackage()));


            final String configFile = "testConfig.properties";
            doc.write("", "",
                    "You can change those folders by creating a properties file named `" + Config.DEFAULT_CONFIGURATION_FILE + "`" +
                            " and put it in a resource folder.",
                    "",
                    "When this file contains:",
                    "----",
                    String.format("include::%s[]", path.relativize(Paths.get("src", "test", "resources")).resolve(configFile).toString()),
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

            doc.write("", "",
                    legend,
                    "",
                    Arrays.stream(Config.Key.values()).
                            map(key -> String.format("* %s=%s", key.name(), config.getPath(key).toString()))
                            .collect(Collectors.joining("\n"))
            );
        } catch (Exception e) {
            e.printStackTrace();
            doc.write("", "", "Exception: " + e.getMessage());
        }
    }
}


