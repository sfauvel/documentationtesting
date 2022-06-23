package docAsTest.approvalFile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ApprovedFile extends ApprovalFile {

    public ApprovedFile(String filename, Status status) {
        super(filename, status, "adoc");
    }

    public ApprovedFile(Path path, String fileName, String methodName, Status status) {
        super(path,
                fileName,
                methodName,
                status,
                "adoc");
    }

    @Override
    public String getFileName() {
        return String.format("%s%s.%s.%s",
                Paths.get("_" + className).getFileName().toString(),
                getMethodName() == null ? "" : "." + getMethodName(),
                getStatusName(status),
                getExtension()
        );
    }

    @Override
    public String getName() {
        return String.format("%s%s.%s.%s",
                getPath().resolve("_" + className),
                getMethodName() == null ? "" : "." + getMethodName(),
                getStatusName(status),
                getExtension()
        );
    }

}
