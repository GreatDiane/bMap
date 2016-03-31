/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapDoubleClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapEventListener {
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;
	private MapOpen mMap;
	private boolean mIsCallBack;

	public MapEventListener(UZModuleContext mModuleContext, MapOpen mMap,
			boolean mIsCallBack) {
		this.mModuleContext = mModuleContext;
		this.mMap = mMap;
		this.mIsCallBack = mIsCallBack;
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void addEventListener() {
		String eventName = mJsParamsUtil.eventName(mModuleContext);
		if (eventName.equals("click")) {
			addMapClickListener();
		} else if (eventName.equals("dbclick")) {
			addMapDoubleClickListener();
		} else if (eventName.equals("longPress")) {
			addMapLongClickListener();
		} else if (eventName.equals("viewChange")) {
			addMapStatusChangeListener();
		}
	}

	public void addDefaultEventListener() {
		addMapClickListener();
		addMapStatusChangeListener();
	}

	public void addMapClickListener() {
		mMap.addMapClickListener(mapClickListener());
	}

	public void addMapDoubleClickListener() {
		mMap.addMapDoubleClickListener(mapDoubleClickListener());
	}

	public void addMapLongClickListener() {
		mMap.addMapLongClickListener(mapLongClickListener());
	}

	public void addMapStatusChangeListener() {
		mMap.addMapStatusChangeListener(mapStatusChangeListener());
	}

	private OnMapClickListener mapClickListener() {
		return new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				if (mIsCallBack) {
					callBack(mapPoi.getPosition());
				}
				mMap.getBaiduMap().hideInfoWindow();
				return false;
			}

			@Override
			public void onMapClick(LatLng latLng) {
				if (mIsCallBack) {
					callBack(latLng);
				}
				mMap.getBaiduMap().hideInfoWindow();
			}
		};
	}

	private OnMapDoubleClickListener mapDoubleClickListener() {
		return new OnMapDoubleClickListener() {

			@Override
			public void onMapDoubleClick(LatLng latLng) {
				if (mIsCallBack) {
					callBack(latLng);
				}
			}
		};
	}

	private OnMapLongClickListener mapLongClickListener() {
		return new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng latLng) {
				if (mIsCallBack) {
					callBack(latLng);
				}
			}
		};
	}

	private OnMapStatusChangeListener mapStatusChangeListener() {
		return new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChangeStart(MapStatus mapStatus) {
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus mapStatus) {
				if (mIsCallBack) {
					callBack(mapStatus);
				}
				if (mapStatus.rotate != 0 || mapStatus.overlook != 0) {
					mMap.getBaiduMap().getUiSettings().setCompassEnabled(true);
				} else {
					mMap.getBaiduMap().getUiSettings().setCompassEnabled(false);
				}
			}

			@Override
			public void onMapStatusChange(MapStatus mapStatus) {
			}
		};
	}

	private void callBack(LatLng latLng, MapStatus mapStatus) {
		float zoomLevel = mapStatus.zoom;
		float rotate = mapStatus.rotate;
		float overLook = mapStatus.overlook;
		double lon = latLng.longitude;
		double lat = latLng.latitude;
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			ret.put("lon", lon);
			ret.put("lat", lat);
			ret.put("overlook", overLook);
			ret.put("rotate", rotate);
			ret.put("zoom", zoomLevel);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void callBack(LatLng latLng) {
		callBack(latLng, mMap.getBaiduMap().getMapStatus());
	}

	private void callBack(MapStatus mapStatus) {
		double lat = mMap.getBaiduMap().getMapStatus().target.latitude;
		double lon = mMap.getBaiduMap().getMapStatus().target.longitude;
		callBack(new LatLng(lat, lon), mapStatus);
	}
}
