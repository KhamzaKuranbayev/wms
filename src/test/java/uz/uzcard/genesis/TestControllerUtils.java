/*
package uz.uzcard.genesis;

import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import uz.uzcard.genesis.config.ApplicationContextProvider;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.uitls.ServerUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TestControllerUtils {


    public static void change(Class<?> clazz, String methodName) {
        Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).findFirst().ifPresent(method -> {
            if (method.getAnnotation(ApiOperation.class) == null)
                return;

            Class<?>[] params = method.getParameterTypes();
            Object[] paramItems = Arrays.stream(params).map(aClass -> {
                try {
                    if ("MultipartFile".equals(aClass.getSimpleName())) {
                        return FileUtilsTest.getMultipartFile("D:\\test.png");
                    } else {
                        Object clz = getRandom(aClass);
                        return clz;
                    }
                } catch (Exception e) {
                    System.out.println("Method:" + methodName);
                    System.out.println("FILE_NOT_FOUND");
                    //                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList()).toArray();

            try {
                method.invoke(ApplicationContextProvider.applicationContext.getBean(clazz), paramItems);
            } catch (RpcException e) {
                System.out.println(e.getCause().getMessage());
            } catch (ValidatorException e) {
                System.out.println(e.getCause().getMessage());
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof ValidatorException || e.getTargetException() instanceof RpcException) {
                    System.out.println(e.getCause().getMessage());
                } else
                    e.getCause().printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Object getRandom(Class clazz) throws Exception {
        switch (clazz.getSimpleName()) {
            case "Long":
            case "long": {
                return randomLong();
            }
            case "double":
            case "Double": {
                return randomDouble();
            }
            case "String": {
                return randomString();
            }
            case "int":
            case "Integer": {
                return randomInteger();
            }
            case "Boolean":
            case "boolean": {
                return ThreadLocalRandom.current().nextBoolean();
            }
            case "Date":
                return new Date();
            default: {
                if (clazz.isEnum())
                    return getEnum(clazz);
                else
                    return create(clazz);
            }
        }
    }

    private static Object create(Class<?> clazz) throws Exception {
        Object object = null;
        if (clazz.getConstructors().length > 0) {
            Constructor<?> constructor = clazz.getConstructors()[ThreadLocalRandom.current().nextInt(0, clazz.getConstructors().length)];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] paramObjects = Arrays.stream(paramTypes).map(type -> {
                try {
                    return getRandom(type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).toArray();
            if (paramTypes.length > 0)
                object = constructor.newInstance(paramObjects);
            else
                object = newInstance(clazz, object);
        } else {
            object = newInstance(clazz, object);
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            field.set(object, getRandom(field.getType()));
        }
        return object;
    }

    private static Object newInstance(Class<?> clazz, Object object) throws InstantiationException, IllegalAccessException {
        if (Collection.class.isAssignableFrom(clazz)) {
            object = new ArrayList<>();
        } else if (Map.class.isAssignableFrom(clazz)) {
            object = new HashMap<>();
        } else if (HttpServletResponse.class.equals(clazz) || HttpServletRequest.class.equals(clazz)) {
        } else
            object = clazz.newInstance();
        return object;
    }

    private static String randomString() {
        return ServerUtils.generateUniqueCode();
    }

    private static Long randomLong() {
        return ThreadLocalRandom.current().nextLong(0, 1000);
    }

    private static Double randomDouble() {
        return ThreadLocalRandom.current().nextDouble(0, 1000);
    }

    private static Integer randomInteger() {
        return ThreadLocalRandom.current().nextInt(0, 1000);
    }

    @SneakyThrows
    private static Object getEnum(Class<?> clazz) {
        Object object = null;
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            Class t = f.getType();
            object = f.get(clazz);
            if (!t.isPrimitive() && object != null) {
                return object;
            }
        }
        return object;
    }
}
*/
