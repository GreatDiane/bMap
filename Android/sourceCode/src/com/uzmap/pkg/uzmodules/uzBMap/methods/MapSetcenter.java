/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import android.content.Context;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.geocoder.GeoCoderInterface;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationInterface;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapSetcenter implements GeoCoderInterface, LocationInterface {
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;
	private MapOpen mMap;
	private LocationUtil mLocationUtil;

	public MapSetcenter(UZModuleContext mModuleContext, Context mContext,
			MapOpen mMap) {
		this.mModuleContext = mModuleContext;
		this.mMap = mMap;
		mLocationUtil = new LocationUtil(mContext, mModuleContext, this);
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void setCenter() {
		float lon = mJsParamsUtil.lon(mModuleContext, "coords");
		float lat = mJsParamsUtil.lat(mModuleContext, "coords");
		if (lon != 0 && lat != 0) {
			setCenterAndLevel(lon, lat);
		}
	}

	private void setCenterAndLevel(double centerLon, double centerLat) {
		boolean isAnimation = mModuleContext.optBoolean("animation",true);
		mMap.setCenter(centerLon, centerLat,isAnimation);
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result != null && result.error == SearchResult.ERRORNO.NO_ERROR) {
			LatLng latLng = result.getLocation();
			setCenterAndLevel(latLng.longitude, latLng.latitude);
		}
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

	}

	@Override
	public void onReceive(BDLocation location) {
		if (isLocationSuccess(location)) {
			setCenterAndLevel(location.getLongitude(), location.getLatitude());
			mLocationUtil.onDestory();
		}
	}

	private boolean isLocationSuccess(BDLocation location) {
		if (location != null) {
			if (location.getLocType() == BDLocation.TypeGpsLocation
					|| location.getLocType() == BDLocation.TypeNetWorkLocation
					|| location.getLocType() == BDLocation.TypeOffLineLocation) {
				return true;
			}
		}
		return false;
	}
}
