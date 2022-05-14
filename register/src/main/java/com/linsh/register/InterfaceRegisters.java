package com.linsh.register;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2022/05/12
 *    desc   : 接口注册器
 *
 *             使用注解 {@link InterfaceRegister} 对接口进行注册，使用 {@link InterfaceRegisters#findRegisters(Class)}
 *             方法获取注册的实现类
 * </pre>
 */
public class InterfaceRegisters {

    private static final Map<Class<?>, Class<?>[]> REGISTER_MAP = new java.util.HashMap<>();

    static {
        char postFixIndex = 'A';
        for (Class<?> registerClazz; (registerClazz = hasNextRegister(postFixIndex)) != null; postFixIndex++) {
            try {
                System.out.println("InterfaceRegisters: init for " + registerClazz.getName());
                Method method = registerClazz.getMethod("init");
                method.invoke(null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 查找下一个子模块注册器
     * <p>
     * 为解决子模块类冲突问题，注册器动态注册使用了分模块不同注册器的方式，并使用 A - Z 排序的方法定义注册器。
     *
     * @param postFixIndex 注册器后缀索引
     */
    private static Class<?> hasNextRegister(char postFixIndex) {
        try {
            return Class.forName("com.linsh.register._InterfaceRegisters" + postFixIndex);
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 手动注册接口
     *
     * @param interfaceClazz 接口类
     * @param registers      实现类
     */
    @SafeVarargs
    public static <T> void register(@NonNull Class<T> interfaceClazz, @NonNull Class<? extends T>... registers) {
        Class<?>[] newList;
        Class<?>[] registeredList = REGISTER_MAP.get(interfaceClazz);
        if (registeredList == null) {
            newList = new Class[registers.length];
            System.arraycopy(registers, 0, newList, 0, registers.length);
        } else {
            newList = new Class[registeredList.length + registers.length];
            System.arraycopy(registeredList, 0, newList, 0, registeredList.length);
            System.arraycopy(registers, 0, newList, registeredList.length, registers.length);
        }
        REGISTER_MAP.put(interfaceClazz, newList);
    }

    /**
     * 获取所有注册指定接口的实现类
     * <p>
     * 注：获取结果仅针对使用 {@link InterfaceRegister} 或 {@link InterfaceRegisters#register(Class, Class[])}
     * 注册过的实现类。
     *
     * @param interfaceClazz 接口类
     * @return 注册了该接口的实现类
     */
    @NonNull
    public static <T> List<Class<? extends T>> findRegisters(@NonNull Class<T> interfaceClazz) {
        Class<?>[] registers = REGISTER_MAP.get(interfaceClazz);
        if (registers == null) return Collections.emptyList();
        return Arrays.asList((Class<? extends T>[]) registers);
    }
}
