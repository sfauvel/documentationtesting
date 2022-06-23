package tools;

import com.intellij.psi.PsiFile;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldAutoNaming {

    protected FieldAutoNaming() {
        final Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            final String name = declaredField.getName();
            final String[] tokens = name.split("_");

            String separator = "/";
            String value = tokens[0];
            for (int i = 1; i < tokens.length; i++) {

                if (tokens[i].startsWith("file")) {
                    // We start alls file by "_File" because it's the standard naming for our approved files on classes.
                    // It should be more flexible.
                    value += separator + "_"
                            + tokens[i].substring(0,1).toUpperCase()
                            + tokens[i].substring(1);
                    separator = ".";
                } else {
                    value += separator + tokens[i];
                }
            }

            try {
                declaredField.set(this, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> getAllFileNames() {
        return Arrays.stream(this.getClass().getDeclaredFields())
                .map(field -> {
                    try {
                        return field.get(this).toString();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error reading field value of " + field.getName(), e);
                    }
                })
                .collect(Collectors.toList());

    }

}
