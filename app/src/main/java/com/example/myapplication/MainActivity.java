package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Text;

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
    TextView weather, locate, traffic;
    Button button;
    String city = null;
    String county = null;
    String village = null;
    String sky = null;
    String temp = null;
//test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestLocation();
        initView();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    private void map() {
        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        final TMapView tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("app key");
        linearLayoutTmap.addView(tMapView);
        tMapView.setCenterPoint(longitude,latitude);

        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Toast.makeText(getApplicationContext(),"lon=" + tMapPoint.getLongitude() + "\nlat=" + tMapPoint.getLatitude(), Toast.LENGTH_SHORT).show();
                getWeather(tMapPoint.getLatitude(),tMapPoint.getLongitude());
                return false;
            }
        });

        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList arrayList, ArrayList arrayList1, TMapPoint tMapPoint) {
                Toast.makeText(getApplicationContext(), "onLongPress~!", Toast.LENGTH_SHORT).show();
                getTraffic(tMapPoint.getLatitude(),tMapPoint.getLongitude());

                TMapCircle tMapCircle = new TMapCircle();
                tMapCircle.setCenterPoint(tMapPoint);
                tMapCircle.setRadius(300);
                tMapCircle.setCircleWidth(2);
                tMapCircle.setLineColor(Color.MAGENTA);
                tMapCircle.setAreaColor(Color.GRAY);
                tMapCircle.setAreaAlpha(100);
                tMapView.addTMapCircle("circle",tMapCircle);

                TMapMarkerItem markerItem1 = new TMapMarkerItem();
                Bitmap bitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.marker);

                markerItem1.setIcon(bitmap);
                markerItem1.setPosition(0.5f,1.0f);
                markerItem1.setTMapPoint(tMapPoint);
                tMapView.addMarkerItem("markerItem1",markerItem1);

                tMapView.setCenterPoint(tMapPoint.getLongitude(),tMapPoint.getLatitude(),true);
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
        traffic=(TextView)findViewById(R.id.traffic);
        locate = (TextView) findViewById(R.id.locate);
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
        //날씨 가져오기 통신
        map();
        getWeather(latitude, longitude);
        getTraffic(latitude,longitude);
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
        String APPKEY = "app key";

        //get 메소드를 통한 http rest api 통신
        @GET("weather/current/hourly")
        Call<JsonObject> getHourly(@Header("appkey") String appKey, @Query("version") int version,
                                   @Query("lat") double lat, @Query("lon") double lon);

        @GET("tmap/traffic")
        Call<JsonObject> getTraffic(@Query("appkey") String appKey, @Query("version") int version,
                                    //@Query("minLat") double minlat, @Query("minLon") double minlon,
                                    //@Query("maxLay") double maxlat, @Query("maxLon") double maxlon,
                                    @Query("centerLat") double centerlat, @Query("centerLon") double centerlon,
                                    //@Query("reqCoordType") String reqtype, @Query("resCoordType") String restype,
                                    @Query("trafficType") String type, @Query("radius") int n,
                                    @Query("zoomLevel") int level/*,@Query("sort") String sort*/);
    }

    //교통정보
            private void getTraffic(double centerlat, double centerlon){
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(ApiService.BASEURL)
                        .build();
                ApiService apiService = retrofit.create(ApiService.class);
                Call<JsonObject> call = apiService.getTraffic(ApiService.APPKEY,1,centerlat, centerlon,"AROUND",1,19);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if(response.isSuccessful()){
                            JsonObject object = response.body();
                            if (object != null) {
                                traffic.setText(null);
                                trafficParser(object);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        traffic.setText("fail");
                    }
        });
    }

     private void getWeather(final double latitude, double longitude) {
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
                        weatherParser(object);
                        //날씨, 온도 텍스트 뷰 출력
                        weather.setText(sky + temp);
                        //locate.setText(city + county + village);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
            }
        });
    }

    public void weatherParser(JsonObject Object) {

        JsonArray jarr = Object.getAsJsonObject("weather").getAsJsonArray("hourly");
        JsonObject jobject = jarr.get(0).getAsJsonObject();

        city = jobject.get("grid").getAsJsonObject().get("city").toString();
        county = jobject.get("grid").getAsJsonObject().get("county").toString();
        village = jobject.get("grid").getAsJsonObject().get("village").toString();
        sky = jobject.get("sky").getAsJsonObject().get("name").toString();
        temp = jobject.get("temperature").getAsJsonObject().get("tc").toString();

    }

    public void trafficParser(JsonObject Object){

        JsonArray jarr = Object.getAsJsonArray("features");
        for(int i = 0; i < jarr.size(); i++){
            JsonObject jobject = (JsonObject)jarr.get(i).getAsJsonObject().get("properties");

            traffic.append(jobject.get("name").toString() + "\n" +
                    jobject.get("description").toString() + "\n" +
                    "통행속도 : " + jobject.get("speed").toString() + "km/h" + "\n\n");
        }
    }

}
