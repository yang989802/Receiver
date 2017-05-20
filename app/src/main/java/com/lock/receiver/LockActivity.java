package com.lock.receiver;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lock.receiver.view.view.GestureLockViewGroup;
import com.lock.receiver.view.view.GestureLockViewGroup.OnGestureLockViewListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import static com.lock.receiver.R.layout.lock_screen;

/**
 * Created by 奕旸 on 2017/3/21.
 */

public class LockActivity extends Activity {
    private Button exitBtn;
    private GestureLockViewGroup mGestureLockViewGroup;
    private EditText nameText;
    private float light;

    //光感监听
    private SensorManager sm;
    private MyListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_screen);



        //start 光感
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        //光线传感器
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        listener = new MyListener();
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
        //end

        //启动锁屏

        SharedPreferences pref = LockActivity.this.getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
        boolean locked = pref.getBoolean("lockState",false);
        String answerStr = pref.getString("answer","1,2,3");
        if(!answerStr.equals("1,2,3"))
        answerStr =answerStr.substring(0,answerStr.length()-1);
        Log.i("answerStr   ",answerStr);
        String [] str = answerStr.split(",");
        Log.i("str   ",str.length+" "+str[0]+" "+str[1]);
        int answer[] =new int [str.length];
        for(int i=0;i<str.length;i++){
            answer[i]=Integer.parseInt(str[i]);
            Log.i("answer"+i+"为",answer[i]+"");
        }


        SharedPreferences.Editor editor = pref.edit();
        //第二个参数为默认值
        Log.i("1.获取的锁屏状态为",locked+"");
//光感抽样
        light = listener.getLight();
        Log.i("super光感大小Light",light+"");
        Toast.makeText(LockActivity.this,light+"  光感大小", Toast.LENGTH_SHORT).show();
        editor = getSharedPreferences("lockSTATE",MODE_PRIVATE).edit();
        editor.putFloat("light",light);

        editor.commit();
//        if(!locked){
//            finish();
//
//        }
        //失主电话显示
        String phoneNum= pref.getString("phoneNum","未设置");
        Log.i("获取到的失主电话",phoneNum);
        TextView lostPhone=(TextView)findViewById(R.id.lostPhone);
        lostPhone.setText("失主电话为:"+phoneNum+"点击拨打");
        //拨打电话
        lostPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = LockActivity.this.getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
                String phoneNum= pref.getString("phoneNum","未设置");
                Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phoneNum));
                startActivity(intent);
            }
        });
        //解锁操作

        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.id_gestureLockViewGroup);
        mGestureLockViewGroup.setAnswer(answer);
        mGestureLockViewGroup
                .setOnGestureLockViewListener(new OnGestureLockViewListener()
                {

                    @Override
                    public void onUnmatchedExceedBoundary()
                    {
                        Toast.makeText(LockActivity.this, "错误5次...",
                                Toast.LENGTH_SHORT).show();
                        mGestureLockViewGroup.setUnMatchExceedBoundary(5);
                        mGestureLockViewGroup.setUnMatchExceedBoundary(5);
                        //开启闹铃
                        Intent intentSV = new Intent(LockActivity.this, MusicService.class);
                        startService(intentSV);

//                                          new Thread() {
//                 public void run() {
//                                                          try {
//                                                                  Thread.sleep(3000);
//                                                                } catch (InterruptedException e) {
//                                                                    // TODO Auto-generated catch block
//                                                                          e.printStackTrace();
//                                                                 }
//                                                              startAlarm();
//                                                             mGestureLockViewGroup.setUnMatchExceedBoundary(5);
//                                                          }
//                     }.start();
                    }


                    @Override
                    public void onGestureEvent(boolean matched)
                    {
                        SharedPreferences pref = LockActivity.this.getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
                        boolean locked = pref.getBoolean("lockState",false);

                        if(matched){
                            setLock();
                            //关闭闹铃声
                            Intent intentSV = new Intent(LockActivity.this, MusicService.class);
                            stopService(intentSV);

                            finish();

                        }else{
                            Toast.makeText(LockActivity.this,"false" ,
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onBlockSelected(int cId)
                    {
                    }
                });

        //查看进入该activity的方式
        List<String> pkgNamesT = new ArrayList<String>();
        List<String> actNamesT = new ArrayList<String>();
        Intent intent = getIntent();
        Context context =getApplicationContext();
        List<ResolveInfo>resolveInfos=context.getPackageManager().queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        Log.i("resolveInfos="," "+resolveInfos.size());
        for (int i = 0; i < resolveInfos.size(); i++) {
            String string = resolveInfos.get(i).activityInfo.packageName;
            Log.i("获取的STRING名字=",string);
            if (!string.equals(context.getPackageName())) {//自己的launcher不要
                Log.i("获取的launcher", resolveInfos.get(i).activityInfo.packageName);
                pkgNamesT.add(string);
                string = resolveInfos.get(i).activityInfo.name;
                actNamesT.add(string);
            }
        }
        Log.e("启动的方法",getIntent().getCategories()+"");
        //android.intent.category.LAUNCHER：点击图标启动
        //android.intent.category.HOME：点击Home启动
//        Set catg = getIntent().getCategories();
//        if (catg!=null){
//            Log.e("catg=",catg+"");
//            finish();
//        }




        ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        name.equals(LockActivity.class.getName());
        Log.e("  true在栈顶false不在栈顶",name.equals(LockActivity.class.getName())+"");



     exitBtn = (Button) findViewById(R.id.exit);
     exitBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             // 设备安全管理服务    2.2之前的版本是没有对外暴露的 只能通过反射技术获取
             DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

             // 申请权限
             ComponentName componentName = new ComponentName(LockActivity.this, MyAdmin.class);
             // 判断该组件是否有系统管理员的权限
             boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
             if(isAdminActive){

                 devicePolicyManager.lockNow(); // 锁屏

      //           devicePolicyManager.resetPassword("123", 0); // 设置锁屏密码
//            devicePolicyManager.wipeData(0);  恢复出厂设置  (建议大家不要在真机上测试) 模拟器不支持该操作

             } else {
                 Intent intent = new Intent();
                 // 指定动作名称
                 intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                 // 指定给哪个组件授权
                 intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                 startActivity(intent);
             }
         }
     });


        }

    void setLock(){
        SharedPreferences pref = LockActivity.this.getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("lockState",false);

        editor.commit();
        boolean locked = pref.getBoolean("lockState",false);
        Toast.makeText(LockActivity.this,"lockedstate="+locked+"" ,
                Toast.LENGTH_SHORT).show();
        Log.i("获取的锁屏状态为",locked+"");
    }

    //光感监听
        private class MyListener implements SensorEventListener {
        int flag = 0;
        float firstLight=0;
        float light;
            @Override
            public void onSensorChanged(SensorEvent event) {
              light = event.values[0];
                if(flag==0)
                {
                    firstLight = event.values[0];
                    flag=1;

                }else if(Math.abs(light-firstLight)>50){

                    //开启闹铃
                    Intent intentSV = new Intent(LockActivity.this, MusicService.class);
                    startService(intentSV);

                }
                Log.i("光感大小Light",light+"");
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
            public float getLight(){
                return light;

            }
    }
    @Override
    protected void onDestroy() {
        sm.unregisterListener(listener);
        listener = null;
        super.onDestroy();
        //防止退出程序时继续
    }
    //响铃


}

