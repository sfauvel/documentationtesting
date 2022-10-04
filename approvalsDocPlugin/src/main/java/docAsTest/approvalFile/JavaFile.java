package docAsTest.approvalFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaFile extends ApprovalFile {

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
        return String.format("%s.%s", getPath().resolve(getMainClass()), getExtension());
    }

    @Override
    public String getFileName() {
        return String.format("%s.%s", getMainClass(), getExtension());
    }

    @Override
    public ApprovalFile to(Status approved) {
        return new ApprovedFile(getPath(), className, getMethodName(), approved);
    }

    private String getMainClass() {
        return getClassName().split("\\.")[0];
    }
}
