package com.lock.receiver;

import android.app.Application;

/**
 * Created by 奕旸 on 2017/4/5.
 */

public class MyApp extends Application {
    // 共享变量
    private MyHandler handler = null;

    // set方法
    public void setHandler(MyHandler handler) {
        this.handler = handler;
    }

    // get方法
    public MyHandler getHandler() {
        return handler;
    }
}
