/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.location;

import android.content.Context;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationUtil implements LocationInterface {
	private LocationClient mLocClient;
	private UzLocationListenner mListener;
	private Context mContext;
	private LocationInterface mInterface;

	public LocationUtil(Context mContext, LocationInterface mInterface) {
		this.mContext = mContext;
		this.mInterface = mInterface;
		initLocationClient();
	}

	public void startLocation() {
		mLocClient.start();
	}

	public void stopLocation() {
		mLocClient.stop();
	}

	private void initLocationClient() {
		mListener = new UzLocationListenner(this);
		mLocClient = new LocationClient(mContext);
		mLocClient.registerLocationListener(mListener);
		mLocClient.setLocOption(locationOption());
	}

	private LocationClientOption locationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		return option;
	}

	@Override
	public void onReceive(BDLocation location) {
		mInterface.onReceive(location);
	}

	public void onDestory() {
		if (mLocClient != null) {
			mLocClient.stop();
			mLocClient = null;
		}
	}

}
