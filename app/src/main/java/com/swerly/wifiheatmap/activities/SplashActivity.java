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

package com.swerly.wifiheatmap.activities;

import android.content.Intent;
import android.os.Bundle;

import com.swerly.wifiheatmap.utils.CacheHelper;
import com.swerly.wifiheatmap.data.HeatmapData;
import com.swerly.wifiheatmap.data.HeatmapPixelCacheObject;
import com.swerly.wifiheatmap.utils.LoadCacheTask;

import java.util.ArrayList;

/**
 * Created by Seth on 8/13/2017.
 */

public class SplashActivity extends ActivityBase implements LoadCacheTask.CacheLoadCallbacks {
    private boolean listLoaded;

    private CacheHelper cacheHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cacheHelper = new CacheHelper(this, this);
        cacheHelper.startupLoad();
    }

    @Override
    public void heatmapListLoaded(ArrayList<HeatmapData> data) {
        listLoaded = true;
        app.setLoadedList(data);
        checkIfLoaded();
    }

    @Override
    public void heatmapPixelsLoaded(HeatmapPixelCacheObject pixels) {
        //dont need to load pixels until the user wants to edit a map
    }

    private void checkIfLoaded(){
        if (listLoaded){
            Intent intent = new Intent(this, ActivityMain.class);
            startActivity(intent);
            finish();
        }
    }
}
