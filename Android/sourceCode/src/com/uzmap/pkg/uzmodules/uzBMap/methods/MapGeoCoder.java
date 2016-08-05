/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult.AddressComponent;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.geocoder.GeoCoderInterface;
import com.uzmap.pkg.uzmodules.uzBMap.geocoder.GeoCoderUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapGeoCoder implements GeoCoderInterface {
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;
	private GeoCoderUtil mGeoCoderUtil;

	public MapGeoCoder(UZModuleContext moduleContext) {
		this.mModuleContext = moduleContext;
		mJsParamsUtil = JsParamsUtil.getInstance();
		mGeoCoderUtil = new GeoCoderUtil(this);
	}

	public void coord2address() {
		float lat = mJsParamsUtil.lat(mModuleContext);
		float lon = mJsParamsUtil.lon(mModuleContext);
		mGeoCoderUtil.coord2address(lat, lon);
	}

	public void address2coord() {
		String city = mJsParamsUtil.city(mModuleContext);
		String address = mJsParamsUtil.address(mModuleContext);
		mGeoCoderUtil.address2coord(city, address);
	}

	/**
	 * address2coord
	 */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		address2coordCallBack(result);
	}

	/**
	 * coord2address
	 */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		coord2addressCallBack(result);
	}

	private void coord2addressCallBack(ReverseGeoCodeResult result) {
		if (result == null) {
			fail("-1");
		} else if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			fail(result.error.toString());
		} else {
			coord2addressSuccess(result);
		}
	}

	private void address2coordCallBack(GeoCodeResult result) {
		if (result == null) {
			fail("-1");
		} else if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			fail(result.error.toString());
		} else {
			address2coordSuccess(result);
		}
	}

	private void fail(String code) {
		JSONObject ret = new JSONObject();
		JSONObject err = new JSONObject();
		try {
			ret.put("status", false);
			err.put("code", code);
			mModuleContext.error(ret, err, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void address2coordSuccess(GeoCodeResult result) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			ret.put("lon", result.getLocation().longitude);
			ret.put("lat", result.getLocation().latitude);
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void coord2addressSuccess(ReverseGeoCodeResult result) {
		JSONObject ret = new JSONObject();
		AddressComponent address = result.getAddressDetail();
		LatLng location = result.getLocation();
		try {
			ret.put("status", true);
			ret.put("lon", location.longitude);
			ret.put("lat", location.latitude);
			ret.put("address", result.getAddress());
			ret.put("province", address.province);
			ret.put("city", address.city);
			ret.put("district", address.district);
			ret.put("streetName", address.street);
			ret.put("streetNumber", address.streetNumber);
			JSONArray poiListJson = new JSONArray();
			List<PoiInfo> poiList = result.getPoiList();
			if (poiList != null) {
				ret.put("poiList", poiListJson);
				PoiInfo p = null;
				for (int i = 0; i < poiList.size(); i++) {
					JSONObject poiInfo = new JSONObject();
					p = poiList.get(i);
					poiInfo.put("name", p.name);
					poiInfo.put("uid", p.uid);
					poiInfo.put("address", p.address);
					poiInfo.put("city", p.city);
					poiInfo.put("phone", p.phoneNum);
					poiInfo.put("postcode", p.postCode);
					poiInfo.put("epoitype", p.type);
					JSONObject coord = new JSONObject();
					poiInfo.put("coord", coord);
					coord.put("lat", p.location.latitude);
					coord.put("lon", p.location.longitude);
					poiListJson.put(poiInfo);
				}
			}
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
