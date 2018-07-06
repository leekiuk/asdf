package com.example.sjc.day11;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DigitalFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        //매개변수로 전달받은 LayoutInflater객체를 통해 xml화면(fragement_digital.xml) 레이아웃 파일을
        //View 객체로 생성함.
        view = inflater.inflate(R.layout.fragment_digital, null);
        return view;
    }
}
