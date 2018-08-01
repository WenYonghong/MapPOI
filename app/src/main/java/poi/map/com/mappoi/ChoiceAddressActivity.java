package poi.map.com.mappoi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import poi.map.com.mappoi.Model.PoiModel;


public class ChoiceAddressActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener, View.OnClickListener {


    private MapView mapView;
    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    private AMap aMap;
    private LatLng target;
    private GPS_Receiver gps_receiver;
    private GeocodeSearch geocodeSearch;
    private ImageView iv;
    private RelativeLayout rl;
    private Activity mActivity;

    private String address1 = "";
    private String address2 = "";

    private PoiSearch.Query query;
    private TranslateAnimation animation;
    private PoiSearch poiSearch;
    private boolean first = true;

    private List<PoiModel> data = new ArrayList<>();
    private LvAdapter lvAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_address);
        mActivity = this;
        mapView = (MapView) findViewById(R.id.map);
        iv = (ImageView) findViewById(R.id.iv);
        rl = (RelativeLayout) findViewById(R.id.rl);
        ListView lv = (ListView) findViewById(R.id.lv);
        lvAdapter = new LvAdapter();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setChecked(false);
                }
                data.get(position).setChecked(true);
                lvAdapter.notifyDataSetChanged();
            }
        });
        lv.setAdapter(lvAdapter);
        // 此方法必须重写
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //自定义图标与灵位跟随
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_sign));
        //设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setOnCameraChangeListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        });


        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        animation = new TranslateAnimation(0, 0, 0, -100);
                        animation.setDuration(500);//设置动画持续时间
                        animation.setRepeatCount(1);//设置重复次数
                        animation.setRepeatMode(Animation.REVERSE);//设置反方向执行
                        rl.startAnimation(animation);
                        getAddress2();
                        break;
                }
            }
        });
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        registerGSPreceiver();
    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            //启动定位
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);
                if (first) {
                    first = false;
                    query = new PoiSearch.Query(amapLocation.getAddress(), "", amapLocation.getCityCode());
                    query.setPageSize(10);
                    query.setPageNum(0);
                    poiSearch = new PoiSearch(ChoiceAddressActivity.this, query);
                    poiSearch.setOnPoiSearchListener(ChoiceAddressActivity.this);
                    poiSearch.searchPOIAsyn();
                }
            } else {

            }
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        target = cameraPosition.target;

    }

    public void getAddress2() {
        LatLonPoint latLonPoint = new LatLonPoint(target.latitude, target.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }


    private void registerGSPreceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        gps_receiver = new GPS_Receiver();
        registerReceiver(gps_receiver, filter);

    }

    private boolean getGPSState(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean on = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return on;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unregisterReceiver(gps_receiver);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        address1 = regeocodeResult.getRegeocodeAddress().getFormatAddress();
        if (!address1.equals(address2)) {
            query = new PoiSearch.Query(regeocodeResult.getRegeocodeAddress().getFormatAddress(), "", regeocodeResult.getRegeocodeAddress().getCityCode());
            query.setPageSize(10);
            query.setPageNum(0);
            poiSearch = new PoiSearch(ChoiceAddressActivity.this, query);
            poiSearch.setOnPoiSearchListener(ChoiceAddressActivity.this);
            poiSearch.searchPOIAsyn();
            address2 = address1;
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onPoiSearched(PoiResult result, int resultCode) {
        if (resultCode == 1000) {
            data.removeAll(data);
            for (int i = 0; i < result.getPois().size(); i++) {
                if (i == 0) {
                    data.add(new PoiModel(result.getPois().get(i).toString(), result.getPois().get(i).getCityName(),
                            result.getPois().get(i).getProvinceName(),
                            result.getPois().get(i).getCityCode(),
                            result.getPois().get(i).getProvinceCode(),
                            result.getPois().get(i).getSnippet(),
                            result.getPois().get(i).getLatLonPoint().getLongitude(),
                            result.getPois().get(i).getLatLonPoint().getLatitude(),
                            true));
                } else {
                    data.add(new PoiModel(result.getPois().get(i).toString(), result.getPois().get(i).getCityName(),
                            result.getPois().get(i).getProvinceName(),
                            result.getPois().get(i).getCityCode(),
                            result.getPois().get(i).getProvinceCode(),
                            result.getPois().get(i).getSnippet(),
                            result.getPois().get(i).getLatLonPoint().getLongitude(),
                            result.getPois().get(i).getLatLonPoint().getLatitude(),
                            false));
                }
                lvAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_Y:
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).isChecked()) {
                        Toast.makeText(mActivity, data.get(i).getProvince() + data.get(i).getCity() + data.get(i).getSnippet() + data.get(i).getAddressName(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.ib_search:
                startActivityForResult(new Intent(mActivity, SearchAddressActivity.class), 101);
                break;
            case R.id.ib_N:
                finish();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            LatLng latLng = new LatLng(data.getDoubleExtra("LocationY", 0), data.getDoubleExtra("LocationX", 0));
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            query = new PoiSearch.Query(data.getStringExtra("addressName"), "", data.getStringExtra("cityCode"));
            query.setPageSize(10);
            query.setPageNum(0);
            poiSearch = new PoiSearch(ChoiceAddressActivity.this, query);
            poiSearch.setOnPoiSearchListener(ChoiceAddressActivity.this);
            poiSearch.searchPOIAsyn();
            address2 = address1;
        }
    }

    class LvAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            viewHolder vh = null;
            if (convertView == null) {
                vh = new viewHolder();
                convertView = View.inflate(mActivity, R.layout.item_map, null);
                vh.iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
                vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                vh.tv_address_name = (TextView) convertView.findViewById(R.id.tv_address_name);
                convertView.setTag(vh);
            } else {
                vh = (viewHolder) convertView.getTag();
            }
            if (data.get(position).isChecked()) {
                vh.iv_img.setBackgroundResource(R.drawable.icon_choice_bjg_h);
            } else {
                vh.iv_img.setBackgroundResource(R.drawable.icon_choice_bjg_d);
            }
            vh.tv_name.setText(data.get(position).getProvince() + data.get(position).getCity() + data.get(position).getSnippet());
            vh.tv_address_name.setText(data.get(position).getAddressName());

            return convertView;
        }

    }

    class viewHolder {
        ImageView iv_img;
        TextView tv_name;
        TextView tv_address_name;
    }


    class GPS_Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                boolean on = getGPSState(context);
                if (on) {

                } else {

                }
            }
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {
                    ////////网络连接
                    if (netInfo.getType() == ConnectivityManager.TYPE_WIFI || netInfo.getType() == ConnectivityManager.TYPE_ETHERNET || netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        //初始化定位
                        mlocationClient = new AMapLocationClient(ChoiceAddressActivity.this);
                        //初始化定位参数
                        mLocationOption = new AMapLocationClientOption();
                        //设置定位回调监听
                        mlocationClient.setLocationListener(ChoiceAddressActivity.this);
                        //设置为高精度定位模式
                        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                        mLocationOption.setOnceLocation(true);
                        //设置定位参数
                        mlocationClient.setLocationOption(mLocationOption);
                        //启动定位
                        mlocationClient.startLocation();
                    }
                } else {
                    ////////网络断开

                }
            }
        }

    }
}