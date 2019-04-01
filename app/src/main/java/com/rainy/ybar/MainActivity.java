package com.rainy.ybar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rainy.ybarlib.YBar;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        YBar.with(this).init();
    }

    private boolean dartMode;

    public void click(View v) {
        int r = new Random().nextInt(256);
        int g = new Random().nextInt(256);
        int b = new Random().nextInt(256);
        int color = Color.rgb(r, g, b);
        YBar.with(this).barColor(color).dartMode(dartMode).init();
        dartMode = !dartMode;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YBar.destroy(this);
    }
}
