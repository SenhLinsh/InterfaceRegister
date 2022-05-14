package com.linsh.demo.register;

import android.app.Application;
import android.util.Log;

import com.linsh.register.InterfaceRegisters;

import java.util.List;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2022/05/10
 *    desc   : 真正的 Application 入口
 * </pre>
 */
public class DemoApplication extends Application {

    private static final String TAG = "DemoApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "init in DemoApplication");
        // 以下部分初始化逻辑以及 IApplication 接口的定义可放在基础依赖库中，可以给其他依赖模块进行初始化，来达到
        // 动态注册的作用，用于解耦、隔离、组件化等场景。
        List<Class<? extends IApplication>> classes = InterfaceRegisters.findRegisters(IApplication.class);
        for (Class<? extends IApplication> clazz : classes) {
            try {
                IApplication instance = clazz.newInstance();
                Log.i(TAG, "init for " + clazz.getName());
                instance.onCreate(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
