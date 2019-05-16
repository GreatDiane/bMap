/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.LocationManager;
import android.text.TextUtils;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap.OnBaseIndoorMapListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo.SwitchFloorError;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorInfo;
import com.baidu.mapapi.search.poi.PoiIndoorOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapAnimationOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapBusLine;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapCluster;
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
import com.uzmap.pkg.uzmodules.uzBMap.overlay.BusLineOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.overlay.OverlayManager;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class UzBMap extends UZModule {
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
	}

	public void jsmethod_openActivity(UZModuleContext moduleContext) {
		startActivity(new Intent(context(), MapTestActivity.class));
	}

	public void jsmethod_open(UZModuleContext moduleContext) {
		if (mMap == null) {
			mMap = new MapOpen(this, moduleContext, mode, configPath, context());
			mMap.open();
			new MapEventListener(moduleContext, mMap, false).addDefaultEventListener();
		} else {
			if (mMap != null) {
				mMap.show();
			}
		}
	}

	private String mode = "4";
	private String configPath;

	public void jsmethod_customStyle(UZModuleContext moduleContext) {
		if (!moduleContext.isNull("configPath")) {
			configPath = makeRealPath(moduleContext.optString("configPath"));
			mode = "0";
		} else {
			String customConfig = moduleContext.optString("customConfig", "night");
			if (TextUtils.equals(customConfig, "night")) {
				mode = "1";
			} else if (TextUtils.equals(customConfig, "lightblue")) {
				mode = "2";
			} else if (TextUtils.equals(customConfig, "midnightblue")) {
				mode = "3";
			} else {
				mode = "4";
			}
		}
	}

	/**
	 * 切换自定义地图
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_enableCustom(UZModuleContext moduleContext) {
		if (TextUtils.equals(mode, "4")) {
			return;
		}
		TextureMapView.setMapCustomEnable(moduleContext.optBoolean("enable", true));
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

	/**
	 * 截图
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_snapshotMap(final UZModuleContext moduleContext) {
		if (mMap != null) {
			final String path = moduleContext.optString("path");
			BaiduMap baiduMap = mMap.getBaiduMap();
			if (moduleContext.isNull("rect")) {
				baiduMap.snapshot(new SnapshotReadyCallback() {

					@Override
					public void onSnapshotReady(Bitmap bitmap) {
						saveBitmap(moduleContext, bitmap, path);
					}
				});
			}else {
				JSONObject rectJson = moduleContext.optJSONObject("rect");
				Rect rect = new Rect(UZUtility.dipToPix(rectJson.optInt("x")), UZUtility.dipToPix(rectJson.optInt("y")), UZUtility.dipToPix(rectJson.optInt("w")), UZUtility.dipToPix(rectJson.optInt("h")));
				baiduMap.snapshotScope(rect, new SnapshotReadyCallback() {
					
					@Override
					public void onSnapshotReady(Bitmap bitmap) {
						saveBitmap(moduleContext, bitmap, path);
					}
				});
			}
			
		}
	}

	public void saveBitmap(UZModuleContext moduleContext, Bitmap bitmap, String picName) {
		File f = new File(makeRealPath(picName));
		if (f.exists()) {
			f.delete();
		}
		JSONObject result = new JSONObject();
		try {
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			
			result.put("status", true);
			result.put("path", makeRealPath(picName));
			moduleContext.success(result, false);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				result.put("status", false);
				moduleContext.success(result, false);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			try {
				result.put("status", false);
				moduleContext.success(result, false);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			try {
				result.put("status", false);
				moduleContext.success(result, false);
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
		}
		
		
	}

	public void jsmethod_setRect(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setRect(moduleContext);
		}
	}

	public void jsmethod_getLocation(UZModuleContext moduleContext) {
		if (mLocation != null) {
			mLocation.stopLocation();
		}
		mLocation = new MapLocation(moduleContext, context());
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
		new MapGeoCoder(moduleContext).address2coord();
	}

	public void jsmethod_getNameFromCoords(UZModuleContext moduleContext) {
		new MapGeoCoder(moduleContext).coord2address();
	}

	public void jsmethod_getDistance(UZModuleContext moduleContext) {
		new MapSimple(moduleContext).getDistance();
	}

	public void jsmethod_showUserLocation(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.showUserLocation(moduleContext);
		}
	}

	public void jsmethod_setCenter(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapSetcenter(moduleContext, context(), mMap).setCenter();
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
			mMap.setRotateEnabled(jsParamsUtil.rotateEnabled(moduleContext));
			mMap.setOverlookEnabled(jsParamsUtil.overlookEnabled(moduleContext));
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
		mMapGPSSignal.getGPSSnr(moduleContext, context());
	}

	public void jsmethod_stopSearchGPS(UZModuleContext moduleContext) {
		if (mMapGPSSignal != null) {
			mMapGPSSignal.stop();
		}
	}

	public void jsmethod_isPolygonContantsPoint(UZModuleContext moduleContext) {
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
				mMapAnimationOverlay.addMoveOverlay(
						new MoveOverlay(moduleContext, anno.getMarker(), duration, new LatLng(lat, lon)));
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
			mMapOverlay.setBubble(moduleContext, false);
		}
	}

	/**
	 * 设置点击标注时弹出的气泡信息
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_setWebBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.setBubble(moduleContext, true);
		}
	}

	/**
	 * 添加网页气泡点击监听
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_addWebBubbleListener(UZModuleContext moduleContext) {
		BMapConfig.getInstance().setAddWebBubble(moduleContext);
	}

	/**
	 * 移除网页气泡点击监听
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_removeWebBubbleListener(UZModuleContext moduleContext) {
		BMapConfig.getInstance().setAddWebBubble(null);
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

	private MapCluster mMapCluster;

	/**
	 * 往地图上添加聚合点
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_addCluster(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapCluster == null) {
				mMapCluster = new MapCluster(this, mMap);
			}
			mMapCluster.addCluster(moduleContext);
		}
	}

	/**
	 * 移除本次添加的聚合点
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_removeCluster(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapCluster == null) {
				mMapCluster = new MapCluster(this, mMap);
			}
			mMapCluster.removeCluster();
		}
	}

	/**
	 * 添加聚合点点击事件的监听
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_addClusterListener(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapCluster == null) {
				mMapCluster = new MapCluster(this, mMap);
			}
			mMapCluster.addClusterListener(moduleContext);
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

	public void jsmethod_searchDistrict(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addDistrict(moduleContext);
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

	public void jsmethod_removeDistrict(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry != null) {
				mMapGeometry.removeDistrict(moduleContext);

			}
		}
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_searchRoute(UZModuleContext moduleContext) {
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
			OverlayManager routeOverlay = new MapDrawRoute(this, moduleContext, mSearchRouteMap, mMap.getBaiduMap())
					.drawRoute(moduleContext);
			mRouteMap.put(id, routeOverlay);
		}
	}

	public void jsmethod_removeRoute(UZModuleContext moduleContext) {
		if (mMap != null && mRouteMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(moduleContext);
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
		new MapBusLine().busLine(moduleContext);
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_drawBusRoute(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mBusLineMap == null) {
				mBusLineMap = new HashMap<Integer, BusLineOverlay>();
			}
			new MapBusLine().drawBusLine(moduleContext, mBusLineMap, mMap.getBaiduMap());
		}
	}

	public void jsmethod_removeBusRoute(UZModuleContext moduleContext) {
		if (mBusLineMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(moduleContext);
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
		new MapPoi().searchInCity(moduleContext);
	}

	public void jsmethod_searchNearby(UZModuleContext moduleContext) {
		new MapPoi().searchNearby(moduleContext);
	}

	public void jsmethod_searchInBounds(UZModuleContext moduleContext) {
		new MapPoi().searchInBounds(moduleContext);
	}

	public void jsmethod_autocomplete(UZModuleContext moduleContext) {
		new MapPoi().autoComplete(moduleContext);
	}

	public void jsmethod_getHotCityList(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.getHotCityList(moduleContext);
	}

	public void jsmethod_getOfflineCityList(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.getOfflineCityList(moduleContext);
	}

	public void jsmethod_searchCityByName(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.searchCityByName(moduleContext);
	}

	public void jsmethod_getAllUpdateInfo(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.getAllUpdateInfo(moduleContext);
	}

	public void jsmethod_getUpdateInfoByID(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.getUpdateInfoByID(moduleContext);
	}

	public void jsmethod_start(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.startDownload(moduleContext);
	}

	public void jsmethod_update(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.updateOffLine(moduleContext);
	}

	public void jsmethod_pause(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.pauseDownload(moduleContext);
	}

	public void jsmethod_remove(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.removeDownload(moduleContext);
	}

	public void jsmethod_addOfflineListener(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.addOfflineListener(moduleContext);
	}

	public void jsmethod_removeOfflineListener(UZModuleContext moduleContext) {
		if (mOffLine == null) {
			mOffLine = new MapOffLine(context());
		}
		mOffLine.removeOfflineListener();
	}

	public void jsmethod_getLocationServices(UZModuleContext moduleContext) {
		boolean status = isLocationOPen();
		getLocationPermissionCallBack(moduleContext, status);
	}

	/**
	 * @param moduleContext
	 * @see add by 2017年10月10日 12:28:02 控制底图标注的显示隐藏
	 */
	public void jsmethod_setShowMapPoi(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean showMapPoi = moduleContext.optBoolean("showMapPoi", true);
			mMap.getBaiduMap().showMapPoi(showMapPoi);
		}
	}

	/**
	 * @param moduleContext
	 * @see add by 2017年10月10日 12:28:02 设置最大缩放比例，取值范围：3-18级
	 */
	public void jsmethod_setMaxAndMinZoomLevel(UZModuleContext moduleContext) {
		if (mMap != null) {
			int maxLevel = moduleContext.optInt("maxLevel", 15);
			int minLevel = moduleContext.optInt("minLevel", 10);
			mMap.getBaiduMap().setMaxAndMinZoomLevel(maxLevel, minLevel);
		}
	}

	/**
	 * 打开关闭室内地图
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_setIndoorMap(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean draggable = moduleContext.optBoolean("draggable", true);
			mMap.getBaiduMap().setIndoorEnable(draggable);
		}
	}

	/**
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_addIndoorListener(final UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.getBaiduMap().setOnBaseIndoorMapListener(new OnBaseIndoorMapListener() {

				@Override
				public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
					try {
						JSONObject result = new JSONObject();
						result.put("enter", b);
						result.put("strID", mapBaseIndoorMapInfo.getID());
						result.put("strFloor", mapBaseIndoorMapInfo.getCurFloor());
						moduleContext.success(result, false);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 切换楼层
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_switchIndoorMapFloor(UZModuleContext moduleContext) {
		try {
			if (mMap != null) {
				String strID = moduleContext.optString("strID");
				String strFloor = moduleContext.optString("strFloor");
				if (!TextUtils.isEmpty(strID) && !TextUtils.isEmpty(strFloor)) {
					SwitchFloorError switchFloorError = mMap.getBaiduMap().switchBaseIndoorMapFloor(strID, strFloor);
					JSONObject success = new JSONObject();
					JSONObject error = new JSONObject();
					if (switchFloorError == SwitchFloorError.SWITCH_OK) {
						success.put("status", true);
					} else {
						success.put("status", false);
						if (switchFloorError == SwitchFloorError.FLOOR_INFO_ERROR) {
							error.put("code", 1);
						} else if (switchFloorError == SwitchFloorError.FLOOR_OVERLFLOW) {
							error.put("code", 3);
						} else if (switchFloorError == SwitchFloorError.FOCUSED_ID_ERROR) {
							error.put("code", 2);
						} else if (switchFloorError == SwitchFloorError.SWITCH_ERROR) {
							error.put("code", 1);
						}
					}
					moduleContext.error(success, success, false);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 搜索室内地图内容
	 * 
	 * @param moduleContext
	 */
	public void jsmethod_indoorSearch(final UZModuleContext moduleContext) {
		try {
			String strID = moduleContext.optString("strID");
			String keyword = moduleContext.optString("keyword");
			int pageIndex = moduleContext.optInt("pageIndex", 0);
			int pageCapacity = moduleContext.optInt("pageCapacity", 10);
			final PoiSearch poiSearch = PoiSearch.newInstance();
			poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {

				@Override
				public void onGetPoiResult(PoiResult poiResult) {

				}

				@Override
				public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
					try {
						JSONObject result = new JSONObject();
						JSONObject error = new JSONObject();
						int status = poiIndoorResult.status;

						if (status == 0) {
							result.put("status", true);
							result.put("pageNum", poiIndoorResult.getPageNum());
							result.put("totalPoiNum", poiIndoorResult.poiNum);
							List<PoiIndoorInfo> list = poiIndoorResult.getmArrayPoiInfo();
							if (list != null && list.size() > 0) {
								JSONArray array = new JSONArray();
								for (int i = 0; i < list.size(); i++) {
									PoiIndoorInfo info = list.get(i);
									JSONObject jsonInfo = new JSONObject();
									jsonInfo.put("name", info.name);
									jsonInfo.put("uid", info.uid);
									jsonInfo.put("indoorId", info.bid);
									jsonInfo.put("floor", info.floor);
									jsonInfo.put("address", info.address);
									jsonInfo.put("cid", info.cid);
									jsonInfo.put("phone", info.phone);
									LatLng latLng = info.latLng;
									JSONObject ptJson = new JSONObject();
									ptJson.put("latitude", latLng.latitude);
									ptJson.put("longtitude", latLng.longitude);
									jsonInfo.put("pt", ptJson);
									jsonInfo.put("tag", info.tag);
									jsonInfo.put("price", info.price);
									jsonInfo.put("starLevel", info.starLevel);
									jsonInfo.put("grouponFlag", info.isGroup);
									jsonInfo.put("takeoutFlag", info.isTakeOut);
									jsonInfo.put("waitedFlag", info.isWaited);
									jsonInfo.put("grouponNum", info.groupNum);
									array.put(jsonInfo);
								}
								result.put("poiIndoorInfoList", array);
							}
							moduleContext.error(result, error, false);
						} else {
							result.put("status", false);
							result.put("code", poiIndoorResult.error.name());
							moduleContext.error(result, error, false);
						}
						poiSearch.destroy();
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}

				@Override
				public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

				}
			});
			PoiIndoorOption option = new PoiIndoorOption().poiIndoorBid(strID).poiIndoorWd(keyword)
					.poiCurrentPage(pageIndex).poiPageSize(pageCapacity);
			poiSearch.searchPoiIndoor(option);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isLocationOPen() {
		LocationManager locationManager = (LocationManager) context().getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return gps;
	}

	@Override
	protected void onClean() {
		super.onClean();
		cleanMap();
		cleanLocation();
		mode = "4";
	}

	private void cleanMap() {
		if (mMap != null) {
			// mMap.onDestory();
			mMap.close();
			mMap = null;
		}
	}

	private void cleanLocation() {
		if (mLocation != null) {
			mLocation.onDestory();
			mLocation = null;
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

	private void isAnnoExistCallBack(UZModuleContext moduleContext, boolean status) {
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

	private void getLocationPermissionCallBack(UZModuleContext moduleContext, boolean enable) {
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
