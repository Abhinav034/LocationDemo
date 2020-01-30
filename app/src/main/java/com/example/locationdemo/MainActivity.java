package com.example.locationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;
    LocationManager locationManager;
    LocationListener locationListener;


    Button btn_start , btn_stop;
    TextView location_textview;
    Location lastKnownLocation;

    // the other way to access the userLocation with FusedLocationProvider

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission()){
           requestPermission();
        }else{
            getLastLocation();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location_textview = findViewById(R.id.txt_location);
        btn_start = findViewById(R.id.btn_update);
        btn_stop = findViewById(R.id.btn_stop);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged" + location);

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
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);

        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0 ,locationListener);

            buildLocationRequest();
            buildLocationCallback();

        }

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fusedLocationProviderClient.requestLocationUpdates(locationRequest , locationCallback , Looper.myLooper());
                btn_start.setEnabled(btn_start.isEnabled());
                btn_stop.setEnabled(btn_stop.isEnabled());

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                btn_start.setEnabled(btn_start.isEnabled());
                btn_stop.setEnabled(btn_stop.isEnabled());



            }
        });

    }
            // allowed or denied permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length <= 0){

            Log.i(TAG , "onRequestPermission:" + "User interactrion was cancelled");


        }
        else if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            buildLocationCallback();
            buildLocationRequest();


        }else {
            showSnackBar(R.string.warning_txt, R.string.settings, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }



    }

    private void buildLocationRequest(){
       locationRequest = new LocationRequest();
       locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       locationRequest.setInterval(5000);
       locationRequest.setFastestInterval(3000);
       locationRequest.setSmallestDisplacement(10);

    }

    private void buildLocationCallback(){
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location: locationResult.getLocations()){
                    setLocation(location);

                }
            }
        };

    }

        private boolean checkPermission(){

        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;

        }
        private void requestPermission(){

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this ,Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale){
            Log.i(TAG , "requestPermission" + "Displaying the permission");
            //provide a way so that user grant permission
            showSnackBar(R.string.warning_txt, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLocationPermissionRequest();
                }
            });
        }
        else{
            startLocationPermissionRequest();
        }

        }
        private void startLocationPermissionRequest(){
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , REQUEST_CODE);
        }


        private void getLastLocation(){

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                  if (task.isSuccessful() && task.getResult() != null){
                      lastKnownLocation = task.getResult();

                      setLocation(lastKnownLocation);


                  }
                }
            });


        }

        private void setLocation(Location location){
            location_textview.setText(String.valueOf(location.getLatitude()) + "/" +
                    String.valueOf(location.getLongitude()));

        }

        private void getLocation(){
            locationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    for (Location location: locationResult.getLocations()){
                        setLocation(location);
                    }
                }
            };
        }

        private void showSnackBar(final int mainStringID , final int actionStringID , View.OnClickListener listener){
            Snackbar.make(findViewById(android.R.id.content),
                    getString(mainStringID),
                    Snackbar.LENGTH_INDEFINITE).setAction(actionStringID ,listener).show();
        }

}
