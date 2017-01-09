/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapAnimationOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapBusLine;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapDrawRoute;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapEventListener;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGPSSignal;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGeoCoder;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGeometry;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGoogleCoords;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapLocation;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOffLine;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOpen;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapPoi;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSearchRoute;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSetcenter;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSimple;
import com.uzmap.pkg.uzmodules.uzBMap.mode.Annotation;
import com.uzmap.pkg.uzmodules.uzBMap.mode.MoveOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class UzBMap extends UZModule {
	public static boolean INITIALIZED = false;
	private MapOpen mMap;
	private MapLocation mLocation;
	private MapOverlay mMapOverlay;
	private MapGeometry mMapGeometry;
	private Map<Integer, MapSearchRoute> mSearchRouteMap;
	private Map<Integer, OverlayManager> mRouteMap;
	private Map<Integer, BusLineOverlay> mBusLineMap;
	private MapAnimationOverlay mMapAnimationOverlay;
	private MapGPSSignal mMapGPSSignal;
	private MapOffLine mOffLine;

	public UzBMap(UZWebView webView) {
		super(webView);
		SDKInitializer.initialize(mContext.getApplication());
		INITIALIZED = true;
	}

	public void jsmethod_open(UZModuleContext moduleContext) {
		if (mMap == null) {
			init();
			mMap = new MapOpen(this, moduleContext, mContext);
			mMap.open();
			new MapEventListener(moduleContext, mMap, false)
					.addDefaultEventListener();
		} else {
			if (mMap != null) {
				mMap.show();
			}
		}
	}

	public void jsmethod_close(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.close();
			mMap = null;
			mMapOverlay = null;
			mMapGeometry = null;
		}
		if (mMapAnimationOverlay != null) {
			mMapAnimationOverlay.stop();
		}
	}

	public void jsmethod_show(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.show();
		}
	}

	public void jsmethod_hide(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.hide();
		}
	}

	public void jsmethod_setRect(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setRect(moduleContext);
		}
	}

	public void jsmethod_getLocation(UZModuleContext moduleContext) {
		init();
		if (mLocation != null) {
			mLocation.stopLocation();
		}
		mLocation = new MapLocation(moduleContext, mContext);
		mLocation.startLocation();
	}

	public void jsmethod_getCurrentLocation(UZModuleContext moduleContext) {
		if (mMap != null) {
			BDLocation loc = mMap.getCurLoc();
			getCurLocation(moduleContext, loc);
		}
	}

	public void jsmethod_stopLocation(UZModuleContext moduleContext) {
		if (mLocation != null) {
			mLocation.stopLocation();
		}
	}

	public void jsmethod_getCoordsFromName(UZModuleContext moduleContext) {
		init();
		new MapGeoCoder(moduleContext).address2coord();
	}

	public void jsmethod_getNameFromCoords(UZModuleContext moduleContext) {
		init();
		new MapGeoCoder(moduleContext).coord2address();
	}

	public void jsmethod_getDistance(UZModuleContext moduleContext) {
		init();
		new MapSimple(moduleContext).getDistance();
	}

	public void jsmethod_showUserLocation(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.showUserLocation(moduleContext);
		}
	}

	public void jsmethod_setCenter(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapSetcenter(moduleContext, mContext, mMap).setCenter();
		}
	}

	public void jsmethod_getCenter(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapSimple(moduleContext).getCenter(mMap.getMapView().getMap());
		}
	}

	public void jsmethod_setZoomLevel(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setZoomLevel(JsParamsUtil.getInstance().level(moduleContext));
		}
	}

	public void jsmethod_getZoomLevel(UZModuleContext moduleContext) {
		if (mMap != null) {
			getZoomCallBack(moduleContext, mMap.getZoomLevel());
		}
	}

	public void jsmethod_setMapAttr(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setMapType(jsParamsUtil.mapType(moduleContext));
			mMap.setZoomEnable(jsParamsUtil.zoomEnable(moduleContext));
			mMap.setScrollEnable(jsParamsUtil.scrollEnable(moduleContext));
		}
	}

	public void jsmethod_setRotation(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setRotation(jsParamsUtil.rotateDegree(moduleContext));
		}
	}

	public void jsmethod_setOverlook(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setOverlook(jsParamsUtil.overlookDegree(moduleContext));
		}
	}

	public void jsmethod_setScaleBar(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setScaleBar(moduleContext);
		}
	}

	public void jsmethod_setCompass(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setCompassPosition(moduleContext);
		}
	}

	public void jsmethod_setTraffic(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean traffic = moduleContext.optBoolean("traffic", true);
			mMap.getBaiduMap().setTrafficEnabled(traffic);
		}
	}

	public void jsmethod_setHeatMap(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean heatMap = moduleContext.optBoolean("heatMap", true);
			if (heatMap) {
				mMap.addHeatMap();
			} else {
				mMap.removeHeatMap();
			}
		}
	}

	public void jsmethod_setBuilding(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean building = moduleContext.optBoolean("building", true);
			mMap.setBuilding(building);
		}
	}

	public void jsmethod_setRegion(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setRegion(moduleContext);
		}
	}

	public void jsmethod_getRegion(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.getRegion(moduleContext);
		}
	}

	public void jsmethod_transCoords(UZModuleContext moduleContext) {
		new MapGoogleCoords(moduleContext, this).translateCoord();
	}

	public void jsmethod_zoomIn(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.zoomIn();
		}
	}

	public void jsmethod_zoomOut(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.zoomOut();
		}
	}

	public void jsmethod_addEventListener(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapEventListener(moduleContext, mMap, true).addEventListener();
		}
	}

	public void jsmethod_removeEventListener(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapEventListener(moduleContext, mMap, false).addEventListener();
		}
	}

	public void jsmethod_startSearchGPS(UZModuleContext moduleContext) {
		if (mMapGPSSignal == null) {
			mMapGPSSignal = new MapGPSSignal();
		}
		mMapGPSSignal.getGPSSnr(moduleContext, mContext);
	}

	public void jsmethod_stopSearchGPS(UZModuleContext moduleContext) {
		if (mMapGPSSignal != null) {
			mMapGPSSignal.stop();
		}
	}

	public void jsmethod_isPolygonContantsPoint(UZModuleContext moduleContext) {
		init();
		new MapSimple(moduleContext).isPolygonContantsPoint();
	}

	public void jsmethod_addAnnotations(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.addAnnotations(moduleContext);
		}
	}

	public void jsmethod_addMobileAnnotations(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.addMobileAnnotations(moduleContext);
		}
	}

	public void jsmethod_moveAnnotation(UZModuleContext moduleContext) {
		if (mMapOverlay != null) {
			Map<Integer, Annotation> markerMap = mMapOverlay.getMoveMarkerMap();
			int id = moduleContext.optInt("id");
			Annotation anno = markerMap.get(id);
			if (anno != null) {
				JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
				float lat = jsParamsUtil.lat(moduleContext, "end");
				float lon = jsParamsUtil.lon(moduleContext, "end");
				double duration = moduleContext.optDouble("duration");
				if (mMapAnimationOverlay == null) {
					mMapAnimationOverlay = new MapAnimationOverlay();
				}
				mMapAnimationOverlay.addMoveOverlay(new MoveOverlay(
						moduleContext, anno.getMarker(), duration, new LatLng(
								lat, lon)));
				mMapAnimationOverlay.startMove();
			}
		}
	}

	public void jsmethod_getAnnotationCoords(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.getAnnotationCoords(moduleContext);
			}
		}
	}

	public void jsmethod_setAnnotationCoords(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.setAnnotationCoords(moduleContext);
			}
		}
	}

	public void jsmethod_annotationExist(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				if (!moduleContext.isNull("id")) {
					mMapOverlay.isAnnotationExist(moduleContext);
				}
			} else {
				isAnnoExistCallBack(moduleContext, false);
			}
		}
	}

	public void jsmethod_setBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.setBubble(moduleContext);
		}
	}

	public void jsmethod_popupBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.popupBubble(moduleContext);
			}
		}
	}

	public void jsmethod_closeBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.closeBubble(moduleContext);
			}
		}
	}

	public void jsmethod_addBillboard(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.addBillboard(moduleContext);
		}
	}

	public void jsmethod_removeAnnotations(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.removeOverlay(moduleContext);
			}
		}
	}

	public void jsmethod_addLine(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addLine(moduleContext);
		}
	}

	public void jsmethod_addPolygon(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addPolygon(moduleContext);
		}
	}

	public void jsmethod_addArc(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addArc(moduleContext);
		}
	}

	public void jsmethod_addCircle(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addCircle(moduleContext);
		}
	}

	public void jsmethod_addImg(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addImg(moduleContext);
		}
	}

	public void jsmethod_removeOverlay(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry != null) {
				mMapGeometry.removeOverlays(moduleContext);
			}
		}
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_searchRoute(UZModuleContext moduleContext) {
		init();
		if (mSearchRouteMap == null) {
			mSearchRouteMap = new HashMap<Integer, MapSearchRoute>();
		}
		MapSearchRoute mapSearchRoute = new MapSearchRoute(moduleContext);
		mapSearchRoute.searchRoute();
		mSearchRouteMap.put(mapSearchRoute.getSearchID(), mapSearchRoute);
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_drawRoute(UZModuleContext moduleContext) {
		if (mMap != null && mSearchRouteMap != null) {
			if (mRouteMap == null) {
				mRouteMap = new HashMap<Integer, OverlayManager>();
			}
			int id = moduleContext.optInt("id");
			OverlayManager routeOverlay = new MapDrawRoute(this, moduleContext,
					mSearchRouteMap, mMap.getBaiduMap())
					.drawRoute(moduleContext);
			mRouteMap.put(id, routeOverlay);
		}
	}

	public void jsmethod_removeRoute(UZModuleContext moduleContext) {
		if (mMap != null && mRouteMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(
					moduleContext);
			if (ids != null && ids.size() > 0) {
				OverlayManager routeOverlay = null;
				for (int id : ids) {
					routeOverlay = mRouteMap.get(id);
					if (routeOverlay != null) {
						routeOverlay.removeFromMap();
					}
				}
			}
		}
	}

	public void jsmethod_searchBusRoute(UZModuleContext moduleContext) {
		init();
		new MapBusLine().busLine(moduleContext);
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_drawBusRoute(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mBusLineMap == null) {
				mBusLineMap = new HashMap<Integer, BusLineOverlay>();
			}
			new MapBusLine().drawBusLine(moduleContext, mBusLineMap,
					mMap.getBaiduMap());
		}
	}

	public void jsmethod_removeBusRoute(UZModuleContext moduleContext) {
		if (mBusLineMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(
					moduleContext);
			if (ids != null && ids.size() > 0) {
				OverlayManager routeOverlay = null;
				for (int id : ids) {
					routeOverlay = mBusLineMap.get(id);
					if (routeOverlay != null) {
						routeOverlay.removeFromMap();
					}
				}
			}
		}
	}

	public void jsmethod_searchInCity(UZModuleContext moduleContext) {
		init();
		new MapPoi().searchInCity(moduleContext);
	}

	public void jsmethod_searchNearby(UZModuleContext moduleContext) {
		init();
		new MapPoi().searchNearby(moduleContext);
	}

	public void jsmethod_searchInBounds(UZModuleContext moduleContext) {
		init();
		new MapPoi().searchInBounds(moduleContext);
	}

	public void jsmethod_autocomplete(UZModuleContext moduleContext) {
		init();
		new MapPoi().autoComplete(moduleContext);
	}

	public void jsmethod_getHotCityList(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.getHotCityList(moduleContext);
	}

	public void jsmethod_getOfflineCityList(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.getOfflineCityList(moduleContext);
	}

	public void jsmethod_searchCityByName(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.searchCityByName(moduleContext);
	}

	public void jsmethod_getAllUpdateInfo(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.getAllUpdateInfo(moduleContext);
	}

	public void jsmethod_getUpdateInfoByID(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.getUpdateInfoByID(moduleContext);
	}

	public void jsmethod_start(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.startDownload(moduleContext);
	}

	public void jsmethod_update(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.updateOffLine(moduleContext);
	}

	public void jsmethod_pause(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.pauseDownload(moduleContext);
	}

	public void jsmethod_remove(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.removeDownload(moduleContext);
	}

	public void jsmethod_addOfflineListener(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.addOfflineListener(moduleContext);
	}

	public void jsmethod_removeOfflineListener(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(mContext);
		}
		mOffLine.removeOfflineListener();
	}

	public void jsmethod_getLocationServices(UZModuleContext moduleContext) {
		boolean status = isLocationOPen();
		getLocationPermissionCallBack(moduleContext, status);
	}

	private boolean isLocationOPen() {
		LocationManager locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gps;
	}

	@Override
	protected void onClean() {
		super.onClean();
		cleanMap();
		cleanLocation();
	}

	private void cleanMap() {
		if (mMap != null) {
			mMap.onDestory();
			mMap = null;
		}
	}

	private void cleanLocation() {
		if (mLocation != null) {
			mLocation.onDestory();
			mLocation = null;
		}
	}

	private void init() {
		if (!INITIALIZED) {
			SDKInitializer.initialize(mContext.getApplication());
			INITIALIZED = true;
		}
	}

	// 解决当openWin，fixed为false时，部分手机地图不显示问题
	@SuppressLint("NewApi")
	private void makeMapShowSometimes() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View view = (View) mMap.getMapView().getParent();
				if (view != null) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			}
		});
	}

	private void isAnnoExistCallBack(UZModuleContext moduleContext,
			boolean status) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", status);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getZoomCallBack(UZModuleContext moduleContext, float level) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("level", level);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getLocationPermissionCallBack(UZModuleContext moduleContext,
			boolean enable) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("enable", enable);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getCurLocation(UZModuleContext moduleContext, BDLocation loc) {
		JSONObject ret = new JSONObject();
		try {
			if (loc != null) {
				ret.put("status", true);
				ret.put("lon", loc.getLongitude());
				ret.put("lat", loc.getLatitude());
			} else {
				ret.put("status", false);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
