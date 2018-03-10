/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.BusLineOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapBusLine implements OnGetPoiSearchResultListener,
		OnGetBusLineSearchResultListener {
	private JsParamsUtil mJsParamsUtil;
	private PoiSearch mPoiSearch = null;
	private BusLineSearch mBusLineSearch = null;
	private BaiduMap mBaiduMap = null;
	private List<PoiInfo> mBusLineIDList = null;
	private UZModuleContext mModuleContext;
	private UZModuleContext mDrawModuleContext;
	private Map<Integer, BusLineOverlay> mBusLineMap;
	private int mBusLineId;

	public MapBusLine() {
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void busLine(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		String city = mJsParamsUtil.city(moduleContext);
		String busLine = mJsParamsUtil.line(moduleContext);
		mPoiSearch.searchInCity((new PoiCitySearchOption()).city(city).keyword(
				busLine));
	}

	public void drawBusLine(UZModuleContext moduleContext,
			Map<Integer, BusLineOverlay> busLineMap, BaiduMap baiduMap) {
		mDrawModuleContext = moduleContext;
		this.mBaiduMap = baiduMap;
		mBusLineMap = busLineMap;
		mBusLineId = moduleContext.optInt("id");
		String city = moduleContext.optString("city");
		String uid = moduleContext.optString("uid");
		mBusLineSearch = BusLineSearch.newInstance();
		mBusLineSearch.setOnGetBusLineSearchResultListener(this);
		mBusLineSearch.searchBusLine(new BusLineSearchOption().city(city).uid(
				uid));
	}

	@Override
	public void onGetBusLineResult(BusLineResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			busFailCallBack();
		} else {
			UzBusLineOverLay overlay = new UzBusLineOverLay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			mBusLineMap.put(mBusLineId, overlay);
			if (mDrawModuleContext.optBoolean("autoresizing", true)) {
				overlay.zoomToSpan();
			}
			drawSuccessCallBack(result);
		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {

	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			poiFailCallBack(result);
		} else {
			mBusLineIDList = new ArrayList<PoiInfo>();
			for (PoiInfo poi : result.getAllPoi()) {
				if (poi.type == PoiInfo.POITYPE.BUS_LINE
						|| poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
					mBusLineIDList.add(poi);
				}
			}
			poiSuccessCallBack();
		}
	}

	class UzBusLineOverLay extends BusLineOverlay {

		public UzBusLineOverLay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onBusStationClick(int index) {
			nodeClickBack(index);
			return true;
		}
	}

	private void nodeClickBack(int index) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("nodeIndex", index);
			ret.put("routeId", mBusLineId);
			ret.put("eventType", "click");
			ret.put("status", true);
			mDrawModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void busFailCallBack() {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		try {
			ret.put("status", false);
			mModuleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void poiSuccessCallBack() {
		JSONObject ret = new JSONObject();
		JSONArray results = new JSONArray();
		JSONObject result = null;
		try {
			ret.put("status", true);
			for (PoiInfo poi : mBusLineIDList) {
				result = new JSONObject();
				result.put("name", poi.name);
				result.put("uid", poi.uid);
				result.put("city", poi.city);
				result.put("poiType", poi.type.getInt());
				ret.put("eventType", "draw");
				results.put(result);
			}
			ret.put("results", results);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void poiFailCallBack(PoiResult result) {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		try {
			ret.put("status", false);
			if (result == null) {
				err.put("code", -1);
			} else if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
				err.put("code", 1);
			} else if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
				err.put("code", 2);
			} else if (result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
				err.put("code", 3);
			} else if (result.error == SearchResult.ERRORNO.KEY_ERROR) {
				err.put("code", 4);
			} else {
				err.put("code", -1);
			}
			mModuleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void drawSuccessCallBack(BusLineResult result) {
		JSONObject ret = new JSONObject();
		JSONArray stations = new JSONArray();
		JSONObject station = null;
		try {
			ret.put("status", true);
			ret.put("name", result.getBusLineName());
			ret.put("company", result.getBusCompany());
			ret.put("startTime", result.getStartTime());
			ret.put("endTime", result.getEndTime());
			ret.put("isMonTicket", result.isMonthTicket());
			ret.put("status", true);
			for (BusStation busStation : result.getStations()) {
				station = new JSONObject();
				station.put("description", busStation.getTitle());
				station.put("lat", busStation.getLocation().latitude);
				station.put("lon", busStation.getLocation().longitude);
				stations.put(station);
			}
			ret.put("stations", stations);
			mDrawModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult arg0) {
		// TODO Auto-generated method stub  多了一个方法
		
	}

}
