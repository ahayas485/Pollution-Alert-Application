package com.tce.pollutionalert;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;

public class HeatMapView extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,  GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    boolean mapReady = false;
    private int PERMISSION_LOCATION = 0;
    double lat;
    double lon;

    HeatmapTileProvider heatmapTileProvider;

    int[] colors = {
            Color.GREEN,    // green
            Color.YELLOW,    // yellow
            Color.rgb(255,165,0), //Orange
            Color.rgb(255,13,13),  //red
            Color.rgb(153,50,204), //dark orchid
            Color.rgb(128,0,0) //Maroon
    };

    float[] startpoints = {
            0.101f,    //0-50
            0.201f,   //51-100
            0.302f,   //101-150
            0.402f,   //151-200
            0.602f,    //201-300
            1.0f      //301-500
    };


    ////////////////////////////////////////////////VARIABLE DECLARATION ENDS///////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
    }

    @Override
    protected void onStart() {
        super.onStart();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.heatmap);
        mapFragment.getMapAsync(this);

        setsearchforlocation();

        //Google Client setup
        Log.e("setupgoogleclient", "yes");
        setupgoogleclientmarkermap();
        Log.e("connectinggoogleclient", "yes");
        googleApiClient.connect();


    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }


    /**
     * Called when map is ready
     * @param map
     */

    @Override
    public void onMapReady(GoogleMap map) {
        Log.e("onmapreadycall->marker", "yes");

        googleMap = map;
        mapReady = true;
        LatLng india = new LatLng(20.5937, 78.9629);
        CameraPosition target = CameraPosition.builder().target(india).zoom(4).build();
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(target), 5000, null);

        //Enable REQUIRED UI FOR MAPS
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        Log.e("placelocbutton->marker","yes");

        //Enable Location Button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this,"Kindly enable the Location",Toast.LENGTH_SHORT);
            return;
        }else {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
        }
        buildheatmap();

    }


    /**
     * Allows choose the map type
     * @param v
     */

    public void maptype(View v){
        final Dialog dialog = new Dialog(HeatMapView.this);
        dialog.setContentView(R.layout.maptype);
        dialog.setTitle("This is my custom dialog box");
        dialog.setCancelable(true);

        Button bt = (Button)dialog.findViewById(R.id.maptypechose);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup rg = (RadioGroup)dialog.findViewById(R.id.radiogroup);

                int radioButtonID = rg.getCheckedRadioButtonId();

                switch (radioButtonID){
                    case R.id.normal:
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.satellite:
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case R.id.hybrid:
                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case R.id.terrain:
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    default:
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    /**
     * Funcion to handle
     * the location button click event
     * @return
     */

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Location mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if(mLastKnownLocation!=null) {
            lat = mLastKnownLocation.getLatitude();
            lon = mLastKnownLocation.getLongitude();
            setPosition(lat, lon);
        }

        return false;
    }


    /**
     * Function used to search for a particular location
     * in the map and move the map view to that location
     */

    void setsearchforlocation(){

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_heatmap);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Toast.makeText(MainActivity.this,place.getLatLng().toString(),Toast.LENGTH_SHORT).show();
                String latlon = place.getLatLng().toString();
                String lat="",lon="";
                double latitude,longitude;
                for(int i=0;i<latlon.length();i++){
                    if(latlon.charAt(i)=='('){
                        i++;
                        while(latlon.charAt(i)!=','){
                            lat = lat + latlon.charAt(i);
                            i++;
                        }
                        i++;
                        while(latlon.charAt(i)!=')') {
                            lon = lon + latlon.charAt(i);
                            i++;
                        }
                    }
                }
                latitude = Double.parseDouble(lat);
                longitude = Double.parseDouble(lon);
                setPosition(latitude,longitude);
                Toast.makeText(HeatMapView.this,lat+" "+lon,Toast.LENGTH_SHORT).show();
                Log.e("onPlaceselected", "Place: " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.e("onError", "An error occurred: " + status);
            }
        });
    }


    /**
     * Function used to direct the current
     * location in map to the location
     * prointed by lt and lon parameters
     * @param lt
     * @param lon
     */


    public void setPosition(double lt,double lon) {
        LatLng pos = new LatLng(lt,lon);
        CameraPosition target = CameraPosition.builder().target(pos).zoom(15).bearing(0).tilt(65).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(target),4000,null);
    }

    /**
     * Function used to get googleapiclient support
     */

    public void setupgoogleclientmarkermap(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(HeatMapView.this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("onConnected", "yes");

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("nopermission", "yes");
            requestAllPermissions();
        }
    }


    /**
     * Function used to requet permissions
     * to use user's location data
     */

    public void requestAllPermissions() {
        Log.e("inrequestAllPermissions", "yes");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission_group.LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This Permission needed for accessing this application")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HeatMapView.this, new String[]{android.Manifest.permission_group.LOCATION}, PERMISSION_LOCATION);
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE},
                    PERMISSION_LOCATION);
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("suspednded","yes");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Failed","yes");
    }


    /**
     * Function used to add heatmaps into the map
     */

    private void buildheatmap(){

        Gradient gradient = new Gradient(colors,startpoints,500);
        heatmapTileProvider = new HeatmapTileProvider.Builder()
                .weightedData(heatmapdata())
                .radius(50)
                .gradient(gradient)
                .build();
        TileOverlayOptions tileoverlayoptions = new TileOverlayOptions().tileProvider(heatmapTileProvider);
        TileOverlay tileoverlay = googleMap.addTileOverlay(tileoverlayoptions);
        tileoverlay.clearTileCache();
        Toast.makeText(this,"added heatmap",Toast.LENGTH_SHORT).show();
    }

    /**
     * Funtion used to fetch heatmap data
     * @return ArrayList
     */

    private ArrayList heatmapdata() {
        ArrayList<WeightedLatLng> arr = new ArrayList<>();

        Pollution pol;
        for(int i=0;i<PollutionLoader.arrayListpolltion.size();i++){
            pol = PollutionLoader.arrayListpolltion.get(i);
            arr.add(new WeightedLatLng(new LatLng(pol.getlatitute(),pol.getlongitute()),pol.getaqi()));
        }

        Log.e("adding heatmap","yes");

        return arr;
    }

}
