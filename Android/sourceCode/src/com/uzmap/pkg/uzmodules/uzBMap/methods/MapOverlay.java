/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.mode.Annotation;
import com.uzmap.pkg.uzmodules.uzBMap.mode.Billboard;
import com.uzmap.pkg.uzmodules.uzBMap.mode.Bubble;
import com.uzmap.pkg.uzmodules.uzBMap.utils.CallBackUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapOverlay {
	private UzBMap mUzBMap;
	private JsParamsUtil mJsParamsUtil;
	private MapOpen mMap;
	private Map<Integer, Annotation> mMarkerMap;
	private Map<Integer, Annotation> mMoveMarkerMap;
	private Map<Integer, Bubble> mBubbleMap;
	private Map<Integer, Billboard> mBillboardMap;
	private InfoWindow mInfoWindow;
	private OnMarkerClickListener mOnMarkerClickListener;

	@SuppressLint("UseSparseArrays")
	public MapOverlay(UzBMap mUzBMap, MapOpen mMap) {
		this.mUzBMap = mUzBMap;
		this.mMap = mMap;
		mJsParamsUtil = JsParamsUtil.getInstance();
		mMarkerMap = new HashMap<Integer, Annotation>();
		mMoveMarkerMap = new HashMap<Integer, Annotation>();
		mBubbleMap = new HashMap<Integer, Bubble>();
		mBillboardMap = new HashMap<Integer, Billboard>();
	}

	public void addAnnotations(UZModuleContext moduleContext) {
		addMarkerClickListener();
		List<Annotation> annoList = mJsParamsUtil.annotations(moduleContext,
				mUzBMap);
		if (annoList != null) {
			for (Annotation annotation : annoList) {
				removeExistAnno(annotation);
				addAnnotation(annotation);
				mMarkerMap.put(annotation.getId(), annotation);
			}
		}
	}

	private void removeExistAnno(Annotation annotation) {
		if (isAnnotationExist(annotation.getId())) {
			mMarkerMap.get(annotation.getId()).getMarker().remove();
			mMap.getBaiduMap().hideInfoWindow();
		}
	}

	public boolean isAnnotationExist(int id) {
		if (mMarkerMap != null) {
			Annotation annotation = mMarkerMap.get(id);
			if (annotation != null) {
				return true;
			}
		}
		return false;
	}

	public void isAnnotationExist(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		boolean isExist = isAnnotationExist(id);
		isAnnoExistCallBack(moduleContext, isExist);
	}

	public void addMobileAnnotations(UZModuleContext moduleContext) {
		List<Annotation> annoList = mJsParamsUtil.annotations(moduleContext,
				mUzBMap);
		if (annoList != null) {
			for (Annotation annotation : annoList) {
				addMoveAnnotation(annotation);
				mMoveMarkerMap.put(annotation.getId(), annotation);
			}
		}
	}

	public void getAnnotationCoords(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		if (mMarkerMap != null) {
			Annotation annotation = mMarkerMap.get(id);
			if (annotation != null) {
				LatLng latLng = annotation.getMarker().getPosition();
				new CallBackUtil().getAnnoCoordsCallBack(moduleContext, latLng);
			}
		}
	}

	public void setAnnotationCoords(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		double lat = mJsParamsUtil.lat(moduleContext);
		double lon = mJsParamsUtil.lon(moduleContext);
		if (mMarkerMap != null) {
			Annotation annotation = mMarkerMap.get(id);
			if (annotation != null) {
				annotation.getMarker().setPosition(new LatLng(lat, lon));
				mMap.getMapView().invalidate();
			}
		}
	}

	public void setBubble(UZModuleContext moduleContext) {
		Bubble bubble = createBubble(moduleContext);
		mBubbleMap.put(bubble.getId(), bubble);
	}

	public void popupBubble(UZModuleContext moduleContext) {
		int id = mJsParamsUtil.bubbleId(moduleContext);
		if (mMarkerMap != null && mBubbleMap != null) {
			Annotation annotation = mMarkerMap.get(id);
			Bubble bubble = mBubbleMap.get(id);
			if (annotation != null && bubble != null) {
				showBubble(annotation.getMarker());
			}
		}
	}

	public void addBillboard(UZModuleContext moduleContext) {
		addMarkerClickListener();
		boolean isNetIcon = mJsParamsUtil.isBillboardNetIcon(moduleContext);
		Billboard billboard = createBillboard(moduleContext);
		mBillboardMap.put(billboard.getId(), billboard);
		if (isNetIcon) {
			billboard.startLoadIcon();
		} else {
			Marker marker = addBillboard(moduleContext,
					billboard.billboardView());
			billboard.setMarker(marker);
		}
	}

	public void addBillboard(UZModuleContext moduleContext,
			Billboard billboard, ImageView icon) {
		Marker marker = addBillboard(moduleContext,
				netIconView(billboard, icon));
		billboard.setMarker(marker);
	}

	public void removeOverlay(UZModuleContext moduleContext) {
		List<Integer> list = mJsParamsUtil.removeOverlayIds(moduleContext);
		if (list != null) {
			Annotation annotation = null;
			Billboard billboard = null;
			for (int i : list) {
				if (mMarkerMap != null) {
					annotation = mMarkerMap.get(i);
					if (annotation != null) {
						annotation.getMarker().remove();
						mMap.getBaiduMap().hideInfoWindow();
					}
				}
				if (mBillboardMap != null) {
					billboard = mBillboardMap.get(i);
					if (billboard != null) {
						billboard.getMarker().remove();
					}
				}
			}
		}
	}

	private Marker addBillboard(UZModuleContext moduleContext, View view) {
		double lat = mJsParamsUtil.lat(moduleContext, "coords");
		double lon = mJsParamsUtil.lon(moduleContext, "coords");
		LatLng latLng = new LatLng(lat, lon);
		BitmapDescriptor iconImg = BitmapDescriptorFactory.fromView(view);
		OverlayOptions options = new MarkerOptions().position(latLng)
				.icon(iconImg).zIndex(mJsParamsUtil.bubbleId(moduleContext));
		return (Marker) mMap.getBaiduMap().addOverlay(options);
	}

	private View netIconView(Billboard billboard, ImageView icon) {
		return billboard.billboardView(icon);
	}

	private Bubble createBubble(UZModuleContext moduleContext) {
		Context context = mUzBMap.getContext();
		int bubbleId = mJsParamsUtil.bubbleId(moduleContext);
		Bitmap bgImg = mJsParamsUtil.bubbleBgImg(moduleContext, mUzBMap);
		String title = mJsParamsUtil.bubbleTitle(moduleContext);
		String subTitle = mJsParamsUtil.bubbleSubTitle(moduleContext);
		Bitmap icon = mJsParamsUtil.bubbleIllus(moduleContext, mUzBMap);
		String iconStr = mJsParamsUtil.bubbleIllusPath(moduleContext);
		int titleSize = mJsParamsUtil.bubbleTitleSize(moduleContext);
		int titleColor = mJsParamsUtil.bubbleTitleColor(moduleContext);
		int subTitleSize = mJsParamsUtil.bubbleSubTitleSize(moduleContext);
		int subTitleColor = mJsParamsUtil.bubbleSubTitleColor(moduleContext);
		String iconAlign = mJsParamsUtil.bubbleIconAlign(moduleContext);
		int maxWidth = mJsParamsUtil.getScreenWidth(mUzBMap.getContext())
				- UZCoreUtil.dipToPix(50);
		return new Bubble(moduleContext, context, bubbleId, bgImg, title,
				subTitle, icon, iconStr, titleSize, subTitleSize, titleColor,
				subTitleColor, iconAlign, maxWidth, this);
	}

	private Billboard createBillboard(UZModuleContext moduleContext) {
		Context context = mUzBMap.getContext();
		int bubbleId = mJsParamsUtil.bubbleId(moduleContext);
		Bitmap bgImg = mJsParamsUtil.bubbleBgImg(moduleContext, mUzBMap);
		String title = mJsParamsUtil.bubbleTitle(moduleContext);
		String subTitle = mJsParamsUtil.bubbleSubTitle(moduleContext);
		Bitmap icon = mJsParamsUtil.bubbleIllus(moduleContext, mUzBMap);
		String iconStr = mJsParamsUtil.bubbleIllusPath(moduleContext);
		int titleSize = mJsParamsUtil.bubbleTitleSize(moduleContext);
		int titleColor = mJsParamsUtil.bubbleTitleColor(moduleContext);
		int subTitleSize = mJsParamsUtil.bubbleSubTitleSize(moduleContext);
		int subTitleColor = mJsParamsUtil.bubbleSubTitleColor(moduleContext);
		String iconAlign = mJsParamsUtil.bubbleIconAlign(moduleContext);
		int maxWidth = mJsParamsUtil.getScreenWidth(mUzBMap.getContext())
				- UZCoreUtil.dipToPix(50);
		return new Billboard(moduleContext, context, bubbleId, bgImg, title,
				subTitle, icon, iconStr, titleSize, subTitleSize, titleColor,
				subTitleColor, iconAlign, maxWidth, this);
	}

	private void addMarkerClickListener() {
		if (mOnMarkerClickListener == null) {
			markerClickListener();
		}
		mMap.getBaiduMap().setOnMarkerClickListener(mOnMarkerClickListener);
		mMap.getBaiduMap().setOnMarkerDragListener(markerDragListener());
	}

	private void markerClickListener() {
		mOnMarkerClickListener = new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				int id = marker.getZIndex();
				if (isBillBord(marker)) {
					billClickCallBack(id);
				} else {
					annoClickCallBack(id);
					showBubble(marker);
				}
				return false;
			}
		};
	}

	private OnMarkerDragListener markerDragListener() {
		return new OnMarkerDragListener() {
			CallBackUtil callBackUtil = new CallBackUtil();

			@Override
			public void onMarkerDragStart(Marker marker) {
				dragCallBack(marker, callBackUtil, "starting");
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				dragCallBack(marker, callBackUtil, "ending");
			}

			@Override
			public void onMarkerDrag(Marker marker) {
				dragCallBack(marker, callBackUtil, "dragging");
			}
		};
	}

	private void dragCallBack(Marker marker, CallBackUtil callBackUtil,
			String dragState) {
		Annotation annotation = mMarkerMap.get(marker.getZIndex());
		annotation.setMarker(marker);
		callBackUtil.markerDragCallBack(annotation.getModuleContext(),
				marker.getZIndex(), "drag", dragState);
	}

	private boolean isBillBord(Marker marker) {
		int id = marker.getZIndex();
		Billboard billboard = mBillboardMap.get(id);
		if (billboard != null) {
			if (marker.equals(billboard.getMarker())) {
				return true;
			}
		}
		return false;
	}

	private void showBubble(Marker marker) {
		int id = marker.getZIndex();
		Bubble bubble = mBubbleMap.get(id);
		if (bubble != null) {
			mInfoWindow = new InfoWindow(mBubbleMap.get(id).bubbleView(),
					marker.getPosition(), -47);
			mMap.getBaiduMap().showInfoWindow(mInfoWindow);

		}
	}

	private void addAnnotation(Annotation annotation) {
		OverlayOptions options = new MarkerOptions()
				.position(annotation.getLatLng()).icon(annotation.getIcon())
				.zIndex(annotation.getId()).draggable(annotation.isDraggable())
				.anchor(0.5f, 0.5f);
		Marker marker = (Marker) mMap.getBaiduMap().addOverlay(options);
		annotation.setMarker(marker);
	}

	private void addMoveAnnotation(Annotation annotation) {
		OverlayOptions options = new MarkerOptions()
				.position(annotation.getLatLng()).icon(annotation.getIcon())
				.zIndex(annotation.getId()).draggable(annotation.isDraggable())
				.anchor(0.5f, 0.5f);
		Marker marker = (Marker) mMap.getBaiduMap().addOverlay(options);
		annotation.setMarker(marker);
	}

	private void annoClickCallBack(int id) {
		Annotation annotation = mMarkerMap.get(id);
		JSONObject ret = new JSONObject();
		try {
			if (annotation != null) {
				ret.put("id", annotation.getId());
				ret.put("eventType", "click");
				annotation.getModuleContext().success(ret, false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void billClickCallBack(int id) {
		Billboard billboard = mBillboardMap.get(id);
		JSONObject ret = new JSONObject();
		try {
			if (billboard != null) {
				ret.put("id", billboard.getId());
				billboard.getModuleContext().success(ret, false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void bubbleClickCallBack(int id, String eventType) {
		Bubble bubble = mBubbleMap.get(id);
		JSONObject ret = new JSONObject();
		try {
			ret.put("id", id);
			ret.put("eventType", eventType);
			bubble.getModuleContext().success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public Map<Integer, Annotation> getMoveMarkerMap() {
		return mMoveMarkerMap;
	}
}
