/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapDrawRoute {
	private UZModuleContext mModuleContext;
	private Map<Integer, MapSearchRoute> mSearchRouteMap;
	private UzBMap mBMap;
	private BaiduMap mBaiduMap;
	private JsParamsUtil mJsParamsUtil;
	private Bitmap mStartImg;
	private Bitmap mEndImg;
	private int mRouteId;

	public MapDrawRoute(UzBMap bMap, UZModuleContext moduleContext,
			Map<Integer, MapSearchRoute> searchRouteMap, BaiduMap baiduMap) {
		this.mBMap = bMap;
		this.mModuleContext = moduleContext;
		this.mBaiduMap = baiduMap;
		this.mSearchRouteMap = searchRouteMap;
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public OverlayManager drawRoute(UZModuleContext moduleContext) {
		mStartImg = mJsParamsUtil.nodeIcon(moduleContext, mBMap, "start");
		mEndImg = mJsParamsUtil.nodeIcon(moduleContext, mBMap, "end");
		mRouteId = moduleContext.optInt("id");
		MapSearchRoute mapSearchRoute = mSearchRouteMap.get(mRouteId);
		if (mapSearchRoute != null) {
			String type = mapSearchRoute.getSearchType();
			if (type.equals("drive")) {
				return drawDriveRoute(mapSearchRoute, moduleContext);
			} else if (type.equals("transit")) {
				return drawBusRoute(mapSearchRoute, moduleContext);
			} else {
				return drawWalkRoute(mapSearchRoute, moduleContext);
			}
		}
		return null;
	}

	private OverlayManager drawDriveRoute(MapSearchRoute mapSearchRoute,
			UZModuleContext moduleContext) {
		List<DrivingRouteLine> carPlans = mapSearchRoute.getCarPlans();
		if (carPlans != null && carPlans.size() > 0) {
			UzDrivingRouteOverlay routeOverlay = new UzDrivingRouteOverlay(
					mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < carPlans.size()) {
				routeOverlay.setData(carPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
				return routeOverlay;
			}
		}
		return null;
	}

	private OverlayManager drawBusRoute(MapSearchRoute mapSearchRoute,
			UZModuleContext moduleContext) {
		List<TransitRouteLine> mBusPlans = mapSearchRoute.getBusPlans();
		if (mBusPlans != null && mBusPlans.size() > 0) {
			UzTransitRouteOverlay routeOverlay = new UzTransitRouteOverlay(
					mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < mBusPlans.size()) {
				routeOverlay.setData(mBusPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
				return routeOverlay;
			}
		}
		return null;
	}

	private OverlayManager drawWalkRoute(MapSearchRoute mapSearchRoute,
			UZModuleContext moduleContext) {
		List<WalkingRouteLine> walkPlans = mapSearchRoute.getWalkPlans();
		if (walkPlans != null && walkPlans.size() > 0) {
			UzWalkingRouteOverlay routeOverlay = new UzWalkingRouteOverlay(
					mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < walkPlans.size()) {
				routeOverlay.setData(walkPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				routeOverlay.zoomToSpan();
				return routeOverlay;
			}
		}
		return null;
	}

	private class UzDrivingRouteOverlay extends DrivingRouteOverlay {

		public UzDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (mStartImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mStartImg);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (mEndImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mEndImg);
			}
			return null;
		}

		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}
	}

	private class UzWalkingRouteOverlay extends WalkingRouteOverlay {

		public UzWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (mStartImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mStartImg);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (mEndImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mEndImg);
			}
			return null;
		}

		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}

	}

	private class UzTransitRouteOverlay extends TransitRouteOverlay {

		public UzTransitRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (mStartImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mStartImg);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (mEndImg != null) {
				return BitmapDescriptorFactory.fromBitmap(mEndImg);
			}
			return null;
		}

		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}
	}

	private void nodeClickBack(int index) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("nodeIndex", index);
			ret.put("routeId", mRouteId);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
