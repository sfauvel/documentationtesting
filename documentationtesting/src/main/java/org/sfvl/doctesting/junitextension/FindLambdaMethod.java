package org.sfvl.doctesting.junitextension;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FindLambdaMethod {

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

        final Class<?> aClass = getClass(serializedLambda);

            for (Method method : methods) {
                boolean notMatch = false;
                final String s = method.toGenericString();
                final String s1 = method.toString();
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (args.size() == parameterTypes.length) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!args.get(i).equals(parameterTypes[i])) {
                            notMatch=true;
                        }
                    }
                } else {
                    notMatch = true;
                }
                if (!notMatch) {
                    return method;
                }
            }
//            return aClass.getMethod(methodName, args.toArray(new Class[0]));
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
        throw new RuntimeException("No method found");
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
