package org.sfvl.doctesting.writer;

public class Option {
    final String key;
    final String value;

    public Option(String key) {
        this(key, "");
    }

    public Option(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String format() {
        return String.format(":%s: %s", key, value).trim();
    }

}
