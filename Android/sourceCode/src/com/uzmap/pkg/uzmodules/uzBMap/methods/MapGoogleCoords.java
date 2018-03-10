/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapGoogleCoords extends Thread {
	private static final String URL = "http://api.map.baidu.com/geoconv/v1/?coords=";
	private static final String FROM_TO_AK = "&from=3&to=5&ak=";
	private static final String OUTPUT_MCODE = "&output=json&mcode=";
	private UzBMap mUzBMap;
	private UZModuleContext mModuleContext;
	private JsParamsUtil mJsParamsUtil;
	private BasicHttpParams mHttpParameters;
	private DefaultHttpClient mClient;

	public MapGoogleCoords(UZModuleContext mModuleContext, UzBMap mUzBMap) {
		this.mModuleContext = mModuleContext;
		this.mUzBMap = mUzBMap;
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	@Override
	public void run() {
		initHttp();
		onResponse(new HttpGet(uri()));
	}

	private void initHttp() {
		mHttpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(mHttpParameters, 15000);
		HttpConnectionParams.setSoTimeout(mHttpParameters, 15000);
		mClient = new DefaultHttpClient(mHttpParameters);
		mClient.getCookieSpecs().register("chinasource",
				new CookieSpecFactory() {
					public CookieSpec newInstance(HttpParams params) {
						return new LenientCookieSpec();
					}
				});
		mClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				"chinasource");
	}

	private String uri() {
		double lon = mJsParamsUtil.lon(mModuleContext);
		double lat = mJsParamsUtil.lat(mModuleContext);
		String ak = mUzBMap.getFeatureValue("bMap", "android_api_key");
		String mcode = mJsParamsUtil.mcode(mModuleContext);
		String uri = URL + lon + "," + lat + FROM_TO_AK + ak + OUTPUT_MCODE
				+ mcode;
		return uri;
	}

	private void onResponse(HttpGet get) {
		try {
			HttpResponse response = mClient.execute(get);
			parseResp(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseResp(HttpResponse response) {
		int responseCode = response.getStatusLine().getStatusCode();
		if (response != null && responseCode == 200) {
			callBack(response);
		} else {
			netFailBack();
		}
	}

	private void callBack(HttpResponse response) {
		HttpEntity httpEntity = response.getEntity();
		JSONObject json = getRespJson(httpEntity);
		String status = json.optString("status");
		if ("0".equals(status)) {
			successBack(json);
		} else {
			failBack(json);
		}
	}

	private JSONObject getRespJson(HttpEntity httpEntity) {
		try {
			String str = new String(EntityUtils.toByteArray(httpEntity),
					getCharSet(httpEntity));
			JSONObject json = new JSONObject(str);
			return json;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getCharSet(HttpEntity httpEntity) {
		String charSet = EntityUtils.getContentCharSet(httpEntity);
		if (null == charSet) {
			charSet = "UTF-8";
		}
		return charSet;
	}

	private void successBack(JSONObject json) {
		JSONArray jsonArr = json.optJSONArray("result");
		JSONObject ret = new JSONObject();
		try {
			ret.put("lon", jsonArr.getJSONObject(0).get("x"));
			ret.put("lat", jsonArr.getJSONObject(0).get("y"));
			mModuleContext.success(ret, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void failBack(JSONObject json) {
		JSONObject err = new JSONObject();
		try {
			err.put("code", json);
			mModuleContext.error(new JSONObject(), err, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void netFailBack() {
		JSONObject err = new JSONObject();
		try {
			err.put("code", -1);
			mModuleContext.error(new JSONObject(), err, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	class LenientCookieSpec extends BrowserCompatSpec {
		public LenientCookieSpec() {
			super();
			registerAttribHandler(ClientCookie.EXPIRES_ATTR,
					new BasicExpiresHandler(DATE_PATTERNS) {
						@Override
						public void parse(SetCookie cookie, String value)
								throws MalformedCookieException {
							value = value.replace("\"", "");
							super.parse(cookie, value);
						}
					});
		}
	}

	public void translateCoord() {
		if (mJsParamsUtil.isGpsCoord(mModuleContext)) {
			translate(CoordType.GPS);
		} else {
			translate(CoordType.COMMON);
		}
	}

	private void translate(CoordType type) {
		CoordinateConverter coordinateConverter = new CoordinateConverter();
		coordinateConverter.from(type);
		LatLng latLng = new LatLng(mJsParamsUtil.lat(mModuleContext),
				mJsParamsUtil.lon(mModuleContext));
		coordinateConverter.coord(latLng);
		successBack(coordinateConverter.convert());
	}

	private void successBack(LatLng latlng) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("lon", latlng.longitude);
			ret.put("lat", latlng.latitude);
			ret.put("status", true);
			mModuleContext.success(ret, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
