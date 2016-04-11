/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapDoubleClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationInterface;
import com.uzmap.pkg.uzmodules.uzBMap.location.LocationUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.CallBackUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.UzOrientationListener;
import com.uzmap.pkg.uzmodules.uzBMap.utils.UzOrientationListener.OnOrientationListener;

public class MapOpen implements LocationInterface {
	private UzBMap mUzBMap;
	private UZModuleContext mModuleContext;
	private Context mContext;
	private JsParamsUtil mJsParamsUtil;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationUtil mLocationUtil;
	private boolean mIsFirstLoc = true;
	private boolean mIsShowLoc = false;
	private boolean mIsCenter = false;
	private BDLocation mCurrLoc;
	private int mXDirection;
	private InfoWindow mInfoWindow;
	private HeatMap mHeatMap;
	private UzOrientationListener mOrientationListener;
	private String mFixedOn;
	private boolean mFixed;
	private LayoutParams mLayoutParams;
	private int mX;
	private int mY;
	private int mW;
	private int mH;

	public MapOpen(UzBMap mUzBMap, UZModuleContext mModuleContext,
			Context mContext) {
		this.mUzBMap = mUzBMap;
		this.mModuleContext = mModuleContext;
		this.mContext = mContext;
		mLocationUtil = new LocationUtil(mContext, this);
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	private void requestParentDisallowInterceptTouchEvent(
			boolean disallowIntercept) {
		final ViewParent parent = mMapView.getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}

	public void open() {
		mMapView = new MapView(mContext);
		requestParentDisallowInterceptTouchEvent(true);
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);
		initBaiduMap();
		setZoomLevel();
		setCenter();
		location();
		insertView();
	}

