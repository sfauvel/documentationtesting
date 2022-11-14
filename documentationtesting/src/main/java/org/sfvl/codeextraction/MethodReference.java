package org.sfvl.codeextraction;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodReference {

    // Runnable

    public @FunctionalInterface
    static interface SerializableRunnable extends Runnable, Serializable {
    }

    public static String getName(SerializableRunnable consumer) {
        return getName(getSerializedLambda(consumer));
    }

    public static Method getMethod(SerializableRunnable lambda) {
        return getMethod(getSerializedLambda(lambda));
    }

    // Consumer

    public @FunctionalInterface
    static interface SerializableConsumer<T> extends Consumer<T>, Serializable {
    }

    public static <T1> String getName(SerializableConsumer<T1> consumer) {
        return getName(getSerializedLambda(consumer));
    }

    public static <T1> Method getMethod(SerializableConsumer<T1> lambda) {
        return getMethod(getSerializedLambda(lambda));
    }

    // BiConsumer

    public @FunctionalInterface
    static interface SerializableBiConsumer<T, V> extends BiConsumer<T, V>, Serializable {
    }

    public static <T1, V1> String getName(SerializableBiConsumer<T1, V1> consumer) {
        return getName(getSerializedLambda(consumer));
    }

    public static <T1, V1> Method getMethod(SerializableBiConsumer<T1, V1> lambda) {
        return getMethod(getSerializedLambda(lambda));
    }

    // Function

    public @FunctionalInterface
    static interface SerializableFunction<T, V> extends Function<T, V>, Serializable {
    }

    public static <T1, V1> String getName(SerializableFunction<T1, V1> function) {
        return getName(getSerializedLambda(function));
    }

    public static <T1, V1> Method getMethod(SerializableFunction<T1, V1> function) {
        return getMethod(getSerializedLambda(function));
    }

    public @FunctionalInterface
    static interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {
    }

//    public static <T1, U1, R1> String getName(SerializableBiFunction<T1, U1, R1> function) {
//        return getName(getSerializedLambda(function));
//    }

    public static <T1, U1, R1> Method getMethod(SerializableBiFunction<T1, U1, R1> function) {
        return getMethod(getSerializedLambda(function));
    }


    // Genereric for custom interfaces

    public static String getName(Serializable referenceMethod) {
        return getName(getSerializedLambda(referenceMethod));
    }

    public static Method getMethod(Serializable referenceMethod) {
        return getMethod(getSerializedLambda(referenceMethod));
    }

    // Private part

    private static String getName(SerializedLambda serializedLambda) {
        return serializedLambda.getImplMethodName();
    }

    private static Method getMethod(SerializedLambda serializedLambda) {
        final String methodName = serializedLambda.getImplMethodName();

        final Class<?> aClass = extractClass(serializedLambda);

        return Arrays.stream(aClass.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> serializedLambda.getImplMethodSignature().replace("/", ".").equals(getSignature(m)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No method found"));
    }

    private static Class<?> extractClass(SerializedLambda serializedLambda) {
        final String className = serializedLambda.getImplClass().replace("/", ".");

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * https://stackoverflow.com/questions/45072268/how-can-i-get-the-signature-field-of-java-reflection-method-object
     */
    private static String getSignature(Method method) {
        final String parameters = Arrays.stream(method.getParameterTypes())
                .map(MethodReference::serializedType)
                .collect(Collectors.joining("", "(", ")"));
        return parameters + serializedType(method.getReturnType());
    }

    private static String getSignatureWithGenerics(Method method) {
        Optional<String> signature = getFieldSignature(method);
        if (signature.isPresent()) {
            return signature.get();
        }

        return getSignature(method);
    }

    /**
     * Retrieve `signature` field that it can be null.
     * This field contains method signature with generics.
     *
     * @param method
     * @return
     */
    private static Optional<String> getFieldSignature(Method method) {
        try {
            Field field = Method.class.getDeclaredField("signature");
            field.setAccessible(true);
            return Optional.ofNullable((String) field.get(method));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    // https://docs.oracle.com/javase/7/docs/platform/serialization/spec/protocol.html
    private static String serializedType(Class<?> clazz) {
        if (clazz == void.class) {
            return "V";
        } else {
            String instance = Array.newInstance(clazz, 0).toString();
            return instance.substring(1, instance.indexOf('@'));
        }
    }

    /**
     * https://github.com/Hervian/safety-mirror
     *
     * @param lambda
     * @return
     */
    private static SerializedLambda getSerializedLambda(Serializable lambda) {

        Method method = getWriteReplaceMethod(lambda);

        try {
            method.setAccessible(true);
            Object replacement = method.invoke(lambda);

            if (replacement instanceof SerializedLambda) {
                return (SerializedLambda) replacement;
            } else {
                return null;// custom interface implementation
            }
        } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
            throw new RuntimeException(e);
        }

    }

    private static Method getWriteReplaceMethod(Serializable lambda) {
        for (Class<?> cl = lambda.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                return cl.getDeclaredMethod("writeReplace");
            } catch (NoSuchMethodException e) {
            }
        }
        throw new RuntimeException("writeReplace method not found");
    }

}
