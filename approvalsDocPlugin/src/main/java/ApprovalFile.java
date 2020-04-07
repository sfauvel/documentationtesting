import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApprovalFile {
    private final String extension;
    private final String filename;
    private final Status status;

    Map<Status, String> statusExtension = new HashMap<>();

    public ApprovalFile(VirtualFile virtualFile) {
        this(virtualFile.getNameWithoutExtension(), null, virtualFile.getExtension());
    }

    public ApprovalFile(String filename, Status status, String extension) {
        this.filename = filename;
        this.status = status;
        this.extension = extension;
    }

    public static Optional<ApprovalFile> valueOf(String fullFileName) {
        String approvalWords = Arrays.stream(Status.values())
                .map(ApprovalFile::getStatusName)
                .collect(Collectors.joining("|"));
        Pattern approvalFilePattern = Pattern.compile("(.*)\\.("+approvalWords+")\\.([^\\.]+)");
        Matcher matcher = approvalFilePattern.matcher(fullFileName);
        if(!matcher.matches()) {
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

    private static String getStatusName(Status status) {
        switch (status) {
            case APPROVED: return "approved";
            case RECEIVED: return "received";
            default: throw new InvalidParameterException("Status not mapped: "  + status);
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
