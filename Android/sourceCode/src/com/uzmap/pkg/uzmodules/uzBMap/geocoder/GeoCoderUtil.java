/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.geocoder;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class GeoCoderUtil implements OnGetGeoCoderResultListener {
	private GeoCoderInterface mInterface;
	private GeoCoder mGeoCoder = null;

	public GeoCoderUtil(GeoCoderInterface mInterface) {
		this.mInterface = mInterface;
		mGeoCoder = GeoCoder.newInstance();
		mGeoCoder.setOnGetGeoCodeResultListener(this);
	}

	public void coord2address(float lat, float lon) {
		LatLng latLng = new LatLng(lat, lon);
		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
	}

	public void address2coord(String city, String address) {
		mGeoCoder.geocode(new GeoCodeOption().city(city).address(address));
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		mInterface.onGetGeoCodeResult(result);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		mInterface.onGetReverseGeoCodeResult(result);
	}

}
