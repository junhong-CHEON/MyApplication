package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
    LocationManager locationManager;
    double latitude;
    double longitude;
    TextView latText, lonText, weather;
    Button button;
//test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        map();
        initView();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    private void map() {
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        TMapView tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("api key");
        linearLayoutTmap.addView(tMapView);

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                //Toast.makeText(getApplicationContext(),"lon=" + tMapPoint.getLongitude() + "\nlat=" + tMapPoint.getLatitude(), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Toast.makeText(getApplicationContext(),"lon=" + tMapPoint.getLongitude() + "\nlat=" + tMapPoint.getLatitude(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint) {
                Toast.makeText(getApplicationContext(), "onLongPress~!", Toast.LENGTH_SHORT).show();
            }
        });

// 지도 스크롤 종료
        tMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                //Toast.makeText(getApplicationContext(), "zoomLevel=" + zoom + "\nlon=" + centerPoint.getLongitude() + "\nlat=" + centerPoint.getLatitude(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        //뷰세팅
        latText = (TextView) findViewById(R.id.latitude);
        lonText = (TextView) findViewById(R.id.longitude);
        weather = (TextView) findViewById(R.id.weather);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        /*현재 위치에서 위도경도 값을 받아온뒤 우리는 지속해서 위도 경도를 읽어올것이 아니니
        날씨 api에 위도경도 값을 넘겨주고 위치 정보 모니터링을 제거한다.*/
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        //위도 경도 텍스트뷰에 보여주기
        latText.setText(String.valueOf(latitude));
        lonText.setText(String.valueOf(longitude));
        //날씨 가져오기 통신
        getWeather(latitude, longitude);
        //위치정보 모니터링 제거
        locationManager.removeUpdates(MainActivity.this);
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
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //버튼 클릭시 현재위치의 날씨를 가져온다
            case R.id.button:
                if (locationManager != null) {
                    requestLocation();
                }

                break;
        }
    }



    private void requestLocation() {
        //사용자로 부터 위치정보 권한체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, this);

        }
    }



    private interface ApiService {
        //베이스 Url
        String BASEURL = "https://api2.sktelecom.com/";
        String APPKEY = "api key";

        //get 메소드를 통한 http rest api 통신
        @GET("weather/current/hourly")
        Call<JsonObject> getHourly(@Header("appkey") String appKey, @Query("version") int version,
                                   @Query("lat") double lat, @Query("lon") double lon);
        @GET("tmap/traffic")
        Call<JsonObject> getTraffic(@Header("appkey") String appKey, @Query("version") int version,
                                    @Query("minLat") double minlat, @Query("minLon") double minlon,
                                    @Query("maxLay") double maxlat, @Query("maxLon") double maxlon,
                                    @Query("centerLat") double centerlat, @Query("centerLon") double centerlon,
                                    @Query("trafficType") String type, @Query("radius") int n);

    }

    private void getTraffic(double centerlat, double centerlon){
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ApiService.BASEURL)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        //Call<JsonObject> call = apiService.getTraffic(ApiService.APPKEY,1,)
    }

    private void getWeather(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ApiService.BASEURL)
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<JsonObject> call = apiService.getHourly(ApiService.APPKEY, 1, latitude, longitude);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    //날씨데이터를 받아옴
                    JsonObject object = response.body();
                    if (object != null) {
                        //데이터가 null 이 아니라면 날씨 데이터를 텍스트뷰로 보여주기
                        weather.setText(object.toString());
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }
}
