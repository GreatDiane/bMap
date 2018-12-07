package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class MapOffLine implements MKOfflineMapListener {

	private UZModuleContext mModuleContext;
	private MKOfflineMap mOffline;
	private Context mContext;

	@Override
	public void onGetOfflineMapState(int type, int state) {
		System.out.println("onGetOfflineMapState   type :" + type + "  state :" + state);
//		type = MKOfflineMap.TYPE_DOWNLOAD_UPDATE;//0
//		type = MKOfflineMap.TYPE_NETWORK_ERROR;//2
//		type = MKOfflineMap.TYPE_NEW_OFFLINE;//6
//		type = MKOfflineMap.TYPE_VER_UPDATE;//4
		listenerCallBack(mModuleContext, type, state);
	}

	public MapOffLine(Context context) {
		this.mContext = context;
	}

	private void initOffLine() {
		if (mOffline == null) {
			SDKInitializer.initialize(mContext.getApplicationContext());
			mOffline = new MKOfflineMap();
			mOffline.init(this);
		}
	}

	public void getHotCityList(UZModuleContext moduleContext) {
		initOffLine();
		ArrayList<MKOLSearchRecord> hotCityList = mOffline.getHotCityList();
		cityListCallBack(moduleContext, hotCityList);
	}

	public void getOfflineCityList(UZModuleContext moduleContext) {
		initOffLine();
		ArrayList<MKOLSearchRecord> offlineCityList = mOffline.getOfflineCityList();
		cityListCallBack(moduleContext, offlineCityList);
	}

	public void searchCityByName(UZModuleContext moduleContext) {
		initOffLine();
		String city = moduleContext.optString("name");
		ArrayList<MKOLSearchRecord> records = mOffline.searchCity(city);
		cityListCallBack(moduleContext, records);
	}

	public void getAllUpdateInfo(UZModuleContext moduleContext) {
		initOffLine();
		ArrayList<MKOLUpdateElement> updateElements = mOffline
				.getAllUpdateInfo();
		updateCallBack(moduleContext, updateElements);
	}

	public void getUpdateInfoByID(UZModuleContext moduleContext) {
		initOffLine();
		int cityId = moduleContext.optInt("cityID");
		MKOLUpdateElement updateElement = mOffline.getUpdateInfo(cityId);
		updateByIdCallBack(moduleContext, updateElement);
	}

	public void startDownload(UZModuleContext moduleContext) {
		initOffLine();
		int cityId = moduleContext.optInt("cityID");
		boolean status = mOffline.start(cityId);
		if (status) {
			successCallBack(moduleContext);
		} else {
			failCallback(moduleContext);
		}
	}

	public void updateOffLine(UZModuleContext moduleContext) {
		initOffLine();
		int cityId = moduleContext.optInt("cityID");
		boolean status = mOffline.update(cityId);
		if (status) {
			successCallBack(moduleContext);
		} else {
			failCallback(moduleContext);
		}
	}

	public void pauseDownload(UZModuleContext moduleContext) {
		if (mOffline != null) {
			int cityId = moduleContext.optInt("cityID");
			boolean status = mOffline.pause(cityId);
			if (status) {
				successCallBack(moduleContext);
			} else {
				failCallback(moduleContext);
			}
		}
	}

	public void removeDownload(UZModuleContext moduleContext) {
		if (mOffline != null) {
			int cityId = moduleContext.optInt("cityID");
			boolean status = mOffline.remove(cityId);
			if (status) {
				successCallBack(moduleContext);
			} else {
				failCallback(moduleContext);
			}
		}
	}

	public void addOfflineListener(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
	}

	public void removeOfflineListener() {
		mModuleContext = null;
	}

	private void listenerCallBack(UZModuleContext moduleContext, int type,
			int state) {
		if (moduleContext != null) {
			JSONObject ret = new JSONObject();
			try {
				ret.put("status", true);
				ret.put("type", type);
				ret.put("state", state);
				moduleContext.success(ret, false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void successCallBack(UZModuleContext moduleContext) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void updateByIdCallBack(UZModuleContext moduleContext,
			MKOLUpdateElement updateElement) {
		JSONObject ret = new JSONObject();
		try {
			if (updateElement != null) {
				ret.put("status", true);
				ret.put("cityInfo", updateJson(updateElement));
			} else {
				ret.put("status", false);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void updateCallBack(UZModuleContext moduleContext,
			ArrayList<MKOLUpdateElement> updateElements) {
		JSONObject ret = new JSONObject();
		try {
			if (updateElements != null) {
				ret.put("status", true);
				JSONArray records = new JSONArray();
				ret.put("records", records);
				for (MKOLUpdateElement record : updateElements) {
					records.put(updateJson(record));
				}
			} else {
				ret.put("status", false);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void failCallback(UZModuleContext moduleContext) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", false);
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONObject updateJson(MKOLUpdateElement record)
			throws JSONException {
		JSONObject city = new JSONObject();
		city.put("name", record.cityName);
		city.put("cityID", record.cityID);
		city.put("size", record.size);
		city.put("serversize", record.serversize);
		city.put("ratio", record.ratio);
		city.put("update", record.update);
		LatLng latLng = record.geoPt;
		if (latLng != null) {
			city.put("lat", latLng.latitude);
			city.put("lon", latLng.longitude);
		}
		city.put("status", record.status);
		return city;
	}

	private void cityListCallBack(UZModuleContext moduleContext, ArrayList<MKOLSearchRecord> hotCityList) {
		JSONObject ret = new JSONObject();
		try {
			if (hotCityList != null) {
				ret.put("status", true);
				JSONArray records = new JSONArray();
				ret.put("records", records);
				for (MKOLSearchRecord record : hotCityList) {
					records.put(cityJson(record));
				}
			} else {
				ret.put("status", false);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONObject cityJson(MKOLSearchRecord record) throws JSONException {
		JSONObject city = new JSONObject();
		city.put("name", record.cityName);
		city.put("cityType", record.cityType);
		city.put("cityID", record.cityID);
		city.put("size", record.dataSize);
		ArrayList<MKOLSearchRecord> childCities = record.childCities;
		if (childCities != null) {
			JSONArray childCityJson = new JSONArray();
			city.put("childCities", childCityJson);
			for (MKOLSearchRecord searchRecord : childCities) {
				JSONObject childCity = new JSONObject();
				childCityJson.put(childCity);
				childCity.put("name", searchRecord.cityName);
				childCity.put("cityID", searchRecord.cityID);
			}
		}
		return city;
	}

}
