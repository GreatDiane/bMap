package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class MapGPSSignal {
	public Context mContext;
	public UZModuleContext mModuleContext;
	public LocationManager mLocationManager;
	private JSONObject mRet;
	private boolean mIsStop = false;

	public void getGPSSnr(UZModuleContext moduleContext, Context context) {
		mIsStop = false;
		mModuleContext = moduleContext;
		mContext = context;
		mRet = new JSONObject();
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			String LocateType = LocationManager.GPS_PROVIDER;
			mLocationManager.getLastKnownLocation(LocateType);
			mLocationManager.addGpsStatusListener(mStatusListener);
			mLocationManager.requestLocationUpdates(LocateType, 5000, 0,
					locationListener);
		} else {
			failCallBack();
		}
	}

	public void stop() {
		mIsStop = true;
	}

	private final GpsStatus.Listener mStatusListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			if (!mIsStop) {
				GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
				switch (event) {
				case GpsStatus.GPS_EVENT_STARTED:
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					getMaxSnr(gpsStatus);
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					break;
				}
			}
		}
	};

	private void getMaxSnr(GpsStatus gpsStatus) {
		Iterable<GpsSatellite> iterable = gpsStatus.getSatellites();
		int count = 0;
		Iterator<GpsSatellite> iterator = iterable.iterator();
		List<Float> snrList = new ArrayList<Float>();
		while (iterator.hasNext()) {
			GpsSatellite satellite = iterator.next();
			float snr = satellite.getSnr();
			snrList.add(snr);
			count++;
		}
		callBack(count, snrList);
	}

	private void callBack(int count, List<Float> snrList) {
		JSONArray snrs = new JSONArray();
		for (Float f : snrList) {
			snrs.put(f);
		}
		try {
			mRet.put("status", true);
			mRet.put("satelliteCount", count);
			mRet.put("snrArray", snrs);
			if (mModuleContext != null)
				mModuleContext.success(mRet, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void failCallBack() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", false);
			if (mModuleContext != null)
				mModuleContext.success(mRet, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.OUT_OF_SERVICE
					|| status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			}
		}
	};
}
