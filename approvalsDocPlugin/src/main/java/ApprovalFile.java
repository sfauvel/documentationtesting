import com.intellij.openapi.vfs.VirtualFile;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApprovalFile {
    private final String extension;
    private final String filename;
    private final Status status;

    public ApprovalFile(VirtualFile virtualFile) {
        this(virtualFile.getNameWithoutExtension(), null, virtualFile.getExtension());
    }

    public ApprovalFile(String filename, Status status, String extension) {
        this.filename = filename;
        this.status = status;
        this.extension = extension;
    }


    public static ApprovalFile fromClass(String packageName, String className) {
        final String fullName = getApprovedFileName(packageName, className);

        return new ApprovalFile(fullName, Status.RECEIVED, "adoc");
    }

    public static ApprovalFile fromMethod(String packageName, String className, String methodName) {
        final String fullName = String.format("%s.%s",
                getApprovedFileName(packageName, className),
                methodName);
        return new ApprovalFile(fullName, Status.RECEIVED, "adoc");
    }

    private static String getApprovedFileName(String packageName, String className) {
        packageName = packageName
                .replaceAll("\\.", "/");
        final String fullName = String.format("%s_%s",
                packageName.isEmpty() ? "" : packageName + "/",
                className);
        return fullName;
    }

    static class JavaApprovalFile extends ApprovalFile {


        private final Method method;

        public JavaApprovalFile(Method method) {
            super(method.getName(), null, "java");
            this.method = method;
        }

        @Override
        public ApprovalFile to(Status status) {
            final String methodName = method.getName();
            final Class<?> declaringClass = method.getDeclaringClass();
            final String packageName = declaringClass.getPackageName()
                    .replaceAll("\\.", "/");
            final String fullName = String.format("%s/_%s.%s",
                    packageName,
                    declaringClass.getSimpleName(),
                    methodName);

            return new ApprovalFile(fullName, status, "adoc");
        }
    }

    public static ApprovalFile valueOf(Method testMethod) {
        return new JavaApprovalFile(testMethod);
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
            return Optional.of(new ApprovalFile(
                    matcher.group(1),
                    matcher.group(2).equals(getStatusName(Status.RECEIVED))
                            ? Status.RECEIVED
                            : Status.APPROVED,
                    matcher.group(3))
            );
        }
    }

    static boolean isReceivedFilename(String filename) {
        return valueOf(filename)
                .filter(ApprovalFile::isReceived)
                .isPresent();
    }

    public boolean isReceived() {
        return status == Status.RECEIVED;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public String getName() {
        return String.format("%s.%s.%s",
                filename,
                getStatusName(status),
                extension
        );
    }

    public String getFileName() {
        return String.format("%s.%s.%s",
                Paths.get(filename).getFileName().toString(),
                getStatusName(status),
                extension
        );
    }

    public String getTestFile() {
        final Path path = Paths.get(filename);
        final String localFileName = path.getFileName().toString()
                .replaceFirst("(^)_", "")
                .split("\\.")[0];


        final String javaFileName = String.format("%s.%s",
                localFileName.substring(0, 1).toUpperCase() + localFileName.substring(1, localFileName.length()),
                "java"
        );

        final Path parent = path.getParent();
        if (parent != null) {
            return Paths.get(parent.toString().replace("src/test/docs", "src/test/java"))
                    .resolve(javaFileName).toString();
        } else {
            return javaFileName;
        }

    }

    private static String getStatusName(Status status) {
        switch (status) {
            case APPROVED:
                return "approved";
            case RECEIVED:
                return "received";
            default:
                throw new InvalidParameterException("Status not mapped: " + status);
        }
    }

    public ApprovalFile to(Status approved) {
        return new ApprovalFile(this.filename, approved, this.extension);
    }

    public static enum Status {
        RECEIVED,
        APPROVED
    }
}
