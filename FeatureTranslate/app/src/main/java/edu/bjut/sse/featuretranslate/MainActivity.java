package edu.bjut.sse.featuretranslate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    EditText et_city;
    Button btn_query;
    TextView tv_result_body;
    TextView tv_result_title;
    Button btn_file_load;
    TransTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this,"Manifest.permission.READ_EXTERNAL_STORAGE")
                    != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        et_city = (EditText)findViewById(R.id.city);
        tv_result_body = (TextView)findViewById(R.id.result_body);
        tv_result_title = (TextView)findViewById(R.id.result_title);
        btn_query = (Button)findViewById(R.id.query);
        btn_file_load = (Button)findViewById(R.id.load);

        btn_query.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String city = et_city.getText().toString();
                if (city.length() < 1) {
                    Toast.makeText(MainActivity.this, "请输入城市名",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                task = new TransTask(MainActivity.this, tv_result_title, tv_result_body);
                task.execute(city);
            }
        });

        btn_file_load.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "选择文件"), 1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                try {
                    Toast.makeText(this, "文件路径："+ getPath(this, uri), Toast.LENGTH_SHORT).show();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it  Or Log it.
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