	private void initBaiduMap() {
		mMapView.setVisibility(View.VISIBLE);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				new CallBackUtil().openCallBack(mModuleContext);
			}
		});
		mBaiduMap.setMyLocationEnabled(true);
	}

	private void setZoomLevel() {
		double zoomLevel = mJsParamsUtil.zoomLevel(mModuleContext);
		setZoomLevel(zoomLevel);
	}

	private void setCenter() {
		double centerLon = mJsParamsUtil.lon(mModuleContext, "center");
		double centerLat = mJsParamsUtil.lat(mModuleContext, "center");
		if (centerLon != 0 && centerLat != 0) {
			LatLng latLng = new LatLng(centerLat, centerLon);
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
			mIsCenter = true;
		}
	}

	private void location() {
		boolean isShowUserLocation = mJsParamsUtil
				.showUserLocation(mModuleContext);
		if (isShowUserLocation) {
			mLocationUtil.startLocation();
		}
	}

	private void insertView() {
		mFixedOn = mModuleContext.optString("fixedOn");
		mFixed = mModuleContext.optBoolean("fixed", true);
		mLayoutParams = layout();
		mUzBMap.insertViewToCurWindow(mMapView, mLayoutParams, mFixedOn, mFixed);
	}

	@SuppressWarnings("deprecation")
	public void setRect(UZModuleContext moduleContext) {
		JSONObject rect = moduleContext.optJSONObject("rect");
		int x = UZCoreUtil.pixToDip(mX);
		int y = UZCoreUtil.pixToDip(mY);
		int w = UZCoreUtil.pixToDip(mW);
		int h = UZCoreUtil.pixToDip(mH);
		if (rect != null) {
			x = UZUtility.dipToPix(rect.optInt("x", x));
			y = UZUtility.dipToPix(rect.optInt("y", y));
			w = UZUtility.dipToPix(rect.optInt("w", w));
			h = UZUtility.dipToPix(rect.optInt("h", h));
			mX = x;
			mY = y;
			mW = w;
			mH = h;
		}
		if (mMapView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(w, h);
			p.setMargins(x, y, 0, 0);
			mMapView.setLayoutParams(p);
		} else if (mMapView.getLayoutParams() instanceof MarginLayoutParams) {
			LayoutParams p = new LayoutParams(w, h);
			p.setMargins(x, y, 0, 0);
			mMapView.setLayoutParams(p);
		} else {
			AbsoluteLayout.LayoutParams p = new AbsoluteLayout.LayoutParams(w,
					h, x, y);
			mMapView.setLayoutParams(p);
		}
	}

	private LayoutParams layout() {
		int x = mJsParamsUtil.x(mModuleContext);
		int y = mJsParamsUtil.y(mModuleContext);
		int w = mJsParamsUtil.w(mModuleContext, mContext);
		int h = mJsParamsUtil.h(mModuleContext, mContext);
		mX = UZUtility.dipToPix(x);
		mY = UZUtility.dipToPix(y);
		mW = UZUtility.dipToPix(w);
		mH = UZUtility.dipToPix(h);
		LayoutParams layoutParams = new LayoutParams(w, h);
		layoutParams.setMargins(x, y, 0, 0);
		return layoutParams;
	}

	private void setLocationData(BDLocation location) {
		mCurrLoc = location;
		MyLocationData locData = new MyLocationData.Builder()
				.accuracy(location.getRadius()).direction(mXDirection)
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);
	}

	private void showUserLocation(BDLocation location) {
		if (mIsShowLoc) {
			animateMove2Center(location.getLatitude(), location.getLongitude());
			mIsShowLoc = false;
		}
	}

	private void firstLocation(BDLocation location) {
		if (mIsFirstLoc) {
			mIsFirstLoc = false;
			if (!mIsCenter) {
				animateMove2Center(location.getLatitude(),
						location.getLongitude());
			}
		}
	}

	private void animateMove2Center(double lat, double lon) {
		LatLng latLng = new LatLng(lat, lon);
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newLatLng(latLng);
		mBaiduMap.animateMapStatus(mapStatusUpdate);
	}

	@Override
	public void onReceive(BDLocation location) {
		if (location == null || mMapView == null)
			return;
		setLocationData(location);
		showUserLocation(location);
		firstLocation(location);
	}

	private LocationMode getTrackingMode(UZModuleContext moduleContext) {
		String trackingMode = mJsParamsUtil.trackingMode(moduleContext);
		LocationMode locationMode = LocationMode.NORMAL;
		if (trackingMode.equals("follow")) {
			locationMode = LocationMode.FOLLOWING;
			mIsShowLoc = false;
		} else if (trackingMode.equals("compass")) {
			locationMode = LocationMode.COMPASS;
			mIsShowLoc = false;
		} else {
			mIsShowLoc = true;
		}
		return locationMode;
	}

	private void setTrackingMode(LocationMode locationMode) {
		if (mBaiduMap != null) {
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					locationMode, true, locIcon()));
		}
	}

	private BitmapDescriptor locIcon() {
		return BitmapDescriptorFactory.fromResource(UZResourcesIDFinder
				.getResDrawableID("location_marker"));
	}

	private void stopLocation() {
		mLocationUtil.onDestory();
		mBaiduMap.setMyLocationEnabled(false);
	}

	public void onDestory() {
		stopLocation();
		mMapView.onPause();
		mMapView.onDestroy();
		mMapView = null;
		if (mOrientationListener != null) {
			mOrientationListener.stop();
			mOrientationListener = null;
		}
	}

	public void close() {
		mUzBMap.removeViewFromCurWindow(mMapView);
		onDestory();
	}

	public void show() {
		mMapView.setVisibility(View.VISIBLE);
		mMapView.onResume();
	}

	public void hide() {
		mMapView.setVisibility(View.GONE);
		mMapView.onPause();
	}

	public void showUserLocation(UZModuleContext moduleContext) {
		boolean isShow = mJsParamsUtil.isShow(moduleContext);
		if (isShow) {
			initOritationListener();
			setTrackingMode(getTrackingMode(moduleContext));
			mLocationUtil.startLocation();
			mBaiduMap.setMyLocationEnabled(true);
		} else {
			stopLocation();
		}
	}

	private void initOritationListener() {
		mOrientationListener = new UzOrientationListener(mContext);
		mOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						if (mCurrLoc != null) {
							System.out.println(x);
							mXDirection = (int) x;
							setLocationData(mCurrLoc);
						}
					}
				});
		mOrientationListener.start();
	}

	public void setCenter(double centerLon, double centerLat,
			boolean isAnimation) {
		if (centerLon != 0 && centerLat != 0) {
			if (isAnimation) {
				animateMove2Center(centerLat, centerLon);
			} else {
				LatLng latLng = new LatLng(centerLat, centerLon);
				mBaiduMap
						.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
			}
		}
	}

	public void setZoomLevel(double zoomLevel) {
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.zoomTo((float) zoomLevel);
		mBaiduMap.animateMapStatus(mapStatusUpdate);
	}

	public void setMapType(String type) {
		if (type.equals("standard")) {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			mBaiduMap.setTrafficEnabled(false);
		} else if (type.equals("trafficOn")) {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			mBaiduMap.setTrafficEnabled(true);
		} else if (type.equals("trafAndsate")) {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			mBaiduMap.setTrafficEnabled(true);
		} else if (type.equals("satellite")) {
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			mBaiduMap.setTrafficEnabled(false);
		}
	}

	public void setZoomEnable(boolean status) {
		UiSettings uiSettings = mBaiduMap.getUiSettings();
		uiSettings.setZoomGesturesEnabled(status);
	}

	public void setScrollEnable(boolean status) {
		UiSettings uiSettings = mBaiduMap.getUiSettings();
		uiSettings.setScrollGesturesEnabled(status);
	}

	public void setRotation(int degree) {
		MapStatus mapStatus = new MapStatus.Builder(mBaiduMap.getMapStatus())
				.rotate(degree).build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mapStatus);
		mBaiduMap.animateMapStatus(mapStatusUpdate);
	}

	public void setOverlook(int degree) {
		MapStatus mapStatus = new MapStatus.Builder(mBaiduMap.getMapStatus())
				.overlook(degree).build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mapStatus);
		mBaiduMap.animateMapStatus(mapStatusUpdate);
	}

	public void setScaleBar(UZModuleContext moduleContext) {
		boolean show = moduleContext.optBoolean("show", false);
		mMapView.showScaleControl(show);
		mMapView.setScaleControlPosition(new Point(mJsParamsUtil
				.controlX(moduleContext), mJsParamsUtil.controlY(moduleContext)));
	}

	public void setCompassPosition(UZModuleContext moduleContext) {
		getBaiduMap().getUiSettings().setCompassPosition(
				new Point(mJsParamsUtil.controlX(moduleContext), mJsParamsUtil
						.controlY(moduleContext)));
	}

	public void setRegion(UZModuleContext moduleContext) {
		LatLngBounds latLngBounds = mJsParamsUtil.latLngBounds(moduleContext);
		boolean isAnimation = mJsParamsUtil
				.latLngBoundsAnimation(moduleContext);
		if (isAnimation) {
			mBaiduMap.animateMapStatus(MapStatusUpdateFactory
					.newLatLngBounds(latLngBounds));
		} else {
			mBaiduMap.setMapStatus(MapStatusUpdateFactory
					.newLatLngBounds(latLngBounds));
		}

	}

	public void getRegion(UZModuleContext moduleContext) {
		LatLng pointLeft = mBaiduMap.getProjection().fromScreenLocation(
				new Point(0, mMapView.getHeight()));
		LatLng pointRight = mBaiduMap.getProjection().fromScreenLocation(
				new Point(mMapView.getWidth(), 0));
		new CallBackUtil().getRegionCallBack(moduleContext, pointLeft,
				pointRight);
	}

	public void zoomIn() {
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
	}

	public void zoomOut() {
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
	}

	public void addMapClickListener(OnMapClickListener clickListener) {
		mBaiduMap.setOnMapClickListener(clickListener);
	}

	public void addMapDoubleClickListener(
			OnMapDoubleClickListener doubleClickListener) {
		mBaiduMap.setOnMapDoubleClickListener(doubleClickListener);
	}

	public void addMapLongClickListener(OnMapLongClickListener longClickListener) {
		mBaiduMap.setOnMapLongClickListener(longClickListener);
	}

	public void addMapStatusChangeListener(
			OnMapStatusChangeListener statusChangeListener) {
		mBaiduMap.setOnMapStatusChangeListener(statusChangeListener);
	}

	public void setBuilding(boolean isBuilding) {
		mBaiduMap.setBuildingsEnabled(isBuilding);
	}

	@SuppressLint("HandlerLeak")
	public void addHeatMap() {
		final Handler h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mBaiduMap != null)
					mBaiduMap.addHeatMap(mHeatMap);
			}
		};
		new Thread() {
			@Override
			public void run() {
				super.run();
				List<LatLng> data = getLocations();
				mHeatMap = new HeatMap.Builder().data(data).build();
				h.sendEmptyMessage(0);
			}
		}.start();
	}

	public void removeHeatMap() {
		if (mHeatMap != null) {
			mHeatMap.removeHeatMap();
		}
	}

	private List<LatLng> getLocations() {
		List<LatLng> list = new ArrayList<LatLng>();
		InputStream inputStream = mContext.getResources().openRawResource(
				UZResourcesIDFinder.getResRawID("locations"));
		@SuppressWarnings("resource")
		String json = new Scanner(inputStream).useDelimiter("\\A").next();
		JSONArray array;
		try {
			array = new JSONArray(json);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				double lat = object.getDouble("lat");
				double lng = object.getDouble("lng");
				list.add(new LatLng(lat, lng));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public BaiduMap getBaiduMap() {
		return mBaiduMap;
	}

	public InfoWindow getInfoWindow() {
		return mInfoWindow;
	}

	public void setInfoWindow(InfoWindow mInfoWindow) {
		this.mInfoWindow = mInfoWindow;
	}

	public MapView getMapView() {
		return mMapView;
	}

	public void setMapView(MapView mMapView) {
		this.mMapView = mMapView;
	}
}
