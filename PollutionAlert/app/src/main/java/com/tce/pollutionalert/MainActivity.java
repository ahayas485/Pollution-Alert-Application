package com.tce.pollutionalert;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        ,LoaderManager.LoaderCallbacks<ArrayList<Pollution>> {

    private String url;
    ProgressBar progressBar;
    private Random randomGenerator;
    private int index,aqi;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = getResources().getString(R.string.URL2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkconnectivity();

    }

    /**
     * Used To Check whether Mobile is Connected to Internet or Not
     */

    public void checkconnectivity(){
        ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = con.getActiveNetworkInfo();
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        Log.e("inoncreate","yes");

        if(netinfo!=null && netinfo.isConnected()){
            Log.e("Internet","yes");
            getLoaderManager().initLoader(0, null, MainActivity.this);
        }else{
            Log.e("NoInternet","yes");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Used to create loader
     * @param id
     * @param args
     * @return
     */


    @Override
    public Loader<ArrayList<Pollution>> onCreateLoader(int id, Bundle args) {
        progressBar.setVisibility(View.VISIBLE);
        Log.e("oncreateloader","yes");
        return (new PollutionLoader(MainActivity.this,url));
    }

    /**
     * Called when loader is finished
     * @param loader
     * @param data
     */

    @Override
    public void onLoadFinished(Loader<ArrayList<Pollution>> loader, ArrayList<Pollution> data) {
        Log.e("onloadfinished","yes");
        progressBar.setVisibility(View.GONE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dochange();
                handler.postDelayed(this,5000);
            }
        }, 5000);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Pollution>> loader) {

    }


    /**
     * Funciton that changes the main screen
     */

    public void dochange(){
        randomGenerator = new Random();
        index = randomGenerator.nextInt(PollutionLoader.arrayListpolltion.size());

        textView = (TextView)findViewById(R.id.city);
        textView.setText(PollutionLoader.arrayListpolltion.get(index).getlocation());

        int aqi = PollutionLoader.arrayListpolltion.get(index).getaqi();
        textView = (TextView)findViewById(R.id.aqiValue);
        textView.setText(Integer.toString(aqi));

        textView = (TextView)findViewById(R.id.report);

        if(aqi>=0 && aqi<=50) {
            textView.setText("GOOD");
        }else if(aqi>=51 && aqi<=100) {
            textView.setText("MODERATE");
        }else if(aqi>=101 && aqi<=150) {
            textView.setText("UNHEALTHY FOR SENSITIVE GROUP");
        }else if(aqi>=151 && aqi<=200) {
            textView.setText("UNHEALTHY");
        }else if(aqi>=201 && aqi<=300) {
            textView.setText("VERY UNHEALTHY");
        }else{
            textView.setText("HAZARDOUS");
        }
    }


    /**
     * Navigation Drawer Works
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.markermapview) {
//            Toast.makeText(this,"makermap",Toast.LENGTH_SHORT).show();
            Log.e("started intenting","yes");
            Intent i = new Intent(MainActivity.this,MarkerMap.class);
            startActivity(i);
        } else if (id == R.id.heatmapview) {
//            Toast.makeText(this,"heatmap",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this,HeatMapView.class);
            startActivity(i);
        } else if(id == R.id.airforecast){
//            Toast.makeText(this,"heatmap",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this,forecast.class);
            startActivity(i);
        } else if(id == R.id.waterforecast){
//            Toast.makeText(this,"heatmap",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this,forecast.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
