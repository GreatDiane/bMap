/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.utils;

import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class CallBackUtil {

	public void openCallBack(UZModuleContext moduleContext) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getRegionCallBack(UZModuleContext moduleContext,
			LatLng pointLeft, LatLng pointRight) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			ret.put("lbLon", pointLeft.longitude);
			ret.put("lbLat", pointLeft.latitude);
			ret.put("rtLon", pointRight.longitude);
			ret.put("rtLat", pointRight.latitude);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getAnnoCoordsCallBack(UZModuleContext moduleContext,
			LatLng point) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("lon", point.longitude);
			ret.put("lat", point.latitude);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
