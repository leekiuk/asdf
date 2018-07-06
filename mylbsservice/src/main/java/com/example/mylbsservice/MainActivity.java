package com.example.mylbsservice;

import android.Manifest;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    //위치 관리자
    private LocationManager locationManager;
    private TextView tvInfo;
    private LocationListener locationListener;
    private Button      btnLocation;
    private TextView    tvMyLocation;
    //정확도, 속도, 고도를 위한 변수 선언
    private TextView tvAccuracy, tvSpeed, tvAltitude;
    //구글맵 객체 변수 선언
    private GoogleMap googleMap;
    private boolean locationTag = true;
    //브로드캐스트 리시버의 인스턴스 정의
    private LocationReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //권한 요청
        checkPermission();

        //객체 얻기
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        tvInfo = findViewById(R.id.tv_info);
        btnLocation = findViewById(R.id.btn_location);
        tvMyLocation = findViewById(R.id.tv_my_location);
        //tvAccuracy = findViewById(R.id.tv_accuracy);
        //tvSpeed = findViewById(R.id.tv_speed);
        //tvAltitude = findViewById(R.id.tv_altitude);

        //제공자 목록을 구해서 화면에 출력하기
        //인자값의 의미: 현재 사용 가능한 프로바이더만 리턴할껀지 여부인데 false로 하면 일단 사용못해도
        //기기에 탑재되어 있으면 리턴받음
        List<String> providerList = locationManager.getProviders(true);

        String result = "";
        for(String provider : providerList) {
            result += "Provider: " + provider + "\n";
        }

        //NO_REQUIREMENT: 상관없다.
        //POWER_LOW: 배터리 조금 사용하는걸 선호함.
        //ACCURACY_COARSE: 대충의 정밀도 요구
        //ACCURACY_FINE: 정밀한 정밀도 요구

        //이용 가능한 최적의 프로바이더를 선택해보자
        //Criteria: 조건 객체
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);                //고도
        criteria.setCostAllowed(false);                     //비용

        String best = locationManager.getBestProvider(criteria, true);
        result += "\n\n";
        result += "best provider: " + best;
        result += "\n\n";

        //GPS와 네트워크 (기지국) 사용 가능성 조사
        result += LocationManager.GPS_PROVIDER + " : " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        result += "\n";
        result += LocationManager.NETWORK_PROVIDER + " : " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        result += "\n";

        //화면에 출력하기
        tvInfo.setText(result);

        //위치 구하기
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("provider>>>", "onLocationChanged");

                if(checkReady()) {  //구글맵 로딩에 성공하면 실행해라.
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)))
                            .setTitle("Marker");
                    if(locationTag) {
                        LatLng loadPoint = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loadPoint, 15));
                    }
                }


                tvMyLocation.setText("위도: " + location.getLatitude() + "   경도: " + location.getLongitude() );

                //속도, 정확도, 고도 정보 표시하기
                //tvAccuracy.setText(location.getAccuracy() + "");                //암시적인 형변환
                //tvSpeed.setText(Double.toString(location.getSpeed()));          //명시적인 형변환
                //tvAltitude.setText(Double.toString(location.getAltitude()));
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("provider>>>", "onStatusChanged");
            }
            @Override
            public void onProviderEnabled(String s) {
                Log.d("provider>>>", "enabled");
            }
            @Override
            public void onProviderDisabled(String s) {
                Log.d("provider>>>", "disabled");
            }
        };

        //내위치 얻기 버튼에 이벤트 장착
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER
                            , 2000
                            , 1
                            , locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                            , 2000
                            , 1
                            , locationListener);
                    Toast.makeText(getApplicationContext(), "위치 정보 얻기 성공", Toast.LENGTH_SHORT).show();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "위치 정보 얻기 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //리시버를 객체화
        //브로드 캐스트 리시버가 메시지를 받을 수 있도록 설정
        receiver = new LocationReceiver();
        IntentFilter filter = new IntentFilter("com.example.mylbsservice.LocationReceiver");
        registerReceiver(receiver, filter);

        Intent intent = new Intent("com.example.mylbsservice.LocationReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        try {
            locationManager.addProximityAlert(37.5001133, 127.0356275, 10000, 1000, pendingIntent);

        } catch (SecurityException ex) {
            ex.printStackTrace();
        }



    }



    //권한 검사를 위한 메소드 추가
    private void checkPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(String permission : permissions) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if(permissionCheck == PackageManager.PERMISSION_DENIED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                //사용자가 권한을 수동으로 취소한 경우니까
                //필요하다면 권한에 대한 설명이 다시 나오면 좋음
                ActivityCompat.requestPermissions(this, permissions, 1);
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    //sdk23 미만에서는 무시되는 코드
    //권한이 없다면 요청을 하는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한 동의 버튼을 클릭했을 때
                    Toast.makeText(this, "권한 사용을 승인하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "권한 사용을 승인하셔야 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    //구글맵 지도가 레디가 되면 호출되는 메소드
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    //맵 연결 체크
    private boolean checkReady() {
        if(googleMap == null) {
            Toast.makeText(this, "구글 맵 로딩 실패!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }








}
