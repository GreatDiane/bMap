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
import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

@SuppressLint("UseSparseArrays")
public class MapGeometry {
	private JsParamsUtil mJsParamsUtil;
	private Map<Integer, Overlay> mLineMap;
	private Map<Integer, Overlay> mPolygonMap;
	private Map<Integer, Overlay> mArcMap;
	private Map<Integer, Overlay> mImgMap;
	private BaiduMap mBaiduMap;
	private UzBMap mUzBMap;

	public MapGeometry(UzBMap uzBMap, BaiduMap baiduMap) {
		this.mUzBMap = uzBMap;
		this.mBaiduMap = baiduMap;
		mJsParamsUtil = JsParamsUtil.getInstance();
		mLineMap = new HashMap<Integer, Overlay>();
		mPolygonMap = new HashMap<Integer, Overlay>();
		mArcMap = new HashMap<Integer, Overlay>();
		mImgMap = new HashMap<Integer, Overlay>();
	}

	public void addLine(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		List<LatLng> points = mJsParamsUtil.overlayPoints(moduleContext);
		if (points == null || points.size() <= 0) {
			return;
		}
		int lineColor = mJsParamsUtil.overlayColor(moduleContext);
		int lineWidth = mJsParamsUtil.overlayWidth(moduleContext);
		boolean lineDash = mJsParamsUtil.lineDash(moduleContext);
		Bitmap bitmap = mJsParamsUtil.dashImg(moduleContext, mUzBMap);
		BitmapDescriptor bdGround = BitmapDescriptorFactory.fromBitmap(bitmap);
		OverlayOptions ooPolyline = new PolylineOptions().width(lineWidth)
				.color(lineColor).points(points).customTexture(bdGround)
				.dottedLine(lineDash);
		mLineMap.put(id, mBaiduMap.addOverlay(ooPolyline));
	}

	public void addPolygon(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		List<LatLng> points = mJsParamsUtil.overlayPoints(moduleContext);
		int overlayColor = mJsParamsUtil.overlayColor(moduleContext);
		int overlayFillColor = mJsParamsUtil.overlayFillColor(moduleContext);
		int overlayWidth = mJsParamsUtil.overlayWidth(moduleContext);
		OverlayOptions ooPolygon = new PolygonOptions().points(points)
				.stroke(new Stroke(overlayWidth, overlayColor))
				.fillColor(overlayFillColor);
		mPolygonMap.put(id, mBaiduMap.addOverlay(ooPolygon));
	}

	public void addArc(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		List<LatLng> points = mJsParamsUtil.overlayPoints(moduleContext);
		if (points == null || points.size() < 3) {
			return;
		}
		int overlayColor = mJsParamsUtil.overlayColor(moduleContext);
		int overlayWidth = mJsParamsUtil.overlayWidth(moduleContext);
		OverlayOptions ooArc = new ArcOptions().color(overlayColor)
				.width(overlayWidth)
				.points(points.get(0), points.get(1), points.get(2));
		mArcMap.put(id, mBaiduMap.addOverlay(ooArc));
	}

	public void addCircle(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		int r = moduleContext.optInt("radius");
		double lat = mJsParamsUtil.lat(moduleContext, "center");
		double lon = mJsParamsUtil.lon(moduleContext, "center");
		LatLng point = new LatLng(lat, lon);
		int overlayColor = mJsParamsUtil.overlayColor(moduleContext);
		int overlayFillColor = mJsParamsUtil.overlayFillColor(moduleContext);
		int overlayWidth = mJsParamsUtil.overlayWidth(moduleContext);
		OverlayOptions ooCircle = new CircleOptions()
				.fillColor(overlayFillColor).center(point)
				.stroke(new Stroke(overlayWidth, overlayColor)).radius(r);
		Overlay existOverLay = mPolygonMap.get(id);
		if (existOverLay != null) {
			existOverLay.remove();
		}
		mPolygonMap.put(id, mBaiduMap.addOverlay(ooCircle));
	}

	public void addImg(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		Bitmap bitmap = mJsParamsUtil.overlayImg(moduleContext, mUzBMap);
		BitmapDescriptor bdGround = BitmapDescriptorFactory.fromBitmap(bitmap);
		LatLngBounds bounds = mJsParamsUtil.latLngBounds(moduleContext);
		float opacity = mJsParamsUtil.overlayImgOpacity(moduleContext);
		OverlayOptions ooGround = new GroundOverlayOptions()
				.positionFromBounds(bounds).image(bdGround)
				.transparency(opacity);
		mImgMap.put(id, mBaiduMap.addOverlay(ooGround));
	}

	public void removeOverlays(UZModuleContext moduleContext) {
		List<Integer> list = mJsParamsUtil.removeOverlayIds(moduleContext);
		if (list != null) {
			for (int index : list) {
				removeOverlay(index, mLineMap);
				removeOverlay(index, mPolygonMap);
				removeOverlay(index, mImgMap);
				removeOverlay(index, mArcMap);
			}
		}
	}

	private void removeOverlay(int index, Map<Integer, Overlay> map) {
		Overlay overlay = null;
		if (map != null) {
			overlay = map.get(index);
			if (overlay != null) {
				overlay.remove();
			}
		}
	}
}
