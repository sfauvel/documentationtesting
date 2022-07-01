package docAsTest.approvalFile;

import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return new ApprovedFile(getPath(), this.className, this.methodName, approved);
    }

    public String getTestFile() {
        return toJava().getName();
    }

    public JavaFile toJava() {
        return new JavaFile(path, getClassName(), getMethodName());
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

            final Path fileName = pathFound.getFileName();
            if (!fileName.toString().startsWith("_")) {
                return Optional.empty();
            }
            final String[] split = fileName.toString().split("\\.");
            final String className = split.length == 1 ? split[0] : Arrays.stream(split).limit(split.length-1).collect(Collectors.joining("."));
            final String methodName = split.length == 1 ? null: split[split.length-1];

            return Optional.of(new ApprovedFile(
                    path,
                    className.replaceFirst("^_", ""),
                    methodName,
                    matcher.group(2).equals(getStatusName(Status.RECEIVED))
                            ? Status.RECEIVED
                            : Status.APPROVED)
            );
        }
    }

    public static boolean isReceivedFilename(String filename) {
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
