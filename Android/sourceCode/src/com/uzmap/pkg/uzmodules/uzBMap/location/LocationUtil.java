/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.uzmap.pkg.EntranceActivity;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class LocationUtil implements LocationInterface {
	private LocationClient mLocClient;
	private UzLocationListenner mListener;
	private Context mContext;
	private LocationInterface mInterface;
	private UZModuleContext moduleContext;
	private NotificationManager notificationManager;
	private Notification notification;
	private int mNotificationId;

	public LocationUtil(Context mContext, UZModuleContext moduleContext, LocationInterface mInterface) {
		this.mContext = mContext;
		this.mInterface = mInterface;
		this.moduleContext = moduleContext;
		initLocationClient();
	}

	public void startLocation() {
		if (mLocClient != null) {
			mLocClient.start();
			mLocClient.requestLocation();
		}
	}

	public void stopLocation() {
		if (mLocClient != null) {
			if (notificationManager != null) {
				notificationManager.cancel(mNotificationId);
				mLocClient.disableLocInForeground(true);
			}
			mLocClient.stop();
			mLocClient = null;
		}
	}

	@SuppressLint("NewApi")
	private void initLocationClient() {
		mListener = new UzLocationListenner(this);
		mLocClient = new LocationClient(mContext);
		mLocClient.registerLocationListener(mListener);
		mLocClient.setLocOption(locationOption());
		boolean enableLocInForeground = moduleContext.optBoolean("enableLocInForeground", false);
		if (enableLocInForeground) {
			JSONObject notificationJson = moduleContext.optJSONObject("notification");
			mNotificationId = notificationJson.optInt("id");
			String contentTitle = notificationJson.optString("contentTitle");
			String contentText = moduleContext.optString("contentText");
			
			notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	        //获取一个Notification构造器
	        Notification.Builder builder = new Notification.Builder(mContext);
	        Intent nfIntent = makeIntent(mContext, "");

	        builder.setContentIntent(PendingIntent.
	                getActivity(mContext, 0, nfIntent, 0)) // 设置PendingIntent
	                .setContentTitle(contentTitle) // 设置下拉列表里的标题
	                .setSmallIcon(getIconResId(mContext)) // 设置状态栏内的小图标
	                .setContentText(contentText) // 设置上下文内容
	                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

	        notification = builder.build(); // 获取构建好的Notification
	        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
			
			mLocClient.enableLocInForeground(mNotificationId, notification);
		}
	}
	
	private synchronized Intent makeIntent(Context context, String extra) {
		Intent action = new Intent(Intent.ACTION_MAIN);
		action.addCategory(Intent.CATEGORY_LAUNCHER);
		action.setComponent(new ComponentName(context, EntranceActivity.class));
		action.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		if (!TextUtils.isEmpty(extra)) {
			JSONObject json = new JSONObject();
			try {
				json.put("type", 0);
				json.put("value", extra);
			} catch (Exception e) {

			}
			// UZEventEnum.API_ARGUMENTS
			action.putExtra("api_arguments", json.toString());
		}
		return action;
	}
	
	private int getIconResId(Context context) {
		String pkg = context.getPackageName();
		PackageManager pkm = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pkm.getApplicationInfo(pkg, 0);
			return appInfo.icon;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private LocationClientOption locationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		option.setLocationMode(LocationMode.Hight_Accuracy);
		return option;
	}

	@Override
	public void onReceive(BDLocation location) {
		mInterface.onReceive(location);
	}

	public void onDestory() {
		if (mLocClient != null) {
			mLocClient.stop();
			mLocClient = null;
		}
	}

}
