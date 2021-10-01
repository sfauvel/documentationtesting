import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ApprovedFile extends ApprovalFile {

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

class JavaFile extends ApprovalFile {

    public JavaFile(String packageName, String className) {
        this(packageName, className, null);
    }

    public JavaFile(String packageName, String className, String methodName) {
        this(Paths.get(packageName.replace('.', File.separatorChar)), className, methodName);
    }

    public JavaFile(Path packagePath, String className, String methodName) {
        super(packagePath, className, methodName, null, "java");
    }

    @Override
    public String getName() {
        return String.format("%s.%s", getPath().resolve(getClassName()), getExtension());
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", getClassName(), getExtension());
    }

    @Override
    public ApprovalFile to(Status approved) {
        return new ApprovedFile(getPath(), getClassName(), getMethodName(), approved);
    }
}

public abstract class ApprovalFile {

    private final Path path;
    protected final String className;
    protected final String methodName;
    private final String extension;
    protected final Status status;

    public ApprovalFile(VirtualFile virtualFile) {
        this(virtualFile.getNameWithoutExtension(), null, virtualFile.getExtension());
    }

    public ApprovalFile(String className, Status status, String extension) {
        this.path = Paths.get("");
        this.className = className;
        this.methodName = null;
        this.status = status;
        this.extension = extension;
    }

    public ApprovalFile(Path path, Status status, String extension) {
        this(path, "", status, extension);
    }

    public ApprovalFile(Path path, String className, Status status, String extension) {
        this(path, className, null, status, extension);
    }

    public ApprovalFile(Path path, String className, String methodName, Status status, String extension) {
        this.path = path == null ? Paths.get("") : path;
        this.className = className;
        this.methodName = methodName;
        this.status = status;
        this.extension = extension;
    }

    public Path getPath() {
        return path;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isReceived() {
        return status == Status.RECEIVED;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public abstract String getName();

    public abstract String getFileName();

    public ApprovalFile to(Status approved) {
        return new ApprovedFile(getPath(), this.className, null, approved);
    }

    public String getTestFile() {
        return new JavaFile(path, className.split("\\.")[0], null).getName();
    }

    public static ApprovalFile fromClass(String packageName, String className) {
        return new JavaFile(packageName, className);
    }

    public static ApprovalFile fromMethod(String packageName, String className, String methodName) {
        return new JavaFile(packageName, className, methodName);
    }

    public static Optional<ApprovalFile> valueOf(String fullFileName) {
        String approvalWords = Arrays.stream(Status.values())
                .map(ApprovalFile::getStatusName)
                .collect(Collectors.joining("|"));


        Pattern approvalFilePattern = Pattern.compile("(.*)\\.(" + approvalWords + ")\\.([^\\.]+)");
        Matcher matcher = approvalFilePattern.matcher(fullFileName);
        if (!matcher.matches()) {
            return Optional.empty();
        } else {
            final Path pathFound = Paths.get(matcher.group(1));
            final Path path = pathFound.getParent();

//            final Path pathDocs = Paths.get("src", "test", "docs");
//            final Path path = pathFound.startsWith(pathDocs)
//                ? pathDocs.relativize(pathFound.getParent())
//                : pathFound.getParent();
            final Path fileName = pathFound.getFileName();
            if (!fileName.toString().startsWith("_")) {
                return Optional.empty();
            }
            return Optional.of(new ApprovedFile(
                    path,
                    fileName.toString().replaceFirst("^_", ""),
                    null,
                    matcher.group(2).equals(getStatusName(Status.RECEIVED))
                            ? Status.RECEIVED
                            : Status.APPROVED)
            );
        }
    }

    static boolean isReceivedFilename(String filename) {
        return valueOf(filename)
                .filter(ApprovalFile::isReceived)
                .isPresent();
    }

    protected static String getStatusName(Status status) {
        switch (status) {
            case APPROVED:
                return "approved";
            case RECEIVED:
                return "received";
            default:
                throw new InvalidParameterException("Status not mapped: " + status);
        }
    }

    public static enum Status {
        RECEIVED,
        APPROVED
    }

}
