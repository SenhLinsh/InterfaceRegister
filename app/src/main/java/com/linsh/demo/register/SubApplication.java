package com.linsh.demo.register;

import android.app.Application;
import android.util.Log;

import com.linsh.register.InterfaceRegister;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2022/05/10
 *    desc   :
 * </pre>
 */
@InterfaceRegister(IApplication.class)
public class SubApplication implements IApplication {

    private static final String TAG = "SubApplication";

    @Override
    public void onCreate(Application application) {
        Log.i(TAG, "init in SubApplication");
    }
}
