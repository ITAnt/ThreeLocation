package com.itant.dotonmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.itant.dotonmap.bean.LocationBean;
import com.itant.dotonmap.tool.DialogTool;
import com.umeng.analytics.MobclickAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int BAIDU_READ_PHONE_STATE = 100;

    public static final String KEY_REQUEST_CODE = "request_code";
    public static final String KEY_REQUEST_CITY = "request_city";
    public static final String KEY_SELECTED_LOCATION = "selected_location";
    public static final int REQUEST_CODE_1 = 1;
    public static final int REQUEST_CODE_2 = 2;
    public static final int REQUEST_CODE_3 = 3;

    private MapView mv_main;
    private BaiduMap mBaiduMap;
    //LocationClient类是定位SDK的核心类
    public LocationClient mLocationClient;
    private BDLocation mLocation;
    private BDLocationListener mLocationListener;

    private LocationBean mLocationBean1;
    private LocationBean mLocationBean2;
    private LocationBean mLocationBean3;
    private LatLng mCurrentLatLng;
    private LatLng mPoint1;
    private LatLng mPoint2;
    private LatLng mPoint3;

    private TextView tv_1;
    private TextView tv_2;
    private TextView tv_3;

    // 保留两位小数
    private DecimalFormat mFormat = new DecimalFormat("#.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ll_1).setOnClickListener(this);
        findViewById(R.id.ll_2).setOnClickListener(this);
        findViewById(R.id.ll_3).setOnClickListener(this);

        tv_1 = findViewById(R.id.tv_1);
        tv_2 = findViewById(R.id.tv_2);
        tv_3 = findViewById(R.id.tv_3);

        boolean isPermissionGranted = isPermissionGranted();
        if (isPermissionGranted) {
            initBaiduMap();
        }
    }

    private void initBaiduMap() {
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // 未打开位置开关，可能导致定位失败或定位不准，提示用户或做相应处理
            Toast.makeText(this, "为保证定位准确请打开位置开关", Toast.LENGTH_SHORT).show();
        }

        //获取地图控件引用
        mv_main = findViewById(R.id.mv_main);
        mBaiduMap = mv_main.getMap();

        // 定位开始----------------------->
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        //打开gps
        option.setOpenGps(true);
        //就是这个方法设置为true，才能获取当前的位置信息(当前城市名等信息)
        option.setIsNeedAddress(true);
        //设置坐标类型为bd09ll 百度需要的坐标，也可以返回其他type类型，大家可以查看下
        option.setCoorType("bd09ll");
        //设置网络优先
        option.setPriority(LocationClientOption.NetWorkFirst);
        //定时定位，每隔5秒钟定位一次
        //option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
        mLocationListener = new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null)
                    return;
                // 这里可以获取经纬度
                mLocation = bdLocation;
                showHere();
            }
        };
        mLocationClient.registerLocationListener(mLocationListener);
        // 这句代码百度api上给的没有，没有这个代码下面的回调方法不会执行的
        mLocationClient.start();
        // 定位结束<-----------------------
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        String[] mRequiredPermissions = {Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        boolean permissionGranted = true;
        for (String permission : mRequiredPermissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }

        if (permissionGranted) {
            return true;
        }

        // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
        ActivityCompat.requestPermissions(this, mRequiredPermissions, BAIDU_READ_PHONE_STATE);

        return false;
    }

    private void showHere() {
        if (mLocation == null) {
            return;
        }
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                //.accuracy(mLocation.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                //.direction(100)
                .latitude(mLocation.getLatitude())
                .longitude(mLocation.getLongitude()).build();

        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);

        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.mipmap.location);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfiguration(config);

        // 显示当前位置
        LatLng ll = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mCurrentLatLng = ll;
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

    }



    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        if (mv_main != null) {
            mv_main.onPause();
        }

        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        if (mv_main != null) {
            mv_main.onResume();
        }

        MobclickAgent.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        if (mv_main != null) {
            mv_main.onDestroy();
        }

        if (mLocationClient != null && mLocationListener != null) {
            mLocationClient.unRegisterLocationListener(mLocationListener);
        }
    }


    @Override
    public void onClick(View v) {

        if (mLocation == null || TextUtils.isEmpty(mLocation.getCity())) {
            Toast.makeText(this, "获取当前城市失败", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {

            case R.id.ll_1:
                Intent locationIntent1 = new Intent(this, SearchActivity.class);
                locationIntent1.putExtra(KEY_REQUEST_CODE, REQUEST_CODE_1);
                locationIntent1.putExtra(KEY_REQUEST_CITY, mLocation.getCity());
                startActivityForResult(locationIntent1, REQUEST_CODE_1);
                break;
            case R.id.ll_2:
                Intent locationIntent2 = new Intent(this, SearchActivity.class);
                locationIntent2.putExtra(KEY_REQUEST_CODE, REQUEST_CODE_2);
                locationIntent2.putExtra(KEY_REQUEST_CITY, mLocation.getCity());
                startActivityForResult(locationIntent2, REQUEST_CODE_2);
                break;
            case R.id.ll_3:
                Intent locationIntent3 = new Intent(this, SearchActivity.class);
                locationIntent3.putExtra(KEY_REQUEST_CODE, REQUEST_CODE_3);
                locationIntent3.putExtra(KEY_REQUEST_CITY, mLocation.getCity());
                startActivityForResult(locationIntent3, REQUEST_CODE_3);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                //没有获取到权限，做特殊处理
                DialogTool.showPermissionDialog(this);
                return;
            }
        }

        //获取到权限，做相应处理
        //调用定位SDK应确保相关权限均被授权，否则会引起定位失败
        initBaiduMap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_1:
                mLocationBean1 = (LocationBean) data.getSerializableExtra(KEY_SELECTED_LOCATION);
                tv_1.setText(mLocationBean1.getAddress() + " " + mLocationBean1.getName());
                break;
            case REQUEST_CODE_2:
                mLocationBean2 = (LocationBean) data.getSerializableExtra(KEY_SELECTED_LOCATION);
                tv_2.setText(mLocationBean2.getAddress() + " " +  mLocationBean2.getName());
                break;
            case REQUEST_CODE_3:
                mLocationBean3 = (LocationBean) data.getSerializableExtra(KEY_SELECTED_LOCATION);
                tv_3.setText(mLocationBean3.getAddress() + " " + mLocationBean3.getName());
                break;
        }

        if (mLocationBean1 != null && mLocationBean2 != null && mLocationBean3 != null) {
            // 说明3个点都齐全了，可以绘制了。绘制之前，先清除之前的图层和标记。

            if (mBaiduMap == null) {
                return;
            }

            // 添加标记
            setupMark();
        }
    }

    private void setupMark() {
        // 清除标记
        mBaiduMap.clear();

        //创建OverlayOptions的集合
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        //设置坐标点
        LatLng point1 = new LatLng(mLocationBean1.getLat(), mLocationBean1.getLng());
        LatLng point2 = new LatLng(mLocationBean2.getLat(), mLocationBean2.getLng());
        LatLng point3 = new LatLng(mLocationBean3.getLat(), mLocationBean3.getLng());
        mPoint1 = point1;
        mPoint2 = point2;
        mPoint3 = point3;

        //创建OverlayOptions属性
        OverlayOptions option1 =  new MarkerOptions()
                .position(point1)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_1));
        OverlayOptions option2 =  new MarkerOptions()
                .position(point2)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_2));
        OverlayOptions option3 =  new MarkerOptions()
                .position(point3)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_3));

        //将OverlayOptions添加到list
        options.add(option1);
        options.add(option2);
        options.add(option3);

        // 1.画线
        List<LatLng> points = new ArrayList<>();
        //构建折线点坐标
        points.add(point1);
        points.add(point2);
        points.add(point3);
        drawLines(points);

        // 2.在地图上批量添加点
        mBaiduMap.addOverlays(options);

        // 3.计算点之间的距离
        calculateDistance();

        // 让标注显示在最佳视野内
        LatLng[] viewPoints = {point1, point2, point3};
        showWellZoom(viewPoints);
    }

    private void showWellZoom(LatLng[] points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : points) {
            builder = builder.include(p);
        }
        LatLngBounds latlngBounds = builder.build();
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds, mv_main.getWidth(), mv_main.getHeight());
        mBaiduMap.animateMapStatus(u);
    }

    private void drawLines(List<LatLng> points) {
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.blue_alipay));
        colors.add(getResources().getColor(R.color.green_pressed));
        //colors.add(getResources().getColor(R.color.purple));
        //colors.add(getResources().getColor(R.color.gray_text_s));

        //绘制折线(这样先画连着的，再画开始和结束，分开画，是为了避免颜色错乱，百度地图的bug)
        OverlayOptions ooPolyline1 = new PolylineOptions()
                .width(8)
                .colorsValues(colors)
                .points(points);
        mBaiduMap.addOverlay(ooPolyline1);

        List<LatLng> startEndPoints = new ArrayList<>();
        startEndPoints.add(mPoint1);
        startEndPoints.add(mPoint3);
        Integer purpleColor = getResources().getColor(R.color.purple);
        OverlayOptions ooPolyline2 = new PolylineOptions()
                .width(8)
                .color(purpleColor)
                .points(startEndPoints);
        mBaiduMap.addOverlay(ooPolyline2);
    }

    private void calculateDistance() {
        String p1ToP2 = mFormat.format(DistanceUtil.getDistance(mPoint1, mPoint2)/1000);
        String p2ToP3 = mFormat.format(DistanceUtil.getDistance(mPoint2, mPoint3)/1000);
        String p1ToP3 = mFormat.format(DistanceUtil.getDistance(mPoint1, mPoint3)/1000);

        //定义文字所显示的坐标点
        LatLng llText1 = new LatLng((mPoint1.latitude+mPoint2.latitude)/2, (mPoint1.longitude+mPoint2.longitude)/2);
        //构建文字Option对象，用于在地图上添加文字
        OverlayOptions textOption1 = new TextOptions()
                .bgColor(getResources().getColor(R.color.gray_light_s))
                .fontSize(34)
                .fontColor(getResources().getColor(R.color.blue_alipay))
                .text(p1ToP2+"公里")
                //.rotate(-30)
                .position(llText1);
        //在地图上添加该文字对象并显示
        mBaiduMap.addOverlay(textOption1);

        //定义文字所显示的坐标点
        LatLng llText2 = new LatLng((mPoint2.latitude+mPoint3.latitude)/2, (mPoint2.longitude+mPoint3.longitude)/2);
        //构建文字Option对象，用于在地图上添加文字
        OverlayOptions textOption2 = new TextOptions()
                .bgColor(getResources().getColor(R.color.gray_light_s))
                .fontSize(34)
                .fontColor(getResources().getColor(R.color.green_pressed))
                .text(p2ToP3+"公里")
                //.rotate(-30)
                .position(llText2);
        //在地图上添加该文字对象并显示
        mBaiduMap.addOverlay(textOption2);

        //定义文字所显示的坐标点
        LatLng llText3 = new LatLng((mPoint1.latitude+mPoint3.latitude)/2, (mPoint1.longitude+mPoint3.longitude)/2);
        //构建文字Option对象，用于在地图上添加文字
        OverlayOptions textOption3 = new TextOptions()
                .bgColor(getResources().getColor(R.color.gray_light_s))
                .fontSize(34)
                .fontColor(getResources().getColor(R.color.purple))
                .text(p1ToP3+"公里")
                //.rotate(-30)
                .position(llText3);
        //在地图上添加该文字对象并显示
        mBaiduMap.addOverlay(textOption3);
    }

    private long mLastBackMillis;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastBackMillis > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            mLastBackMillis = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
