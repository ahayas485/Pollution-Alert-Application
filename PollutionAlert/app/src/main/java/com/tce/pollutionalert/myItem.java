package com.tce.pollutionalert;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class myItem implements ClusterItem {

    private LatLng mlatlon;
    private String mtitle;
    private String msnippet;
    private int aqi;

    public myItem(double lat, double lon, String title, String snippet, int aqilevel){
        mlatlon = new LatLng(lat,lon);
        mtitle = title;
        msnippet = snippet;
        aqi = aqilevel;
    }

    @Override
    public LatLng getPosition() {
        return mlatlon;
    }

    @Override
    public String getTitle() {
        return mtitle;
    }

    @Override
    public String getSnippet() {
        return msnippet;
    }

    public int getaqi() {
        return aqi;
    }
}
