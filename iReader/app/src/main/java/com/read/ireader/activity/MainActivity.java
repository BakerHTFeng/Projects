package com.read.ireader.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.read.ireader.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private int theme = R.drawable.background1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final List<Map<String, Object>> arr = new ArrayList<Map<String, Object>>();
        int a = 10;
        for (int i = 0; i < a; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", "book"+i);
            arr.add(map);
        }
        GridView gridView = (GridView) findViewById(R.id.booklist);
        gridView.setNumColumns(3);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
        SimpleAdapter adapter = new SimpleAdapter(this, arr, R.layout.bookitem_layout, new String[]{"name"}, new int[]{R.id.txtBookname});
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
                Map<String, Object> map = arr.get(position);

                Toast toast = new Toast(MainActivity.this);
                View toast_view = LayoutInflater.from(MainActivity.this).inflate(R.layout.toast_view, null);
                TextView message = (TextView) toast_view.findViewById(R.id.toast);
                message.setText("正在阅读" + "book" + position + "...");
                toast.setGravity(Gravity.BOTTOM, 0, 200);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toast_view);
                toast.show();

                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putExtra("name", "book" + position);
                intent.putExtra("theme", theme);
                startActivity(intent);
            }
        });
    }

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.postil) {
            // Handle the camera action
            Intent intent = new Intent(MainActivity.this, PostilActivity.class);
            startActivity(intent);
        } else if (id == R.id.label) {
            Intent intent = new Intent(MainActivity.this, LabelActivity.class);
            startActivity(intent);

        } else if (id == R.id.upload) {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);

        } else if (id == R.id.record) {
            Intent intent = new Intent(MainActivity.this, RecordActivity.class);
            startActivity(intent);

        } else if (id == R.id.theme) {
            theme = (theme == R.drawable.background1)
                    ? R.drawable.background2 : R.drawable.background1;
            NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
            RelativeLayout rel_layout = (RelativeLayout) findViewById(R.id.content_main);
            nav_view.setBackground(getDrawable(theme));
            rel_layout.setBackground(getDrawable(theme));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
