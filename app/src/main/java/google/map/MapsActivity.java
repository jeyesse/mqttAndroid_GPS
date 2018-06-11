package google.map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import tp.skt.example.Configuration;
import tp.skt.example.MyApp;
import tp.skt.example.NodeData;
import tp.skt.example.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static double latitude;
    public static double longitude;
    //for googleMap
    private GoogleMap mMap;
    private boolean isGPSEnable = false;
    HashMap<String, NodeData> nodeMap = new HashMap<String , NodeData>();

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GPSListener gpsListener = new GPSListener();
        long minTime = 1000;
        float minDistance = 0;

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, gpsListener);
        nodeMap = ((MyApp)getApplication()).getNodeMap();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnable = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Add a marker in my location and move the camera
        if (!isGPSEnable) {  //GPS setting
            showSettingsAlertLocation();
        }
        if (!isConnected()) { //wifi setting
            showSettingsAlertWifi();
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            try {
                Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double nodeLatitude = Double.parseDouble(nodeMap.get(Configuration.ONEM2M_NODEID).getLatitude());
                double nodeLongitude = Double.parseDouble(nodeMap.get(Configuration.ONEM2M_NODEID).getLongitude());
                mMap.addMarker(new MarkerOptions().
                        position(new LatLng(nodeLatitude,nodeLongitude)).
                        title(Configuration.ONEM2M_NODEID)).
                        setSnippet("Lat:" + String.format("%.4f", nodeLatitude) + " Lon:" + String.format("%.4f", nodeLongitude));

                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                    LatLng myLocation = new LatLng(latitude, longitude);

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(20);
                    mMap.animateCamera(zoom);
                } else {
                    Toast.makeText(this, "can not find location", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "start find location", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettingsAlertWifi() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("set Internet");
        alertDialog.setMessage("Internet is not available \n Do you want to go to setting?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                finish();
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext(), "Internet available", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                finish();
            }
        });
        alertDialog.show();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    //go to GPS setting
    private void showSettingsAlertLocation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("set GPS");
        alertDialog.setMessage("GPS is not available \n Do you want to go to setting?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                finish();
                getApplicationContext().startActivity(intent);
                Toast.makeText(getApplicationContext(), "gps available", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                finish();
            }
        });
        alertDialog.show();
    }

    private class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            LatLng myLocation = new LatLng(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
