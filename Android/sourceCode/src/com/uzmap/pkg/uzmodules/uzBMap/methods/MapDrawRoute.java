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
import android.text.TextUtils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.MassTransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.DrivingRouteOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.MassTransitRouteOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.OverlayManager;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.RidingRouteOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.TransitRouteOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.WalkingRouteOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapDrawRoute {
	private UZModuleContext mModuleContext;
	private Map<Integer, MapSearchRoute> mSearchRouteMap;
	private UzBMap mBMap;
	private BaiduMap mBaiduMap;
	private JsParamsUtil mJsParamsUtil;
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
		mRouteId = moduleContext.optInt("id");
		MapSearchRoute mapSearchRoute = mSearchRouteMap.get(mRouteId);
		if (mapSearchRoute != null) {
			String type = mapSearchRoute.getSearchType();
			if (type.equals("drive")) {
				return drawDriveRoute(mapSearchRoute, moduleContext);
			} else if (type.equals("transit")) {
				return drawMassBusRoute(mapSearchRoute, moduleContext);
				//return drawBusRoute(mapSearchRoute, moduleContext);
			} else if (TextUtils.equals(type, "walk")){
				return drawWalkRoute(mapSearchRoute, moduleContext);
			} else if (TextUtils.equals(type, "riding")) {
				return drawRidingRoute(mapSearchRoute, moduleContext);
			}
		}
		return null;
	}

	private OverlayManager drawDriveRoute(MapSearchRoute mapSearchRoute, UZModuleContext moduleContext) {
		List<DrivingRouteLine> carPlans = mapSearchRoute.getCarPlans();
		if (carPlans != null && carPlans.size() > 0) {
			UzDrivingRouteOverlay routeOverlay = new UzDrivingRouteOverlay(mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < carPlans.size()) {
				routeOverlay.setData(carPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				if (moduleContext.optBoolean("autoresizing", true)) {
					routeOverlay.zoomToSpan();
				}
				return routeOverlay;
			}
		}
		return null;
	}

	private OverlayManager drawBusRoute(MapSearchRoute mapSearchRoute, UZModuleContext moduleContext) {
		List<TransitRouteLine> mBusPlans = mapSearchRoute.getBusPlans();
		if (mBusPlans != null && mBusPlans.size() > 0) {
			UzTransitRouteOverlay routeOverlay = new UzTransitRouteOverlay(mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < mBusPlans.size()) {
				routeOverlay.setData(mBusPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				if (moduleContext.optBoolean("autoresizing", true)) {
					routeOverlay.zoomToSpan();
				}
				return routeOverlay;
			}
		}
		return null;
	}
	
	private OverlayManager drawMassBusRoute(MapSearchRoute mapSearchRoute, UZModuleContext moduleContext) {
		List<MassTransitRouteLine> mBusPlans = mapSearchRoute.getMassTransitPlans();
		if (mBusPlans != null && mBusPlans.size() > 0) {
			UzMassTransitRouteOverlay routeOverlay = new UzMassTransitRouteOverlay(mBaiduMap, mapSearchRoute.isSameCity());
			int index = moduleContext.optInt("index");
			if (index < mBusPlans.size()) {
				routeOverlay.setData(mBusPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				if (moduleContext.optBoolean("autoresizing", true)) {
					routeOverlay.zoomToSpan();
				}
				return routeOverlay;
			}
		}
		return null;
	}

	private OverlayManager drawWalkRoute(MapSearchRoute mapSearchRoute, UZModuleContext moduleContext) {
		List<WalkingRouteLine> walkPlans = mapSearchRoute.getWalkPlans();
		if (walkPlans != null && walkPlans.size() > 0) {
			UzWalkingRouteOverlay routeOverlay = new UzWalkingRouteOverlay(mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < walkPlans.size()) {
				routeOverlay.setData(walkPlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				if (moduleContext.optBoolean("autoresizing", true)) {
					routeOverlay.zoomToSpan();
				}
				return routeOverlay;
			}
		}
		return null;
	}
	
	private OverlayManager drawRidingRoute(MapSearchRoute mapSearchRoute, UZModuleContext moduleContext) {
		List<BikingRouteLine> bikePlans = mapSearchRoute.getBikePlans();
		if (bikePlans != null && bikePlans.size() > 0) {
			UzBikingRouteOverlay routeOverlay = new UzBikingRouteOverlay(mBaiduMap);
			int index = moduleContext.optInt("index");
			if (index < bikePlans.size()) {
				routeOverlay.setData(bikePlans.get(index));
				mBaiduMap.setOnMarkerClickListener(routeOverlay);
				routeOverlay.addToMap();
				if (moduleContext.optBoolean("autoresizing", true)) {
					routeOverlay.zoomToSpan();
				}
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
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "start");
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "end");
		}
		
		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}
		
		@Override
		public boolean showStart() {
			return mJsParamsUtil.showNode(mModuleContext, "start");
		}
		
		@Override
		public boolean showEnd() {
			return mJsParamsUtil.showNode(mModuleContext, "end");
		}
		
		@Override
		public BitmapDescriptor getNodeMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, false, "node", "busNode");
		}
		
		@Override
		public int getLineColor() {
			return mJsParamsUtil.getLineColor(mModuleContext, false, "line", "busLine");
		}
		
		@Override
		public int lineWidth() {
			return UZUtility.dipToPix(mJsParamsUtil.lineWidth(mModuleContext, false, "line", "busLine"));
		}
		
		@Override
		public boolean dash() {
			return mJsParamsUtil.dash(mModuleContext, false, "line", "busLine");
		}
		
		@Override
		public BitmapDescriptor getTextureImg() {
			return mJsParamsUtil.getTextureImg(mModuleContext, false, "line", "busLine");
		}
	}

	private class UzWalkingRouteOverlay extends WalkingRouteOverlay {

		public UzWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "start");
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "end");
		}
		
		@Override
		public BitmapDescriptor getNodeMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, false, "node", "busNode");
		}
		
		@Override
		public int getLineColor() {
			return mJsParamsUtil.getLineColor(mModuleContext, false, "line", "");
		}
		
		@Override
		public int getLineWidth() {
			return UZUtility.dipToPix(mJsParamsUtil.lineWidth(mModuleContext, false, "line", ""));
		}
		
		@Override
		public boolean dash() {
			return mJsParamsUtil.dash(mModuleContext, false, "line", "");
		}
		
		@Override
		public BitmapDescriptor getTextureImg() {
			return mJsParamsUtil.getTextureImg(mModuleContext, false, "line", "");
		}

		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}

	}
	
	private class UzBikingRouteOverlay extends RidingRouteOverlay{
		public UzBikingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "start");
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "end");
		}
		
		@Override
		public BitmapDescriptor getNodeMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, false, "node", "busNode");
		}
		
		@Override
		public int getLineColor() {
			return mJsParamsUtil.getLineColor(mModuleContext, false, "line", "");
		}
		
		@Override
		public int getLineWidth() {
			return UZUtility.dipToPix(mJsParamsUtil.lineWidth(mModuleContext, false, "line", ""));
		}
		
		@Override
		public boolean dash() {
			return mJsParamsUtil.dash(mModuleContext, false, "line", "");
		}
		
		@Override
		public BitmapDescriptor getTextureImg() {
			return mJsParamsUtil.getTextureImg(mModuleContext, false, "line", "");
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
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "start");
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "end");
		}

		@Override
		public boolean onRouteNodeClick(int index) {
			nodeClickBack(index);
			return true;
		}
	}
	
	private class UzMassTransitRouteOverlay extends MassTransitRouteOverlay {

		private boolean isSameCity;
		public UzMassTransitRouteOverlay(BaiduMap baiduMap, boolean isSameCity) {
			super(baiduMap);
			this.isSameCity = isSameCity;
		}
		
		@Override
		public boolean isSameCity() {
			return isSameCity;
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "start");
		}
		
		@Override
		public BitmapDescriptor getBusMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, true, "node", "busNode");
		}
		
		@Override
		public BitmapDescriptor getTrainMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, true, "node", "subwayNode");
		}
		
		@Override
		public BitmapDescriptor getWalkMarker() {
			return mJsParamsUtil.getNodeMarker(mModuleContext, true, "node", "walkNode");
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return mJsParamsUtil.getStartOrEndMarker(mModuleContext, "end");
		}
		
		@Override
		public int getLineColor(String node) {
			return mJsParamsUtil.getLineColor(mModuleContext, true, "line", node);
		}
		
		@Override
		public int getLineWidth(String node) {
			return UZUtility.dipToPix(mJsParamsUtil.lineWidth(mModuleContext, true, "line", node));
		}
		
		@Override
		public boolean dash(String node) {
			return mJsParamsUtil.dash(mModuleContext, true, "line", node);
		}
		
		@Override
		public BitmapDescriptor getTextureImg(String node) {
			return mJsParamsUtil.getTextureImg(mModuleContext, true, "line", node);
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
