import docAsTest.approvalFile.ApprovalFile;

import java.nio.file.Path;

public class FullApprovalFilePath {
    final Path rootPath;
    final Path typePath;
    final ApprovalFile file;

    public FullApprovalFilePath(Path rootPath, Path typePath, ApprovalFile file) {
        this.rootPath = rootPath;
        this.typePath = typePath;
        this.file = file;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public Path getTypePath() {
        return typePath;
    }

    public ApprovalFile getFile() {
        return file;
    }

    public Path fullPath() {
        return rootPath
                .resolve(typePath)
                .resolve(file.getName());
    }
}
