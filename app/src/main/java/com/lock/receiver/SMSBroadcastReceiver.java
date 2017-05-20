package com.lock.receiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.content.Context.DEVICE_POLICY_SERVICE;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    double latitude;
    double longitude;
    String locationStr;
    String lastLocation;

    public void onReceive(Context context, Intent intent) {


        SmsMessage msg = null;
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            for (Object p : pdusObj) {
                msg = SmsMessage.createFromPdu((byte[]) p);

                String msgTxt = msg.getMessageBody();//得到消息的内容

                Date date = new Date(msg.getTimestampMillis());//时间
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String receiveTime = format.format(date);
                String senderNumber = msg.getOriginatingAddress();
                SharedPreferences prefer = context.getSharedPreferences("lockSTATE", Context.MODE_PRIVATE);
                String staticPhoneNum = prefer.getString("phoneNum", "未设置");
                locationStr = prefer.getString("locationStr", "未获取到gps");
                Log.i("获取GPS位置", locationStr);
                Log.i("获取默认手机号位", staticPhoneNum);
                Intent intent1=new Intent(context,GPSService.class);
                context.startService(intent1);
                lastLocation= prefer.getString("lastlocation", "321");
                Log.i("获取GPS位置", lastLocation);
                if (senderNumber.equals(staticPhoneNum)) {


                    Toast.makeText(context, "发送人：" + senderNumber + "  短信内容：" + msgTxt + "接受时间：" + receiveTime, Toast.LENGTH_LONG).show();
                    Log.i("发送人 短信内容 接受时间 ", senderNumber + " " + msgTxt + " " + receiveTime);
                    dealMsg(context, msgTxt, senderNumber);
                }

                return;

            }
            return;
        }
    }

    /**
     * 处理短信，验证是否为命令
     * @param context 上下文
     * @param senderNumber 手机号码
     * @param msgTxt 发送的内容
     * 收到的msgTXT
     *               1.关闭响铃
     *               2.格式化
     *               3.获取gps
     *               4.开旗响铃
     * 其余情况回问
     */

    public void dealMsg(Context context, String msgTxt, String senderNumber) {
        Intent service = new Intent(context, MusicService.class);
        Intent reStartService = new Intent(context, reStartService.class);
        switch (msgTxt) {
            case "1":// 关闭闹铃
                context.stopService(service);
                send(senderNumber, "关闭响铃");
                break;
            case "2"://格式化
                send(senderNumber, "格式化");
                context.startService(reStartService);
                break;
            case "3":
                Log.i("GPS地址为", lastLocation);
                send(senderNumber, lastLocation);
                //获取GPS并返回
                break;
            case "4":
                //开启响铃
                context.startService(service);
                send(senderNumber, "开启响铃");
                break;
            default:
                send(senderNumber, "请确认命令，请只输入数字，1.关闭响铃\n2.格式化\n3.获取gps\n 4.开启响铃");
        }

    }

    /**
     * 发送短信
     * @param num 手机号码
     * @param msgTxt 发送的内容
     *
     */
    public void send(String num, String msgTxt) {


        SmsManager smsManager = SmsManager.getDefault();
        //如果字数超过5,需拆分成多条短信发送
        if (msgTxt.length() > 5) {
            ArrayList<String> msgs = smsManager.divideMessage(msgTxt);
            for (String msg : msgs) {
                smsManager.sendTextMessage(num, null, msg, null, null);
            }
        } else {
            smsManager.sendTextMessage(num, null, msgTxt, null, null);
        }
    }

    public void getLocation() {
        Activity mActivity = null;
        LocationManager locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        latitude=location.getLatitude();
        longitude=location.getLongitude();

    }


}
