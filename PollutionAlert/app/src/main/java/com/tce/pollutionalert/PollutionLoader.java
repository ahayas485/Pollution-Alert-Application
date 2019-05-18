package com.tce.pollutionalert;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;


public class PollutionLoader extends AsyncTaskLoader<ArrayList<Pollution> >{

    String url;
    public static ArrayList<Pollution> arrayListpolltion;

    public PollutionLoader(Context context,String url1) {
        super(context);
        url = url1;
    }

    @Override
    protected void onStartLoading() {
        Log.e("onstartloading","yes");
        forceLoad();
    }

    @Override
    public ArrayList<Pollution> loadInBackground() {
        Log.e("onloadbackground","yes");
        ArrayList<Pollution> pollution = new ArrayList<>();

        pollution = Utils.fetchFeedbackData(url);
        arrayListpolltion = pollution;

        return pollution;
    }
}
