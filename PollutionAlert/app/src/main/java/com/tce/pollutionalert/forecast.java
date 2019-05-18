package com.tce.pollutionalert;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class forecast extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        imageView = (ImageView)findViewById(R.id.forecast);
        imageView.setImageResource(R.drawable.graph2019air);

    }
}