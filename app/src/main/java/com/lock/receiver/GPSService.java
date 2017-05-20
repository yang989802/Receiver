package com.lock.receiver;

/**
 * Created by 奕旸 on 2017/4/10.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSService extends Service {
    private LocationManager lm;
    private LocationListener listener;
    private SharedPreferences sp;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    // 服务创建
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("进入了Gps","1");
        sp=getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
// 获取位置管理器
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new MyLocationListener();
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(criteria, true);
        lm.requestLocationUpdates(provider, 0, 0, listener);
    }
    // 服务销毁
    @Override
    public void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(listener);
        listener=null;
    }
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
// 获取经度
            String longitude = "longitude：" + location.getLongitude();
            String latitude = "latitude：" + location.getLatitude();
            String acc = "accuracy：" + location.getAccuracy();
            Log.i("进入了Gps","2");
//保存数据
            Editor editor=sp.edit();
            editor.putString("lastlocation", longitude+latitude+acc);
            editor.commit();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}