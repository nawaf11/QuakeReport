/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private EqAdapter eqAdapter;
    private TextView mEmptyStateTextView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("quiz","Here onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        eqAdapter = new EqAdapter(this, new ArrayList<Earthquake>());
        earthquakeListView.setAdapter(eqAdapter);
        mEmptyStateTextView = (TextView) findViewById(R.id.emptyTxt);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        Log.v("quiz","InitLoader");
        getLoaderManager().initLoader(0,null,this);


        // set ClickLis
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = eqAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    public static ArrayList<Earthquake> fetchEqFromUrl(String url){
        Log.v("quiz","StartFetching");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.v("quiz","InterruptedException");
            e.printStackTrace();
        }
        try{
           return getListFromJson(makeHttpRequest(url));
        }catch (IOException e){
            Log.v("quiz","Exception in fetchEqFromUrl");
            e.printStackTrace();return null;}
    }

    private static ArrayList<Earthquake> getListFromJson (String json){
        Log.v("quiz","start getListFromJson");

        if (json == null || json.length() == 0) {
                return null;
            }
            ArrayList<Earthquake> arr = new ArrayList<>();
            JSONObject root;
            try {
                root = new JSONObject(json);
                JSONArray js_arr = root.getJSONArray("features");
                for (int i = 0; i < js_arr.length(); i++) {
                    JSONObject obj = (JSONObject) js_arr.get(i);
                    JSONObject properties = obj.getJSONObject("properties");
                    double mag = properties.getDouble("mag");
                    String place = properties.getString("place");
                    String time = properties.getString("time");
                    String url = properties.getString("url");
                    Earthquake eq = new Earthquake(mag, time, place, url);
                    arr.add(eq);
                }
                Log.v("quiz","SIZE: "+arr.size());
                return arr;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

    private static String makeHttpRequest(String mUrl) throws IOException {
        Log.v("quiz","start makeHttpReq");

        URL url = new URL(mUrl);
            String jsnResponse = "";
            if (url == null) {
                return jsnResponse;
            }

        HttpURLConnection httpURLConn = null;
            InputStream inputStream = null;
            try {
                httpURLConn = (HttpURLConnection) url.openConnection();
                httpURLConn.setRequestMethod("GET");
                httpURLConn.setReadTimeout(10000);
                httpURLConn.setConnectTimeout(15000);
                httpURLConn.connect();
                Log.e("quiz","mUrl:"+mUrl);

                Log.e("quiz","ResponseCde: "+httpURLConn.getResponseCode());
                if (httpURLConn.getResponseCode() == 200) {
                    inputStream = httpURLConn.getInputStream();
                    jsnResponse = stream2Json(inputStream);
                }

            } catch (IOException e) {
                Log.e("quiz","Exception makeHttpReq");

                e.printStackTrace();
                return "IOE";
            } finally {
                if (httpURLConn != null) {
                    httpURLConn.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }

        return jsnResponse;
        }

    private  static String stream2Json(InputStream inputStream) throws IOException {
        Log.v("quiz","start stream2Json");

        StringBuilder json = new StringBuilder();
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = bufferedReader.readLine();
                while (line != null) {
                    json.append(line);
                    line = bufferedReader.readLine();
                }
            }
            return json.toString();
        }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        Log.v("quiz","onCreateLoader()");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);

        String date =dateFormat.format(new Date());


        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", "time");
        uriBuilder.appendQueryParameter("starttime", yesterday());
        uriBuilder.appendQueryParameter("endtime", date);

        Log.d("x1","URI:"+uriBuilder.toString());



        return new EqLoader(this,uriBuilder.toString());
    }
    private String yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        return dateFormat.format( cal.getTime());

    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        View progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
        Log.v("quiz","onLoadFinished()");
        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText("no eq's");

        // Clear the adapter of previous earthquake data
        eqAdapter.clear();
        eqAdapter.clear();
        if(data!=null && !data.isEmpty()){
            eqAdapter.addAll(data);
        }


    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        eqAdapter.clear();
    }
}

    class EqLoader extends AsyncTaskLoader <List<Earthquake>>{
        private String mUrl;

        @Override
        protected void onStartLoading() {

            forceLoad();
        }

         EqLoader(Context context, String mUrl) {
            super(context);
            this.mUrl=mUrl;
        }

        @Override
        public List<Earthquake> loadInBackground() {

            if(mUrl==null) return null;
            return MainActivity.fetchEqFromUrl(mUrl);
        }


    }



