package com.lock.receiver;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 奕旸 on 2017/4/16.
 */

public class reStartService extends Service {
    public void a(){
        // 申请权限
        ComponentName componentName = new ComponentName(this, reStartService.class);
        // 设备安全管理服务    2.2之前的版本是没有对外暴露的 只能通过反射技术获取
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 判断该组件是否有系统管理员的权限
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
        if(!isAdminActive) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "（自定义区域2）");

        } else {
            // 可以使用devicePolicyManager来操作锁屏设置密码等操作
            Log.i("开始调用a()","1");
            devicePolicyManager.lockNow(); // 锁屏

            //            devicePolicyManager.wipeData(0);  恢复出厂设置  (建议大家不要在真机上测试) 模拟器不支持该操作
        }

    }
    @Override
    public void onCreate() {
        Log.i("开始调用REservice","1");
        a();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
