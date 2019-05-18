package com.tce.pollutionalert;


public class Pollution {
    private String location;
    private int aqi;
    private double latitute;
    private double longitute;

    public Pollution(String loc,int aqilevel,double lat,double lon){
        location = loc;
        aqi = aqilevel;
        latitute = lat;
        longitute = lon;
    }

    public String getlocation(){
        return location;
    }

    public int getaqi(){
        return aqi;
    }

    public double getlatitute(){return latitute;}

    public double getlongitute(){return longitute;}
}
