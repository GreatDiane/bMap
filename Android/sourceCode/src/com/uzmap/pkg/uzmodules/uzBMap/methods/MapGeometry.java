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
import android.graphics.Bitmap;
import android.graphics.Color;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.district.DistrictResult;
import com.baidu.mapapi.search.district.DistrictSearch;
import com.baidu.mapapi.search.district.DistrictSearchOption;
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

@SuppressLint("UseSparseArrays")
public class MapGeometry {
	private JsParamsUtil mJsParamsUtil;
	private Map<Integer, Overlay> mLineMap;
	private Map<Integer, Overlay> mPolygonMap;
	private Map<Integer, Overlay> mArcMap;
	private Map<Integer, Overlay> mImgMap;
	private Map<Integer, Overlay> mLinedMap;
	private Map<Integer, Overlay> mGondMap;
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
		mLinedMap = new HashMap<Integer, Overlay>();
		mGondMap = new HashMap<Integer, Overlay>();
	}
	
	private DistrictSearch mDistrictSearch;
	public void addDistrict(final UZModuleContext moduleContext) {
		if (mDistrictSearch == null) {
			mDistrictSearch = DistrictSearch.newInstance();
		}
		OnGetDistricSearchResultListener listener = new OnGetDistricSearchResultListener() {
		    @Override
		    public void onGetDistrictResult(DistrictResult districtResult) {
		        if (null != districtResult && districtResult.error == SearchResult.ERRORNO.NO_ERROR) {
		            //对检索所得行政区划边界数据进行处理
		        		drawDistrict(moduleContext, districtResult);
		        		callback(moduleContext, true, null);
		        }else {
		        		if (districtResult != null) {
		        			callback(moduleContext, false, districtResult);
					}
		        }
		    }
		};
		mDistrictSearch.setOnDistrictSearchListener(listener);
		mDistrictSearch.searchDistrict(new DistrictSearchOption()
		        .cityName(moduleContext.optString("city"))
		        .districtName(moduleContext.optString("district")));
	}
	
	private void callback(UZModuleContext moduleContext, boolean status, DistrictResult districtResult) {
		try {
			JSONObject result = new JSONObject();
			JSONObject error = new JSONObject();
			if (status) {
				result.put("success", true);
				moduleContext.success(result, false);
			}else {
				result.put("success", false);
				error.put("code", districtResult.error.ordinal());
				moduleContext.error(result, error, false);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private void drawDistrict(UZModuleContext moduleContext, DistrictResult districtResult) {
		List<List<LatLng>> polyLines = districtResult.getPolylines();
        if (polyLines == null) {
            return;
        }
        int id = moduleContext.optInt("id");
        JSONObject style = moduleContext.optJSONObject("style");
        if (style == null) {
			style = new JSONObject();
		}
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (List<LatLng> polyline : polyLines) {
            OverlayOptions ooPolyline11 = new PolylineOptions().width(UZUtility.dipToPix(style.optInt("lineWidth", 1)))
                    .points(polyline).dottedLine(style.optBoolean("lineDash", false)).color(UZUtility.parseCssColor(style.optString("strokeColor", "#ff0000")));
            Overlay linedOverlay = mBaiduMap.addOverlay(ooPolyline11);
            mLinedMap.put(id, linedOverlay);
            OverlayOptions ooPolygon = new PolygonOptions().points(polyline).stroke(new Stroke(1, Color.TRANSPARENT)).fillColor(UZUtility.parseCssColor(style.optString("fillColor", "rgba(0,0,0,0)")));
            Overlay gondOverlay = mBaiduMap.addOverlay(ooPolygon);
            mGondMap.put(id, gondOverlay);
            for (LatLng latLng : polyline) {
                builder.include(latLng);
            }
        }
        mBaiduMap.setMapStatus(MapStatusUpdateFactory
                .newLatLngBounds(builder.build()));
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
	
	public void removeDistrict(UZModuleContext moduleContext) {
		int id = moduleContext.optInt("id");
		removeOverlay(id, mLinedMap);
		removeOverlay(id, mGondMap);
		if (mDistrictSearch != null) {
			mDistrictSearch.destroy();
			mDistrictSearch = null;
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
