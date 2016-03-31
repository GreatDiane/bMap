/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class UzOrientationListener implements SensorEventListener {
	private Context mContext;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float mLastX;
	private OnOrientationListener onOrientationListener;

	public UzOrientationListener(Context context) {
		this.mContext = context;
	}

	@SuppressWarnings("deprecation")
	public void start() {
		mSensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager != null) {
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		if (mSensor != null) {
			mSensorManager.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	public void stop() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			float x = event.values[SensorManager.DATA_X];
			if (Math.abs(x - mLastX) > 1.0) {
				onOrientationListener.onOrientationChanged(x);
			}
			mLastX = x;

		}
	}

	public void setOnOrientationListener(
			OnOrientationListener onOrientationListener) {
		this.onOrientationListener = onOrientationListener;
	}

	public interface OnOrientationListener {
		void onOrientationChanged(float x);
	}

}
