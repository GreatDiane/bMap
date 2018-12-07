/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationInterface;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapLocation implements LocationInterface {
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;
	private LocationUtil mLocationUtil;
	private BDLocation mCurrentLocation;

	public MapLocation(UZModuleContext mModuleContext, Context mContext) {
		this.mModuleContext = mModuleContext;
		mJsParamsUtil = JsParamsUtil.getInstance();
		mLocationUtil = new LocationUtil(mContext, mModuleContext, this);
	}

	public void startLocation() {
		mLocationUtil.startLocation();
	}

	public void stopLocation() {
		mLocationUtil.stopLocation();
	}

	public void getLocation(UZModuleContext mModuleContext) {
		callBack(mModuleContext, mCurrentLocation);
	}

	@Override
	public void onReceive(BDLocation location) {
		mCurrentLocation = location;
		callBack(mModuleContext, location);
		if (isStopLocation()) {
			mLocationUtil.stopLocation();
		}
	}

	private boolean isStopLocation() {
		return mJsParamsUtil.autoStop(mModuleContext);
	}

	private void callBack(UZModuleContext moduleContext, BDLocation location) {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		try {
			if (location != null) {
				if (location.getLocType() == BDLocation.TypeGpsLocation
						|| location.getLocType() == BDLocation.TypeNetWorkLocation
						|| location.getLocType() == BDLocation.TypeOffLineLocation
						|| location.getLocType() == BDLocation.TypeCacheLocation) {
					ret.put("status", true);
					ret.put("timestamp", dateTimeStringToMillis(location.getTime(), formatDateTimeE));
					ret.put("lon", location.getLongitude());
					ret.put("lat", location.getLatitude());
					ret.put("locationType", getLocType(location));
					moduleContext.success(ret, false);
				} else {
					ret.put("status", false);
					err.put("code", location.getLocType());
					moduleContext.error(ret, err, false);
				}
			} else {
				ret.put("status", false);
				err.put("msg", -1);
				moduleContext.error(ret, err, false);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private static final String formatDateTimeE = "yyyy-MM-dd HH:mm:ss";
	private final long dateTimeStringToMillis(String str, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        long res = 0;
        try {
            Date date = dateFormat.parse(str);
            res = date.getTime();
        } catch (Exception e) {
            res = 0;
        }
        return res;
    }

	private String getLocType(BDLocation location) {
		String locType = "NetWork";
		if (location.getLocType() == BDLocation.TypeGpsLocation) {
			locType = "GPS";
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
			locType = "NetWork";
		} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
			locType = "OffLine";
		} else if (location.getLocType() == BDLocation.TypeCacheLocation) {
			locType = "Cache";
		}
		return locType;
	}

	public void onDestory() {
		mLocationUtil.onDestory();
	}
}
