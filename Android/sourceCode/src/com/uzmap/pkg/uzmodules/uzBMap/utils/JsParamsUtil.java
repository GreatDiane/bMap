/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.TransitRoutePlanOption.TransitPolicy;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.mode.Annotation;

public class JsParamsUtil {
	private static JsParamsUtil instance;

	public static JsParamsUtil getInstance() {
		if (instance == null) {
			instance = new JsParamsUtil();
		}
		return instance;
	}

	public String apiKey(UZModuleContext moduleContext, UZModule module) {
		if (!moduleContext.isNull("apiKey")) {
			return moduleContext.optString("apiKey");
		} else {
			return module.getFeatureValue("bMap", "android_apiKey");
		}
	}

	public int x(UZModuleContext moduleContext) {
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("x", 0);
		}
		return 0;
	}

	public int y(UZModuleContext moduleContext) {
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("y", 0);
		}
		return 0;
	}

	public int w(UZModuleContext moduleContext, Context context) {
		int defaultValue = getScreenWidth((Activity) context);
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("w", defaultValue);
		}
		return defaultValue;
	}

	public int h(UZModuleContext moduleContext, Context context) {
		int defaultValue = getScreenHeight((Activity) context);
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("h", defaultValue);
		}
		return defaultValue;
	}

	public double zoomLevel(UZModuleContext moduleContext) {
		return moduleContext.optDouble("zoomLevel", 10);
	}

	public double level(UZModuleContext moduleContext) {
		return moduleContext.optDouble("level", 10);
	}

	public boolean showUserLocation(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("showUserLocation", true);
	}

	public boolean autoStop(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("autoStop", true);
	}

	public String city(UZModuleContext moduleContext) {
		return moduleContext.optString("city");
	}

	public String line(UZModuleContext moduleContext) {
		return moduleContext.optString("line");
	}

	public String address(UZModuleContext moduleContext) {
		return moduleContext.optString("address");
	}

	public float lat(UZModuleContext moduleContext) {
		return (float) moduleContext.optDouble("lat");
	}

	public float lon(UZModuleContext moduleContext) {
		return (float) moduleContext.optDouble("lon");
	}

	public String mcode(UZModuleContext moduleContext) {
		return moduleContext.optString("mcode");
	}

	public float lat(UZModuleContext moduleContext, String parent) {
		JSONObject parentObj = moduleContext.optJSONObject(parent);
		if (parentObj != null) {
			return (float) parentObj.optDouble("lat");
		}
		return 0;
	}

	public float lon(UZModuleContext moduleContext, String parent) {
		JSONObject parentObj = moduleContext.optJSONObject(parent);
		if (parentObj != null) {
			return (float) parentObj.optDouble("lon");
		}
		return 0;
	}

	public boolean isGpsCoord(UZModuleContext moduleContext) {
		String type = moduleContext.optString("type", "common");
		if (type.equals("gps")) {
			return true;
		}
		return false;
	}

	public boolean isShow(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("isShow", true);
	}

	public String trackingMode(UZModuleContext moduleContext) {
		return moduleContext.optString("trackingMode", "none");
	}

	public String mapType(UZModuleContext moduleContext) {
		return moduleContext.optString("type", "standard");
	}

	public boolean zoomEnable(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("zoomEnable", true);
	}

	public boolean scrollEnable(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("scrollEnable", true);
	}

	public int rotateDegree(UZModuleContext moduleContext) {
		return moduleContext.optInt("degree", 0);
	}

	public int overlookDegree(UZModuleContext moduleContext) {
		return moduleContext.optInt("degree", 0);
	}

	public int controlX(UZModuleContext moduleContext) {
		JSONObject position = moduleContext.optJSONObject("position");
		if (position != null) {
			return UZUtility.dipToPix(position.optInt("x", 0));
		}
		return 0;
	}

	public int controlY(UZModuleContext moduleContext) {
		JSONObject position = moduleContext.optJSONObject("position");
		if (position != null) {
			return UZUtility.dipToPix(position.optInt("y", 0));
		}
		return 0;
	}

	public LatLngBounds latLngBounds(UZModuleContext moduleContext) {
		double ltLon = moduleContext.optDouble("lbLon");
		double ltLat = moduleContext.optDouble("lbLat");
		double rbLon = moduleContext.optDouble("rtLon");
		double rbLat = moduleContext.optDouble("rtLat");
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		return builder.include(new LatLng(ltLat, ltLon))
				.include(new LatLng(rbLat, rbLon)).build();
	}

	public LatLngBounds pointsBounds(UZModuleContext moduleContext) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		JSONArray points = moduleContext.optJSONArray("points");
		if (points != null) {
			for (int i = 0; i < points.length(); i++) {
				double lon = points.optJSONObject(i).optDouble("lon");
				double lat = points.optJSONObject(i).optDouble("lat");
				builder.include(new LatLng(lat, lon));
			}
		}
		return builder.build();
	}
	
	public List<LatLng> pointList(UZModuleContext moduleContext) {
		JSONArray points = moduleContext.optJSONArray("points");
		List<LatLng> list = new ArrayList<LatLng>();
		if (points != null) {
			for (int i = 0; i < points.length(); i++) {
				double lon = points.optJSONObject(i).optDouble("lon");
				double lat = points.optJSONObject(i).optDouble("lat");
				list.add(new LatLng(lat, lon));
			}
		}
		return list;
	}

	public boolean latLngBoundsAnimation(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("animation", true);
	}

	public String eventName(UZModuleContext moduleContext) {
		return moduleContext.optString("name");
	}

	public List<Annotation> annotations(UZModuleContext moduleContext,
			UzBMap uzBMap) {
		String defaultIcon = moduleContext.optString("icon");
		boolean draggable = moduleContext.optBoolean("draggable", false);
		JSONArray annotations = moduleContext.optJSONArray("annotations");
		List<Annotation> annoList = new ArrayList<Annotation>();
		if (annotations != null && annotations.length() > 0) {
			JSONObject annoJson = null;
			for (int i = 0; i < annotations.length(); i++) {
				annoJson = annotations.optJSONObject(i);
				annoList.add(annotation(moduleContext, uzBMap, annoJson,
						defaultIcon, draggable));
			}
			return annoList;
		}
		return null;
	}

	private Annotation annotation(UZModuleContext moduleContext, UzBMap uzBMap,
			JSONObject annoJson, String defaultIcon, boolean draggable) {
		int id = annoJson.optInt("id");
		double lon = annoJson.optDouble("lon");
		double lat = annoJson.optDouble("lat");
		String icon = annoIcon(annoJson, defaultIcon);
		boolean unitDraggable = annoJson.optBoolean("draggable", draggable);
		return new Annotation(moduleContext, id, new LatLng(lat, lon),
				createIcon(uzBMap, icon), unitDraggable);
	}

	private String annoIcon(JSONObject annoJson, String defaultIcon) {
		String icon = annoJson.optString("icon");
		if (annoJson.isNull("icon")) {
			icon = defaultIcon;
		}
		return icon;
	}

	private BitmapDescriptor createIcon(UzBMap uzBMap, String iconPath) {
		String realPath = uzBMap.makeRealPath(iconPath);
		Bitmap bitmap = getBitmap(realPath);
		if (bitmap == null) {
			int id = UZResourcesIDFinder
					.getResDrawableID("mo_bmap_icon_gcoding");
			return BitmapDescriptorFactory.fromResource(id);
		}
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}

	public int bubbleId(UZModuleContext moduleContext) {
		return moduleContext.optInt("id");
	}

	public String bubbleTitle(UZModuleContext moduleContext) {
		JSONObject content = moduleContext.optJSONObject("content");
		if (content != null) {
			return content.optString("title");
		}
		return null;
	}

	public String bubbleSubTitle(UZModuleContext moduleContext) {
		JSONObject content = moduleContext.optJSONObject("content");
		if (content != null) {
			return content.optString("subTitle");
		}
		return null;
	}

	public String bubbleIllusPath(UZModuleContext moduleContext) {
		JSONObject content = moduleContext.optJSONObject("content");
		if (content != null) {
			return content.optString("illus");
		}
		return null;
	}

	public Bitmap bubbleIllus(UZModuleContext moduleContext, UzBMap uzBMap) {
		String imgPath = bubbleIllusPath(moduleContext);
		if (imgPath != null && !imgPath.startsWith("http")) {
			return getBitmap(uzBMap.makeRealPath(imgPath));
		}
		return null;
	}

	public Bitmap bubbleBgImg(UZModuleContext moduleContext, UzBMap uzBMap) {
		String imgPath = moduleContext.optString("bgImg");
		if (!moduleContext.isNull("bgImg")) {
			return getBitmap(uzBMap.makeRealPath(imgPath));
		}
		return null;
	}

	public int bubbleTitleColor(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return UZUtility.parseCssColor(styles.optString("titleColor",
					"#000"));
		}
		return UZUtility.parseCssColor("#000");
	}

	public int bubbleTitleSize(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return styles.optInt("titleSize", 16);
		}
		return 16;
	}

	public int bubbleSubTitleColor(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return UZUtility.parseCssColor(styles.optString("subTitleColor",
					"#000"));
		}
		return UZUtility.parseCssColor("#000");
	}

	public int bubbleSubTitleSize(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return styles.optInt("subTitleSize", 16);
		}
		return 16;
	}

	public String bubbleIconAlign(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return styles.optString("illusAlign", "left");
		}
		return "left";
	}

	public boolean isBillboardNetIcon(UZModuleContext moduleContext) {
		JSONObject content = moduleContext.optJSONObject("content");
		if (content != null) {
			String url = content.optString("illus");
			if (url != null && url.startsWith("http")) {
				return true;
			}
		}
		return false;
	}

	public List<Integer> removeOverlayIds(UZModuleContext moduleContext) {
		List<Integer> list = new ArrayList<Integer>();
		JSONArray ids = moduleContext.optJSONArray("ids");
		if (ids != null && ids.length() > 0) {
			for (int i = 0; i < ids.length(); i++) {
				list.add(ids.optInt(i));
			}
			return list;
		}
		return null;
	}

	public List<LatLng> overlayPoints(UZModuleContext moduleContext) {
		List<LatLng> points = new ArrayList<LatLng>();
		JSONArray pointsArray = moduleContext.optJSONArray("points");
		if (pointsArray != null && pointsArray.length() > 0) {
			JSONObject point = null;
			for (int i = 0; i < pointsArray.length(); i++) {
				point = pointsArray.optJSONObject(i);
				points.add(new LatLng(point.optDouble("lat"), point
						.optDouble("lon")));
			}
			return points;
		}
		return null;
	}

	public int overlayColor(UZModuleContext moduleContext) {
		String defaultValue = "#000";
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return UZUtility.parseCssColor(styles.optString("borderColor",
					defaultValue));
		}
		return UZUtility.parseCssColor(defaultValue);
	}

	public int overlayFillColor(UZModuleContext moduleContext) {
		String defaultValue = "#000";
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return UZUtility.parseCssColor(styles.optString("fillColor",
					defaultValue));
		}
		return UZUtility.parseCssColor(defaultValue);
	}

	public boolean lineDash(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return styles.optBoolean("lineDash", false);
		}
		return false;
	}

	public int overlayWidth(UZModuleContext moduleContext) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			return styles.optInt("borderWidth", 2);
		}
		return 2;
	}

	public Bitmap overlayImg(UZModuleContext moduleContext, UZModule module) {
		String imgPath = moduleContext.optString("imgPath");
		return getBitmap(module.makeRealPath(imgPath));
	}

	public Bitmap dashImg(UZModuleContext moduleContext, UZModule module) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (styles != null) {
			String imgPath = styles.optString("dashImg");
			return getBitmap(module.makeRealPath(imgPath));
		}
		return null;
	}

	public float overlayImgOpacity(UZModuleContext moduleContext) {
		return (float) moduleContext.optDouble("opacity", 1);
	}

	public String routeType(UZModuleContext moduleContext) {
		return moduleContext.optString("type");
	}

	public String routePolicy(UZModuleContext moduleContext) {
		return moduleContext.optString("policy", "ebus_time_first");
	}

	public TransitPolicy getBusPolicy(UZModuleContext moduleContext) {
		String policy = routePolicy(moduleContext);
		TransitPolicy transitPolicy = null;
		if (policy.equals("ebus_no_subway")) {
			transitPolicy = TransitPolicy.EBUS_NO_SUBWAY;
		} else if (policy.equals("ebus_time_first")) {
			transitPolicy = TransitPolicy.EBUS_TIME_FIRST;
		} else if (policy.equals("ebus_transfer_first")) {
			transitPolicy = TransitPolicy.EBUS_TRANSFER_FIRST;
		} else if (policy.equals("ebus_walk_first")) {
			transitPolicy = TransitPolicy.EBUS_WALK_FIRST;
		}
		return transitPolicy;
	}

	public DrivingPolicy getDrivePolicy(UZModuleContext moduleContext) {
		String policy = routePolicy(moduleContext);
		DrivingPolicy transitPolicy = null;
		if (policy.equals("ecar_fee_first")) {
			transitPolicy = DrivingPolicy.ECAR_FEE_FIRST;
		} else if (policy.equals("ecar_dis_first")) {
			transitPolicy = DrivingPolicy.ECAR_DIS_FIRST;
		} else if (policy.equals("ecar_time_first")) {
			transitPolicy = DrivingPolicy.ECAR_TIME_FIRST;
		} else if (policy.equals("ecar_avoid_jam")) {
			transitPolicy = DrivingPolicy.ECAR_AVOID_JAM;
		}
		return transitPolicy;
	}

	public PlanNode routePoint(UZModuleContext moduleContext, String type) {
		if (!moduleContext.isNull(type)) {
			JSONObject point = moduleContext.optJSONObject(type);
			if (!point.isNull("lat") && !point.isNull("lon")) {
				double lat = point.optDouble("lat");
				double lon = point.optDouble("lon");
				return PlanNode.withLocation(new LatLng(lat, lon));
			} else if (!point.isNull("city") && !point.isNull("name")) {
				String city = point.optString("city");
				String name = point.optString("name");
				return PlanNode.withCityNameAndPlaceName(city, name);
			}
		}
		return null;
	}

	public String routeCity(UZModuleContext moduleContext, String type) {
		if (!moduleContext.isNull(type)) {
			JSONObject point = moduleContext.optJSONObject(type);
			if (!point.isNull("city")) {
				return point.optString("city");
			}
		}
		return null;
	}

	public Bitmap nodeIcon(UZModuleContext moduleContext, UZModule module,
			String name) {
		JSONObject styles = moduleContext.optJSONObject("styles");
		if (!moduleContext.isNull("styles")) {
			JSONObject start = styles.optJSONObject(name);
			if (start != null) {
				return getBitmap(module.makeRealPath(start.optString("icon")));
			}
		}
		return null;
	}

	public Bitmap getBitmap(String path) {
		Bitmap bitmap = null;
		InputStream input = null;
		try {
			input = UZUtility.guessInputStream(path);
			bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public int getScreenWidth(Activity act) {
		DisplayMetrics metric = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return UZCoreUtil.pixToDip(metric.widthPixels);
	}

	public int getScreenHeight(Activity act) {
		DisplayMetrics metric = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return UZCoreUtil.pixToDip(metric.heightPixels);
	}
}
