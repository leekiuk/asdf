package com.example.koreagooglemap;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private TextView mLongitude, mLatitude;
    private SeekBar mZoomBar;
    private int zoomLevel = 12;
    private CheckBox mSatellite;    //위성 전환 버튼
    //위도와 경도를 입력받는 객체 선언
    private EditText    etLongitude, etLatitude;
    private Button      btnMove;
    //키보드 숨기기
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //객체얻기
        mLongitude = findViewById(R.id.tv_longitude);
        mLatitude = findViewById(R.id.tv_latitude);
        mZoomBar = findViewById(R.id.zoombar);
        mSatellite = findViewById(R.id.check_map_type);
        etLongitude = findViewById(R.id.et_longtitude);
        etLatitude = findViewById(R.id.et_latitude);
        btnMove = findViewById(R.id.btn_move);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        //btnMove 이벤트 등록
        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveMap(etLatitude.getText().toString(), etLongitude.getText().toString());
                imm.hideSoftInputFromWindow(etLongitude.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etLatitude.getWindowToken(), 0);
            }
        });



        //seebar의 기본값 설정
        mZoomBar.setProgress(zoomLevel);

        //체크박스에 위성 타입 변환 이벤트 등록
        mSatellite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //확인용 Toast
                Toast.makeText(getApplicationContext(), "check클릭>>" + isChecked, Toast.LENGTH_SHORT).show();
                if(isChecked) {     //isChecked가 참이면 위성형태로
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {            //isChecked가 거짓이면 일반 지도형태로
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });



        //seekbar 이벤트 등록
        mZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setZoomLevel(progress + 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(final GoogleMap gmap) {
        googleMap = gmap;
        //시작위치를 설정하기 - 구글맵에서 검색해서 시작위치의 위도와 경도를 설정
        LatLng cityHall = new LatLng(37.566652, 126.978145);
        //설정한 위도와 경도로 화면을 이동
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cityHall));
        //줌 레벨 설정
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel));


        //마커 생성하기
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(cityHall);           //마커 생성 위치
        markerOptions.title("시청");                //마커의 제목
        markerOptions.snippet("서울의 시청");       //마커 클릭 시 나오는 조그마한 설명
        googleMap.addMarker(markerOptions);         //설정한 마커를 지도에 추가하기

        //서울 광장 마커 추가하기
        LatLng plaza = new LatLng(37.565580, 126.977927);
        markerOptions.position(plaza);
        markerOptions.title("광장");
        markerOptions.snippet("서울 광장");
        googleMap.addMarker(markerOptions);

        //맵이 로드가 완료되면 실행되는 메소드
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d("set>>>", "on Load 완료");
                //맵의 대한 모든 정보는 GoogleMap 객체가 담고 있다.
                //현재 맵의 위치
                Log.d("set>>>", "현재 맵의 위치: " + googleMap.getCameraPosition());
                drawMarkerWithRectangle(googleMap.getCameraPosition().target);
            }
        });

        //맵 이동이 끝날때 호출되는 이벤트
        //카메라 이동이 종료되었을 경우에 호출되는 이벤트
        //idle: 게으른, 할일없는
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                setPosition();
            }
        });

        //맵 이동 시작하는 메소드
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                Log.d("set>>>", "started 실행");
            }
        });
        //맵 이동 중 실행되는 메소드
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                Log.d("set>>>", "Move 실행");

            }
        });

        //지도를 클릭하면 맵을 이동
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                //기존 지도 정리
                googleMap.clear();

                //신규 마커추가
                //MarkerOptions newMarker = new MarkerOptions();
                //newMarker.position(latLng);
                //googleMap.addMarker(newMarker);

                //원 그리기 메소드 호출
                drawMarkerWithCircle(latLng);
            }
        });

        //지도 롱클릭하면 마커를 생성하기
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                drawMarkerWithCircle(latLng);
            }
        });

        //지도에서 마커를 클릭하면 삭제
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //마커를 지워라
                //marker.remove();
                //마커숨기기
                marker.setVisible(false);
                return false;
            }
        });
    }

    //위도와 경도를 입력받아서 지도를 이동하는 메소드
    public void moveMap(String lat, String lng) {

        //에러 방지를 위한 디폴트 값 설정
        double latitude     = 0d;
        double longitude    = 0d;

        //입력값이 없이 버튼을 클릭하면 디폴트값을 서울 시청으로 맞춰줌
        if("".equals(lat) || "".equals(lng)) {
            latitude = 37.565580;
            longitude = 126.977927;
        } else {
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
        }

        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    //맵의 중앙좌표를 위도와 경도로 표시하는 메소드
    private void setPosition() {
        mLatitude.setText("위도: " + googleMap.getCameraPosition().target.latitude);
        mLongitude.setText("경도: " + googleMap.getCameraPosition().target.longitude);
    }

    //seekbar를 활용한 맵의 줌레벨 변경 메소드
    private void setZoomLevel(int level) {
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(level));
        Log.d("zoom>>>", level + "");
    }

    //원 그리기
    private void drawMarkerWithCircle(LatLng center) {
        double radiusInMeters   = 100.0;
        int strokeColor         = 0xffff0000;
        int shadeColor          = 0x44ff0000;

        //원 그리기 설정
        CircleOptions circleOptions
                = new CircleOptions()
                .center(center)
                .radius(radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);

        //지도에 원 형태 그리기
        googleMap.addCircle(circleOptions);

        //마커추가
        MarkerOptions markerOptions = new MarkerOptions().position(center);
        googleMap.addMarker(markerOptions);
    }

    //사각형 그리기
    private void drawMarkerWithRectangle(LatLng center) {
        //그리기 설정
        PolygonOptions options = new PolygonOptions();
        //가운데 좌료를 기준으로 상하좌우의 크기를 0.005 만큼 만들어라.
        options.addAll(createRectangle(center, 0.005, 0.005));
        //색채우기
        options.fillColor(0x44ff0000);
        //선 색
        options.strokeColor(Color.RED);
        //선 굵기
        options.strokeWidth(5);
        //채우기 없애기
        options.addHole(createRectangle(center, 0.004, 0.004));
        googleMap.addPolygon(options);
    }


    //사각형 계산식
    private List<LatLng> createRectangle(LatLng center, Double halfWidth, Double halfHeight) {
        return Arrays.asList(
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth)
        );
    }






}





