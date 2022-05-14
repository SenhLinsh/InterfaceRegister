package com.linsh.demo.register;

import android.app.Application;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2022/05/10
 *    desc   : 应用入口接口声明
 *
 *             可以依赖库、子模块等地方实现该接口，使用 {@link com.linsh.register.InterfaceRegister} 进行
 *             注册。
 *
 *             在真正的 Application 入口中使用 {@link com.linsh.register.InterfaceRegisters#findRegisters(Class)}
 *             进行间接获取，达到业务逻辑、模块划分等的解耦和隔离的作用。
 * </pre>
 */
public interface IApplication {

    void onCreate(Application application);
}
