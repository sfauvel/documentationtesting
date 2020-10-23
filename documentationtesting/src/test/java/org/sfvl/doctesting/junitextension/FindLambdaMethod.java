package org.sfvl.doctesting.junitextension;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FindLambdaMethod {

    public @FunctionalInterface
    static interface SerializableConsumer<T> extends Consumer<T>, Serializable {
    }

    public @FunctionalInterface
    static interface SerializableBiConsumer<T, V> extends BiConsumer<T, V>, Serializable {
    }

    public @FunctionalInterface
    static interface SerializableSupplier<T, V> extends Supplier<T>, Serializable {
    }

    public @FunctionalInterface
    static interface SerializableFunction<T, V> extends Function<T, V>, Serializable {
    }

    public static <T1> String getName(SerializableConsumer<T1> consumer) {
        return createMethodNameFromSuperConsumer(consumer);
    }

    public static <T1, V1> String getName(SerializableBiConsumer<T1, V1> consumer) {
        return createMethodNameFromSuperConsumer(consumer);
    }

    public static <T1, V1> String getName( SerializableFunction<T1, V1> consumer) {
        return createMethodNameFromSuperConsumer(consumer);
    }

    private static String createMethodNameFromSuperConsumer(Serializable lambda) {
        SerializedLambda serializedLambda = getSerializedLambda(lambda);
        return serializedLambda.getImplMethodName();
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
