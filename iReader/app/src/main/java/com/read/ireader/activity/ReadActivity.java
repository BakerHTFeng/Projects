package com.read.ireader.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.read.ireader.R;
import com.read.ireader.view.ScanView;
import com.read.ireader.view.ScanViewAdapter;

public class ReadActivity extends AppCompatActivity {
    ScanView scanview;
    ScanViewAdapter adapter;
    int index = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        scanview = (ScanView) findViewById(R.id.scanview);
        this.registerForContextMenu(scanview);

        List<String> items = new ArrayList<String> ();
        for (int i = 0; i < 8; i++)
        items.add("第 "  + (i + 1)+ " 页");
        adapter = new ScanViewAdapter(this, items, getDrawable(getIntent().getExtras().getInt("theme")));
        scanview.setAdapter(adapter,index);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getExtras().getString("name"));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();//后退箭头
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {//返回箭头
        finish();
        return super.onSupportNavigateUp();
    }
}




