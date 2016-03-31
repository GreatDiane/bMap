/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapSimple {
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;

	public MapSimple(UZModuleContext mModuleContext) {
		this.mModuleContext = mModuleContext;
		this.mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void getDistance() {
		float startLat = mJsParamsUtil.lat(mModuleContext, "start");
		float startLon = mJsParamsUtil.lon(mModuleContext, "start");
		float endLat = mJsParamsUtil.lat(mModuleContext, "end");
		float endLon = mJsParamsUtil.lon(mModuleContext, "end");
		double distance = DistanceUtil.getDistance(new LatLng(startLat,
				startLon), new LatLng(endLat, endLon));
		getDistanceBack(distance);
	}

	private void getDistanceBack(double distance) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			ret.put("distance", distance);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
