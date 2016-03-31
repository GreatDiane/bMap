/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

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
		mLocationUtil = new LocationUtil(mContext, this);
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
						|| location.getLocType() == BDLocation.TypeOffLineLocation) {
					ret.put("status", true);
					ret.put("timestamp", location.getTime());
					ret.put("lon", location.getLongitude());
					ret.put("lat", location.getLatitude());
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
	
	public void onDestory() {
		mLocationUtil.onDestory();
	}
}
