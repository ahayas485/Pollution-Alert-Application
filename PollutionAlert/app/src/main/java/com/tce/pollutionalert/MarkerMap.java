package com.tce.pollutionalert;

import android.*;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;

public class MarkerMap extends AppCompatActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationButtonClickListener {

    GoogleMap googleMap;
    boolean mapReady = false;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private int PERMISSION_LOCATION = 1;
    double lat;
    double lon;
    private ClusterManager<myItem> clusterManager;
    HashMap<String,Integer> h = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_map);

        Log.e("done intenting","yes");

    }

    @Override
    protected void onStart() {
        super.onStart();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
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
     * Function is called when map is ready
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
        googleMap.setTrafficEnabled(false);

        Log.e("placelocbutton->marker", "yes");


        //Enable Location Button
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }else {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);

            //CLUSTERING OF MARKERS
            addclustermarker();
            clusterManager.cluster();
            clusterManager.setRenderer(new changecluster());
        }

        //Enable REQUIRED UI FOR MAPS
        googleMap.getUiSettings().setZoomControlsEnabled(true);


        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<myItem>() {
            @Override
            public boolean onClusterItemClick(myItem myItem) {
                Log.e("MarkerClicked","yes");
                final Dialog dialog = new Dialog(MarkerMap.this);
                dialog.setContentView(R.layout.custominfowindow);
                dialog.setTitle("ALERT");
                dialog.setCancelable(true);

                String loc;
                int aqi;
                TextView textView;
                ImageView imageView;
                View window;

                loc = myItem.getTitle();
                aqi = h.get(loc);
                textView = (TextView)dialog.findViewById(R.id.infoplace);
                textView.setText(loc);
                imageView = (ImageView)dialog.findViewById(R.id.infoimage);
                window  = dialog.findViewById(R.id.infowindow);

                if(aqi>=0 && aqi<=50) {
                    imageView.setImageResource(R.drawable.happy3);
                    textView = (TextView)dialog.findViewById(R.id.infostatus);
                    textView.setText("GOOD");
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.good));
                    window.setBackgroundColor(Color.GREEN);
                }else if(aqi>=51 && aqi<=100) {
                    imageView.setImageResource(R.drawable.smile3);
                    textView = (TextView)dialog.findViewById(R.id.infostatus);
                    textView.setText("MODERATE");
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.moderate));
                    window.setBackgroundColor(Color.YELLOW);
                }else if(aqi>=101 && aqi<=150) {
                    imageView.setImageResource(R.drawable.sad3);
                    textView = (TextView)window.findViewById(R.id.infostatus);
                    textView.setText("UNHEALTHY FOR SENSITIVE GROUP");
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.unhealthyforsensitivegroups));
                    textView.setText("");
                    window.setBackgroundColor(Color.rgb(255,127,39));
                }else if(aqi>=151 && aqi<=200) {
                    imageView.setImageResource(R.drawable.vsad3);
                    textView = (TextView)window.findViewById(R.id.infostatus);
                    textView.setText("UNHEALTHY");
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.unhealthy));
                    window.setBackgroundColor(Color.RED);
                }else if(aqi>=201 && aqi<=300) {
                    textView.setTextColor(Color.WHITE);
                    imageView.setImageResource(R.drawable.vvsad3);
                    textView = (TextView)window.findViewById(R.id.infostatus);
                    textView.setText("VERY UNHEALTHY");
                    textView.setTextColor(Color.WHITE);
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.veryunhealthy));
                    textView.setTextColor(Color.WHITE);
                    window.setBackgroundColor(Color.rgb(179,0,179));
                }else{
                    textView.setTextColor(Color.WHITE);
                    imageView.setImageResource(R.drawable.crying3);
                    textView = (TextView)window.findViewById(R.id.infostatus);
                    textView.setText("HAZARDOUS");
                    textView.setTextColor(Color.WHITE);
                    textView = (TextView)dialog.findViewById(R.id.infodescription);
                    textView.setText(getResources().getString(R.string.hazardous));
                    textView.setTextColor(Color.WHITE);
                    window.setBackgroundColor(Color.rgb(128,0,0));
                }

                dialog.show();
                Button bt = (Button)dialog.findViewById(R.id.ok);

                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                return false;
            }
        });
    }

    /**
     * Allows choose the map type
     * @param v
     */

    public void maptype(View v){
        final Dialog dialog = new Dialog(MarkerMap.this);
        dialog.setContentView(R.layout.maptype);
        dialog.setTitle("Choose Map Type");
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
     * This Function is used to
     * add individual markers
     * into the map
     */

    void addmarker(){
        Pollution pol;
        for(int i=0;i<PollutionLoader.arrayListpolltion.size();i++){
            pol  = PollutionLoader.arrayListpolltion.get(i);
            googleMap.addMarker(new MarkerOptions().position(new LatLng(pol.getlatitute(),pol.getlongitute())).title(pol.getlocation()));
        }
    }


    /**
     * Function used to search for a particular location
     * in the map and move the map view to that location
     */

    void setsearchforlocation(){
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

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
                Toast.makeText(MarkerMap.this,lat+" "+lon,Toast.LENGTH_SHORT).show();
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
     * Function used to respond to
     * the mylocation button click
     * @return boolean
     */

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                .addConnectionCallbacks(MarkerMap.this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /////////////////////////////////////////ONCONNECTED()/////////////////////////////////////////////////////////////////

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
                            ActivityCompat.requestPermissions(MarkerMap.this, new String[]{android.Manifest.permission_group.LOCATION}, PERMISSION_LOCATION);
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
     *Function to add Markerluster to the map
     */

    public void addclustermarker(){
        clusterManager = new ClusterManager<myItem>(this,googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        addItems();
    }

    /**
     * Function to add Items to the ClusterManager
     */

    public void addItems(){
        Pollution pol;

        Log.e("size",Integer.toString(PollutionLoader.arrayListpolltion.size()));
        for(int i=0;i<PollutionLoader.arrayListpolltion.size();i++){
            pol  = PollutionLoader.arrayListpolltion.get(i);
            clusterManager.addItem(new myItem(pol.getlatitute(),pol.getlongitute(),pol.getlocation(),"snippet",pol.getaqi()));
            h.put(pol.getlocation(),pol.getaqi());
        }

    }


    /**
     * Class which extends DefaultClusterRenderer<myItem> class
     * for marker customization
     */

    public class changecluster extends DefaultClusterRenderer<myItem> {
        public changecluster() {
            super(getApplicationContext(),googleMap,clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(myItem item, MarkerOptions markerOptions) {
            Log.e("onClusterItemRendered","yes");
            int aqi = h.get(markerOptions.getTitle());

            if(aqi>=0 && aqi<=50) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerOptions.snippet("Good " + Integer.toString(aqi));
            }else if(aqi>=51 && aqi<=100) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                markerOptions.snippet("Moderate "+ Integer.toString(aqi));
            }else if(aqi>=101 && aqi<=150) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                markerOptions.snippet("Unhealthy For Sensitive Groups "+ Integer.toString(aqi));
            }else if(aqi>=151 && aqi<=200) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markerOptions.snippet("Unhealthy "+ Integer.toString(aqi));
            }else if(aqi>=201 && aqi<=300) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                markerOptions.snippet("Very Unhealthy "+ Integer.toString(aqi));
            }else{
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker48));
                markerOptions.snippet("Hazardous "+ Integer.toString(aqi));
            }

            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////






}
