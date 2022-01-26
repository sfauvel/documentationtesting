package org.sfvl.printer;

public class CodeAndResult<T> {
    private final String code;
    private final T value;

    public CodeAndResult(String code, T value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public T getValue() {
        return value;
    }
}
