/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.location;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class UzLocationListenner implements BDLocationListener {

	private LocationInterface mInterface;
	
	public UzLocationListenner(LocationInterface mInterface) {
		this.mInterface = mInterface;
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		mInterface.onReceive(location);
	}

	public void onReceivePoi(BDLocation poiLocation) {
	}
}
