package com.example.accesscurrentloction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.Permission;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mgoogleApiClient;
    private Location mlocation;
    private LocationManager mlocationManager;
    private LocationRequest mlocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL=2000;
    private long FASTEST_INTERVAL=5000;
    private LocationManager locationManager;
    private LatLng latLng;
    private boolean ispermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(requestSinglepermission())
        {

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mgoogleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mlocationManager=(LocationManager) this.getSystemService(LOCATION_SERVICE);
        checkLocation();










    }

    private Boolean checkLocation() {
        if(!isLoctionEnable())
        {
            showAlert();
        }
        return isLoctionEnable();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location").setMessage("Your location settismgs is set off.\n please Enable location to"+"Use this app")
                .setPositiveButton("LOcation Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myintent=new Intent(Settings.ACTION_LOCALE_SETTINGS);
                        startActivity(myintent);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
        dialog.show();
    }

    private boolean isLoctionEnable() {
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean requestSinglepermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                ispermission=true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                if(response.isPermanentlyDenied())
                {
                    ispermission=false;
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
        return  ispermission;
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(latLng!=null)
        {
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17F));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;

        }
        startlocationupdate();
        mlocation=LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
        if(mlocation==null)
        {
            startlocationupdate();

        }
        else
        {
            Toast.makeText(MapsActivity.this,"Location Not Detected",Toast.LENGTH_LONG).show();
        }

    }

    private void startlocationupdate() {
        mlocationRequest=LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,mlocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String mag="your Location is:"+
                Double.toString(location.getLatitude())+","+
                Double.toString(location.getLongitude());
        Toast.makeText(MapsActivity.this,mag,Toast.LENGTH_LONG).show();
        latLng=new LatLng(location.getLatitude(),location.getLongitude());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mgoogleApiClient!=null)
        {
            mgoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mgoogleApiClient.isConnected())
        {
            mgoogleApiClient.disconnect();
        }
    }
}
