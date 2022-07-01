package tools;

import java.nio.file.Path;

public class CodeGenerator {
    private String className = "MyClass";
    private String method = "myMethod";
    private String fullPackage = null;

    String generate(FileHelper.CaretOn caretOn) {
        return String.format("%simport %sorg.demo; class %s%s { public void %s%s() {} class %sInnerClass{ public void %sinnerMethod() {} } }",
//                    CaretOn.NONE.equals(caretOn) ? "<caret>" : "",
                this.fullPackage != null ? "package " + fullPackage + "; " : "",
                FileHelper.CaretOn.IMPORT.equals(caretOn) ? "<caret>" : "",
                FileHelper.CaretOn.CLASS.equals(caretOn) ? "<caret>" : "",
                this.className,
                FileHelper.CaretOn.METHOD.equals(caretOn) ? "<caret>" : "",
                this.method,
                FileHelper.CaretOn.INNER_CLASS.equals(caretOn) ? "<caret>" : "",
                FileHelper.CaretOn.INNER_METHOD.equals(caretOn) ? "<caret>" : "");
    }

    public CodeGenerator withClass(String className) {
        this.className = className;
        return this;
    }

    public CodeGenerator withMethod(String method) {
        this.method = method;
        return this;
    }

    public CodeGenerator withPackage(String fullPackage) {
        this.fullPackage = fullPackage;
        return this;
    }

    public static String generateCode(Path packagePath, String className, FileHelper.CaretOn caretOn) {
        return new CodeGenerator()
                .withPackage(packagePath.toString().replace(java.io.File.separatorChar, '.'))
                .withClass(className)
                .generate(caretOn);
    }

    public static String generateCode(Path packagePath, FileHelper.CaretOn caretOn) {
        return String.format("package %s;\n%s",
                packagePath.toString().replace(java.io.File.separatorChar, '.'),
                generateCode(caretOn));
    }

    public static String generateCode(String className, FileHelper.CaretOn caretOn) {
        return new CodeGenerator()
                .withClass(className)
                .generate(caretOn);
    }

    public static String generateCode(FileHelper.CaretOn caretOn) {
        return new CodeGenerator()
                .generate(caretOn);
    }
}
