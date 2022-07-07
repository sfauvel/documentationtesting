package docAsTest.approvalFile;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBuilder {

    public final String projectRootPath;
    public final Optional<String> packagePath;
    public final String filePath;

    FileBuilder(String projectRootPath, Optional<String> packagePath, String filePath) {
        this.projectRootPath = projectRootPath;
        this.packagePath = packagePath;
        this.filePath = filePath;
    }

    static public Optional<FileBuilder> extractFileInfo(final Path projectPath,
                                                        final VirtualFile file,
                                                        final String prefixFolder) {
        if (file == null) {
            return Optional.empty();
        }
        Pattern pattern = Pattern.compile("(" + Paths.get(projectPath + File.separator).toString() + "(.*" + File.separator + ")?)"
                + prefixFolder + File.separator
                + "(.*" + File.separator + ")?"
                + "(" + file.getName() + ")");
        Matcher matcher = pattern.matcher(file.getPath());
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(new FileBuilder(
                matcher.group(1),
                Optional.ofNullable(matcher.group(3)),
                matcher.group(4)
        ));

    }
}
