package com.read.ireader.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.read.ireader.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by fht_6 on 2017/5/8.
 */

public class TransTask extends AsyncTask<String, Void, String> {
    Context context;
    TextView tv_result_title;
    TextView tv_result_body;
    String query;

    private static final String TRANSLATOR_URL_BASE = "http://fanyi.youdao.com/openapi.do";
    private static final String TRANSLATOR_AUTH_FROM = "sse-iReader";
    private static final String TRANSLATOR_KEY = "1519952188";

    public TransTask(Context context, View trans_view) {
        // TODO Auto-generated constructor stub
        super();
        this.context = context;
        this.tv_result_title = (TextView) trans_view.findViewById(R.id.result_title);;
        this.tv_result_body = (TextView) trans_view.findViewById(R.id.result_body);
    }

    @Override
    protected String doInBackground(String... params) {
        query = params[0];

        ArrayList<NameValuePair> headerList = new ArrayList<NameValuePair>();
        headerList.add(new BasicNameValuePair("Content-Type", "text/html; charset=utf-8"));

        String targetUrl = TRANSLATOR_URL_BASE;

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair("keyfrom", TRANSLATOR_AUTH_FROM));
        paramList.add(new BasicNameValuePair("key", TRANSLATOR_KEY));
        paramList.add(new BasicNameValuePair("type", "data"));
        paramList.add(new BasicNameValuePair("doctype", "json"));
        paramList.add(new BasicNameValuePair("version", "1.1"));
        paramList.add(new BasicNameValuePair("q", query));

        for (int i = 0; i < paramList.size(); i++) {
            NameValuePair nowPair = paramList.get(i);
            String value = nowPair.getValue();
            try {
                value = URLEncoder.encode(value, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (i == 0) {
                targetUrl += ("?" + nowPair.getName() + "=" + value);
            } else {
                targetUrl += ("&" + nowPair.getName() + "=" + value);
            }
        }

        HttpGet httpRequest = new HttpGet(targetUrl);
        try {
            for (int i = 0; i < headerList.size(); i++) {
                httpRequest.addHeader(headerList.get(i).getName(),
                        headerList.get(i).getValue());
            }

            HttpClient httpClient = new DefaultHttpClient();

            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                return strResult;
            } else {
                tv_result_body.setText("查询失败！\n");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        tv_result_title.setText(query);
        StringBuilder translation = new StringBuilder("");
        if (result != null) {
            try {
                JSONObject query_result = new JSONObject(result);

                int errorCode = query_result.getInt("errorCode");
                if (errorCode == 0 && query_result.has("basic") && query_result.has("web")) {
                    translation.append(query_result.getJSONArray("translation").getString(0))
                            .append("\n\n");
                    if(query_result.has("basic")){
                        JSONObject basic = query_result.getJSONObject("basic");
                        if(basic.has("us-phonetic"))
                            translation.append("美式音标:")
                                    .append(basic.getString("us-phonetic"))
                                    .append("\n");
                        if(basic.has("phonetic"))
                            translation.append("英式音标:")
                                    .append(basic.getString("phonetic"))
                                    .append("\n");
                        translation.append("\n");
                        JSONArray explains = basic.getJSONArray("explains");
                        for(int i = 0;i < explains.length();i++){
                            translation.append(explains.getString(i))
                                    .append("\n");
                        }
                    }
                    if(query_result.has("web")) {
                        translation.append("\n相关引用:\n");
                        JSONArray web = query_result.getJSONArray("web");
                        for(int i = 0;i < web.length();i++){
                            JSONObject key = web.getJSONObject(i);
                            String value = (key.getString("value"));
                            value = value.replace("\",\"",";");
                            value = value.replace("[\"","");
                            value = value.replace("\"]","\n");
                            translation.append(key.getString("key"))
                                    .append("\n")
                                    .append(value);
                        }
                    }

                } else {
                    translation.append("查询失败！\n");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            translation.append("查询失败！\n");
        }
        tv_result_body.setText(translation);
    }

}
