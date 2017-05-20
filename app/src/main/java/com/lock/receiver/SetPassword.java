package com.lock.receiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.lock.receiver.view.view.GestureLockViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by 奕旸 on 2017/4/14.
 */

public class SetPassword extends Activity {
    private GestureLockViewGroup mGestureLockViewGroup;
    private List<Integer> mChoose1 = new ArrayList<Integer>();
    private List<Integer> mChoose2 = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_password);

        mGestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.id_gestureLockViewGroup);
        mGestureLockViewGroup
                .setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener()
                {

                    @Override
                    public void onGestureEvent(boolean matched) {
                        if (mChoose1.size() == 0) {
                            for(int i=0;i<mGestureLockViewGroup.getmChoose().size();i++){
                                mChoose1.add(mGestureLockViewGroup.getmChoose().get(i));
                            }

                            Log.i("mChoose1的值为",mChoose1.toString());
                            Toast.makeText(SetPassword.this, "请输入第二次密码", Toast.LENGTH_SHORT).show();
                        } else {
                            for(int i=0;i<mGestureLockViewGroup.getmChoose().size();i++){
                                mChoose2.add(mGestureLockViewGroup.getmChoose().get(i));
                            }
                            Log.i("mChoose2的值为",mChoose2.toString());
                            Log.i("check值为",check(mChoose1,mChoose2)+"" );
                                if(check(mChoose1,mChoose2)){
                                    String answer = "";
                                    for(int i=0;i<mChoose1.size();i++){
                                        answer= answer+mChoose1.get(i);
                                        answer+=",";
                                    }
                                    Log.i("密码为",answer);
                                    SharedPreferences.Editor editor = getSharedPreferences("lockSTATE",MODE_PRIVATE).edit();
                                    editor.putString("answer",answer);

                                    editor.commit();

                                    Toast.makeText(SetPassword.this, "设置完成", Toast.LENGTH_SHORT).show();
                                    Intent intent =new Intent();
                                    intent.setClass(SetPassword.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else{
                                    Toast.makeText(SetPassword.this, "两次密码不正确", Toast.LENGTH_SHORT).show();
                                    mChoose1.removeAll(mChoose1);
                                    mChoose2.removeAll(mChoose2);
                                    Log.i("清空手势",mChoose1.toString()+"  "+mChoose2.toString());
                                }
                        }
                    }

                    @Override
                    public void onUnmatchedExceedBoundary() {

                    }

                    @Override
                    public void onBlockSelected(int cId)
                    {



                    }
                });


    }
    public boolean check(List list1,List list2){
        Log.i("收到的List1 list2",list1.toString()+"  "+list2.toString());
        if (list1.size() != list2.size()){

            return false;
        }
        for (int i = 0; i < list1.size(); i++)
        {
            if (list1.get(i) != list2.get(i))
                return false;
        }
        return true;
    }

}
