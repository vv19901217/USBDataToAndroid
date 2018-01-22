package cn.wiky.systemLocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.wiky.usbdatatoandroid.R;
import logger.Logger;

import static java.security.AccessController.getContext;

public class GPSLocationActivity extends AppCompatActivity {

    @Bind(R.id.textview)
    TextView textview;
    private LocationManager mLocationManager;//位置管理器
    private static final int REQUEST_PERMISSION_LOCATION = 255; // int should be between 0 and 255


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpslocation);
        ButterKnife.bind(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        } else {
            // We have already permission to use the location
        }
        init();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We now have permission to use the location
                Logger.e("没有权限？" +
                        "1111");
                
            }
        }
    }
    private void init() {

        //获取到位置管理器实例
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取到GPS_PROVIDER
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Logger.e(String.valueOf(location));
        mLocationManager.addNmeaListener(mNeaListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Logger.e("111111111====");

                // 当GPS定位信息发生改变时，更新位置
                updata(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Logger.e("444444====");

            }

            @Override
            public void onProviderEnabled(String s) {
                Logger.e("2222222====");

// 当GPS Location Provider可用时，更新位置
                if (ActivityCompat.checkSelfPermission(GPSLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GPSLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                updata(mLocationManager.getLastKnownLocation(s));
            }

            @Override
            public void onProviderDisabled(String s) {
                Logger.e("33333333====");

            }


        });
    }

    private GpsStatus.NmeaListener mNeaListener=new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long l, String s) {
            Logger.e("gps:=="+s);
        }
    };
    private void updata(Location location){
        Logger.e("55555====");

        if(location != null){
            StringBuilder sb = new StringBuilder();
            sb.append("实时的位置信息:\n");
//            sb.append("经度:");
//            sb.append(location.getLongitude());
//            sb.append("\n纬度:");
//            sb.append(location.getLatitude());
//            sb.append("\b高度:");
//            sb.append(location.getAltitude());
//            sb.append("\n速度：");
//            sb.append(location.getSpeed());
//            sb.append("\n方向：");
//            sb.append(location.getBearing());
            sb.append(location);
            textview.setText(sb.toString());
        }
    }
}
