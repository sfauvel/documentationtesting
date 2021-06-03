package fr.sfvl;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FindLambdaMethod {

    public @FunctionalInterface
    static interface SerializableConsumer<T> extends Consumer<T>, Serializable {
    }

    public @FunctionalInterface
    static interface SerializableBiConsumer<T, V> extends BiConsumer<T, V>, Serializable {
    }
//
//    public @FunctionalInterface
//    static interface SerializableSupplier<T, V> extends Supplier<T>, Serializable {
//    }
//
//    public @FunctionalInterface
//    static interface SerializableFunction<T, V> extends Function<T, V>, Serializable {
//    }
//
    public static <T1> String getName(SerializableConsumer<T1> consumer) {
        return createMethodNameFromSuperConsumer(consumer);
    }

    public static <T1, V1> String getName(SerializableBiConsumer<T1, V1> consumer) {
        return createMethodNameFromSuperConsumer(consumer);
    }
//
//    public static <T1, V1> String getName( SerializableFunction<T1, V1> consumer) {
//        return createMethodNameFromSuperConsumer(consumer);
//    }
//
//    public static <T1> String getClassName( SerializableConsumer<T1> consumer) {
//        SerializedLambda serializedLambda = getSerializedLambda(consumer);
//        return serializedLambda.getImplClass();
//    }
//
    public static <T1> Method getMethod( SerializableConsumer<T1> lambda) {
        SerializedLambda serializedLambda = getSerializedLambda(lambda);
        final String className = serializedLambda.getImplClass().replace("/", ".");
        final String methodName = serializedLambda.getImplMethodName();

        final Class<?> aClass;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            return aClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T1, V1> Method getMethod(SerializableBiConsumer<T1, V1> lambda) {
        SerializedLambda serializedLambda = getSerializedLambda(lambda);
        final String className = serializedLambda.getImplClass().replace("/", ".");
        final String methodName = serializedLambda.getImplMethodName();
        final String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
//        for (int i = 0; i < serializedLambda.getCapturedArgCount(); i++) {
//            final Object capturedArg = serializedLambda.getCapturedArg(i);
//            System.out.println(capturedArg);
//        }
        //(Lorg/sfvl/doctesting/SimpleClass;Ljava/lang/Integer;)V
        final Pattern compile = Pattern.compile("L.*?;");
        final Matcher matcher = compile.matcher(instantiatedMethodType);
        List<Class<?>> args = new ArrayList<>();
        while (matcher.find()) {
            final String group = matcher.group();
            final String substring = group.substring(1, group.length() - 1)
                    .replace("/", ".");
            try {
                if (Integer.class.getName().equals(substring)) {
                    args.add(int.class);
                } else {
                    args.add(Class.forName(substring));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        args.remove(0);

        final Class<?> aClass;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

//        try {
            final List<Method> methods = Arrays.stream(aClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .collect(Collectors.toList());

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
