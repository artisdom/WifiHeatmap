/*
 * Copyright (c) 2017 Seth Werly.
 *
 * This file is part of WifiHeatmap.
 *
 *     WifiHeatmap is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     WifiHeatmap is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with WifiHeatmap.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swerly.wifiheatmap.utils;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.swerly.wifiheatmap.BaseApplication;
import com.swerly.wifiheatmap.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Seth on 7/15/2017.
 *
 * friggin google maps apis for android never working and causing me to write my own code smh
 */

public class GeocodingHelper{
    private static String API_KEY = BaseApplication.getContext().getString(R.string.geocode_api_key);
    private static String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?key=" + API_KEY + "&address=";

    private final static String STATUS_OK = "OK";
    private final static String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
    private final static String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    private final static String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
    private final static String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
    private final static String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";

    private GeocodingResultCallback callback;
    private Context context;

    public GeocodingHelper(GeocodingResultCallback callback, Context context){
        this.callback = callback;
        this.context = context;
    }

    public void requestLatlngFromSearch(String address){
        try {
            String query = URLEncoder.encode(address, "utf-8");
            String url = BASE_URL + query;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()){
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            parseJson(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void parseJson(JSONObject json){
        try {
            String status = json.getString("status");
            String toastMessage = "";
            LatLng toReturn = null;
            switch (status){
                case STATUS_OK:
                    JSONArray result = json.getJSONArray("results");
                    JSONObject location = result.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");

                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    if (callback != null) {
                        toastMessage = context.getResources().getString(R.string.search_result_ok);
                        toReturn = new LatLng(lat, lng);
                    }
                    break;
                case STATUS_ZERO_RESULTS:
                    toastMessage = context.getResources().getString(R.string.no_search_results);
                    break;
                case STATUS_UNKNOWN_ERROR:
                    toastMessage = context.getResources().getString(R.string.unknown_search_error);
                    break;
                case STATUS_OVER_QUERY_LIMIT:
                case STATUS_REQUEST_DENIED:
                case STATUS_INVALID_REQUEST:
                    toastMessage = context.getResources().getString(R.string.geocode_other_error);
                    break;
            }

            goToUiThread(toReturn, toastMessage);
        } catch (JSONException e) {
            goToUiThread(null, context.getResources().getString(R.string.geocode_other_error));
            e.printStackTrace();
        }
    }

    private void goToUiThread(final LatLng latlng, final String toastMsg){
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.gotLatlng(latlng, toastMsg);
            }
        });
    }

    public interface GeocodingResultCallback{
        void gotLatlng(LatLng latLng, String resultMsg);
    }
}
