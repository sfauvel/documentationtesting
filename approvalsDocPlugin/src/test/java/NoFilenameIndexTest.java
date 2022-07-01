import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoFilenameIndexTest extends TestCase {
    public void testNoFilenameIndexUsed() throws IOException {
        try (Stream<Path> pathsInSource = Files.list(Paths.get("src/main/java"))) {
            final List<Path> filesImportingFilenameIndex = pathsInSource
                    .filter(path -> path.toFile().isFile())
                    .filter(this::importFilenameIndex)
                    .collect(Collectors.toList());

            if (!filesImportingFilenameIndex.isEmpty()) {
                fail("FilenameIndex is imported in: \n" + filesImportingFilenameIndex.stream()
                        .map(path -> "  - " + path)
                        .collect(Collectors.joining("\n")) +
                        "\nUse docAsTest/DocAsTestFilenameIndex instead");
            }
        }
    }

    private boolean importFilenameIndex(Path file) {
        try {
            String read = Files.readAllLines(file).stream().collect(Collectors.joining(" "));
            return read.matches(".*import +com\\.intellij\\.psi\\.search\\.FilenameIndex;.*");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NoFilenameIndexTest.importFilenameIndex; IOException reading " + file);
            return false;
        }
    }

}
