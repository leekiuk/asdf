package com.example.sjc.day11;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    private FragmentManager     manager;                //Fragment를 관리하는 클래스의 참조변수
    private FragmentTransaction fragmentTransaction;    //실제로 Fragment를 추가/삭제/재배치하는 클래스의 참조변수
    private Fragment            analogFragment, digitalFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객체 얻기

        //Fragment를 관리하는 Manager 객체 얻기
        manager = (FragmentManager)getFragmentManager();

        //Fragment 객체 얻기
        analogFragment  = new AnalogFragment();
        digitalFragment = new DigitalFragment();

    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.btn01:
                Toast.makeText(this, "Analog Btn 클릭", Toast.LENGTH_SHORT).show();
                fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.replace(R.id.container, analogFragment);
                //commit: fragment 변경 작업을 완료했으니 실행해라
                fragmentTransaction.commit();
                break;
            case R.id.btn02:
                Toast.makeText(this, "Digital Btn 클릭", Toast.LENGTH_SHORT).show();
                fragmentTransaction = manager.beginTransaction();
                fragmentTransaction.replace(R.id.container, digitalFragment);
                //commit: fragment 변경 작업을 완료했으니 실행해라
                fragmentTransaction.commit();
                break;
        }
    }







}
