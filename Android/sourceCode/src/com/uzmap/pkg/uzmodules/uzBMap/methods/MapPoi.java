/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapPoi implements OnGetPoiSearchResultListener,
		OnGetSuggestionResultListener {
	private JsParamsUtil mJsParamsUtil;
	private UZModuleContext mModuleContext;

	public MapPoi() {
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void searchInCity(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		String city = mJsParamsUtil.city(moduleContext);
		String keyword = moduleContext.optString("keyword");
		int pageCapacity = moduleContext.optInt("pageCapacity", 10);
		int pageNum = moduleContext.optInt("pageIndex", 0);
		PoiSearch search = PoiSearch.newInstance();
		search.setOnGetPoiSearchResultListener(this);
		search.searchInCity(new PoiCitySearchOption().city(city)
				.keyword(keyword).pageCapacity(pageCapacity).pageNum(pageNum));
	}

	public void searchNearby(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		String keyword = moduleContext.optString("keyword");
		double lon = mJsParamsUtil.lon(moduleContext);
		double lat = mJsParamsUtil.lat(moduleContext);
		int r = moduleContext.optInt("radius");
		int pageCapacity = moduleContext.optInt("pageCapacity", 10);
		int pageNum = moduleContext.optInt("pageIndex", 0);
		PoiSearch search = PoiSearch.newInstance();
		search.setOnGetPoiSearchResultListener(this);
		search.searchNearby(new PoiNearbySearchOption()
				.location(new LatLng(lat, lon)).radius(r)
				.pageCapacity(pageCapacity).pageNum(pageNum).keyword(keyword));
	}

	public void searchInBounds(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		String keyword = moduleContext.optString("keyword");
		LatLngBounds latLngBounds = mJsParamsUtil.latLngBounds(moduleContext);
		int pageCapacity = moduleContext.optInt("pageCapacity", 10);
		int pageNum = moduleContext.optInt("pageIndex", 0);
		PoiSearch search = PoiSearch.newInstance();
		search.setOnGetPoiSearchResultListener(this);
		search.searchInBound(new PoiBoundSearchOption().bound(latLngBounds)
				.keyword(keyword).pageCapacity(pageCapacity).pageNum(pageNum));
	}

	public void autoComplete(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		String keyword = moduleContext.optString("keyword");
		String city = mJsParamsUtil.city(moduleContext);
		SuggestionSearch search = SuggestionSearch.newInstance();
		search.setOnGetSuggestionResultListener(this);
		search.requestSuggestion((new SuggestionSearchOption())
				.keyword(keyword).city(city));
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult result) {
		if (result == null || result.getAllSuggestions() == null) {
			suggestFailBack();
		} else {
			suggestCallBack(result);
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
			poiSuccessCallBack(result);
		}
	}

	private void suggestFailBack() {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		try {
			ret.put("status", false);
			mModuleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void suggestCallBack(SuggestionResult result) {
		JSONObject ret = new JSONObject();
		JSONArray results = new JSONArray();
		try {
			ret.put("status", true);
			for (SuggestionResult.SuggestionInfo info : result
					.getAllSuggestions()) {
				results.put(info.key);
			}
			ret.put("results", results);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void poiSuccessCallBack(PoiResult result) {
		JSONObject ret = new JSONObject();
		JSONArray results = new JSONArray();
		JSONObject resultObj = null;
		try {
			ret.put("status", true);
			ret.put("currentNum", result.getCurrentPageCapacity());
			ret.put("pageIndex", result.getCurrentPageNum());
			ret.put("totalPage", result.getTotalPageNum());
			ret.put("totalNum", result.getTotalPoiNum());
			for (PoiInfo poi : result.getAllPoi()) {
				resultObj = new JSONObject();
				if (poi != null) {
					LatLng loc = poi.location;
					if (loc != null) {
						resultObj.put("lon", loc.longitude);
						resultObj.put("lat", loc.latitude);
					}
					resultObj.put("name", poi.name);
					resultObj.put("uid", poi.uid);
					resultObj.put("address", poi.address);
					resultObj.put("city", poi.city);
					resultObj.put("phone", poi.phoneNum);
					resultObj.put("postCode", poi.postCode);
					resultObj.put("poiType", poi.type.getInt());
					results.put(resultObj);
				}
			}
			ret.put("results", results);
			mModuleContext.success(ret, false);
			Log.e("poi", ret.toString());
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
}
