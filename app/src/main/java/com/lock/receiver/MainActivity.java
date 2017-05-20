package com.lock.receiver;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import static android.R.attr.onClick;


public class MainActivity extends AppCompatActivity  implements LocationListener {
    public static final Long MIN_TIME_LOCATION_UPDATE = 2000L; // 2 seg
    public  static final Float MIN_DISTANCE_LOCATION_UPDATE = 0f;
    public static final String TAG = MainActivity.class.getSimpleName();
    private Button setPhoneBtn;
    private Button lockBtn;
    static String locationStr;
    private LocationManager locationManager;
    private Location currentLocation;
    private Button btnRegistrar;
    private TextView tvUbicacion;
    private float light;
    private Button setPassword;

    // USB设备连接



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //申请权限
        ComponentName componentName = new ComponentName(this, reStartService.class);
        //判断该组件是否有系统管理员的权限
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);

        if(!isAdminActive){

            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给哪个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivity(intent);
        }



        //test
        btnRegistrar = (Button) findViewById(R.id.btn_registrar);
        tvUbicacion = (TextView) findViewById(R.id.tv_ubicacion);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        HandlerThread handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        // 锁屏状态设定
        //设置Lock状态
        SharedPreferences sharedPreferences=getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences("lockSTATE",MODE_PRIVATE).edit();


        editor.putBoolean("lockState",true);//true home 不可用

        editor.commit();
        //按键功能

        lockBtn=(Button)findViewById(R.id.lock_btn);
        setPhoneBtn=(Button)findViewById(R.id.phone_num);
        setPassword =(Button)findViewById(R.id.set_password);

        setPassword.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setClass(MainActivity.this,SetPassword.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation == null) {
                    tvUbicacion.setText("Todavía no se logro obtener la ubicación");
                } else {
                    tvUbicacion.setText("Lat: " + currentLocation.getLatitude() + ", Lng: " + currentLocation.getLongitude());
                    SharedPreferences prefer = getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);

                    Toast.makeText(MainActivity.this,prefer.getString("locationStr","123"), Toast.LENGTH_SHORT).show();
                }
            }
        });


        setPhoneBtn.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {
                EditText phoneNumText = (EditText)findViewById(R.id.Number);
                String number = phoneNumText.getText().toString();
                Log.i("获取到的紧急电话",number);
                SharedPreferences.Editor editor = getSharedPreferences("lockSTATE",MODE_PRIVATE).edit();
                editor.putString("phoneNum",number);

                editor.commit();
            }
        });


        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"5秒后进入锁屏界面", Toast.LENGTH_SHORT).show();
                Log.e("lockBtn","启动lockActivity");

                new Thread() {
                                     public void run() {
                                                          try {
                                                                  Thread.sleep(5000);
                                                                } catch (InterruptedException e) {
                                                                    // TODO Auto-generated catch block
                                                                          e.printStackTrace();
                                                                 }

                                         Intent intent =new Intent();
                                         intent.setClass(MainActivity.this,LockActivity.class);
                                         startActivity(intent);
                                                          }
                     }.start();

            }
        });
    }
    public String getLocationStr(){

        return "Lat: " + currentLocation.getLatitude() + ", Lng: " + currentLocation.getLongitude();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_LOCATION_UPDATE,
                    MIN_DISTANCE_LOCATION_UPDATE,
                    this
            );
            Toast.makeText(this, "GPS activado!", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "No se han otorgrado los permisos para localización", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Toast.makeText(this, "No se han otorgrado los permisos para localización", Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // Do nothing
    }

    @Override
    public void onProviderEnabled(String s) {
        // Do nothing
    }

    @Override
    public void onProviderDisabled(String s) {
        // Do nothing
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_BACK){
//            Toast.makeText(MainActivity.this, "按下了back键", Toast.LENGTH_SHORT).show();
//
//        }
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                return true;
//
//            case KeyEvent.KEYCODE_MENU:
//                return false;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}
