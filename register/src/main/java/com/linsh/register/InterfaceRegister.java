package com.linsh.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2021/05/18
 *    desc   : 接口注册器
 *             通过定义一些通用接口，使用接口注册器注册该接口，即可做到解耦式的动态接口注册
 *             之后再框架或需要使用指定接口实现的地方，获取已注册的接口即可。
 * </pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface InterfaceRegister {
    Class<?> value();
}