package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.UzBMap;
import com.uzmap.pkg.uzmodules.uzBMap.clusterutil.clustering.Cluster;
import com.uzmap.pkg.uzmodules.uzBMap.clusterutil.clustering.ClusterItem;
import com.uzmap.pkg.uzmodules.uzBMap.clusterutil.clustering.ClusterManager;
import com.uzmap.pkg.uzmodules.uzBMap.clusterutil.clustering.ClusterManager.OnClusterClickListener;
import com.uzmap.pkg.uzmodules.uzBMap.clusterutil.clustering.ClusterManager.OnClusterItemClickListener;
import com.uzmap.pkg.uzmodules.uzBMap.mode.ClusPoint;
import com.uzmap.pkg.uzmodules.uzBMap.view.ClusterView;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MapCluster implements OnMapLoadedCallback {
	private UzBMap mUzBMap;
	private MapOpen mMap;
	private ClusterManager<MyItem> mClusterManager;
	private UZModuleContext moduleContext;
	private MapStatus ms;
	private Map<String, ClusPoint> mClusMap = new HashMap<String, ClusPoint>();
	private String mPointBg;
	private int mWidth;
	private int mHeight;
	public MapCluster(UzBMap mUzBMap, MapOpen mMap) {
		this.mUzBMap = mUzBMap;
		this.mMap = mMap;
		
		mMap.getBaiduMap().setOnMapLoadedCallback(this);
	}
	
	public void addCluster(UZModuleContext moduleContext) {
		initStyle(moduleContext);
		Object data = moduleContext.optObject("data");
		mClusterManager = new ClusterManager<MapCluster.MyItem>(moduleContext.getContext(), mMap.getBaiduMap());
		//ms = new MapStatus.Builder().target(new LatLng(39.914935, 116.403119)).zoom(8).build();
		//mMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
		if (data instanceof JSONArray) {
			JSONArray dataArray = (JSONArray)data;
			List<MyItem> items = new ArrayList<MapCluster.MyItem>();
			
			for(int i = 0; i < dataArray.length(); i++) {
				JSONObject dataJson = dataArray.optJSONObject(i);
				double lat = dataJson.optDouble("lat");
				double lon = dataJson.optDouble("lon");
				
				String title = dataJson.optString("title");
				String subTitle = dataJson.optString("subtitle");
				String customID = dataJson.optString("customID");
				
				LatLng latLng = new LatLng(lat, lon);
				items.add(new MyItem(latLng, customID));
				mClusMap.put(customID, new ClusPoint(title, subTitle));
			}
			addCluster2Map(items);
			
		}else if (data instanceof String) {
			String dataPath = (String) data;
			new MyAsyncTask().execute(moduleContext.makeRealPath(dataPath));
		}
	}
	
	private void initStyle(UZModuleContext moduleContext) {
		JSONObject stylesJson = moduleContext.optJSONObject("styles");
		if (stylesJson == null) {
			stylesJson = new JSONObject();
		}
		
		JSONObject sizeJson = stylesJson.optJSONObject("size");
		if (sizeJson == null) {
			sizeJson = new JSONObject();
		}
		mWidth = sizeJson.optInt("w", 22);
		mHeight = sizeJson.optInt("h", 22);
		
		JSONObject bgJson = stylesJson.optJSONObject("bg");
		if (bgJson == null) {
			bgJson = new JSONObject();
		}
		mPointBg = bgJson.optString("pointBg");
	}
	
	public void removeCluster() {
		if (mClusterManager != null) {
			mClusterManager.clearItems();
			mClusterManager.cluster();
			mClusMap.clear();
		}
	}
	
	
	public void addClusterListener(UZModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}
	
	private class MyAsyncTask extends AsyncTask<String, Void, List<MyItem>>{

		@Override
		protected List<MyItem> doInBackground(String... params) {
			List<MyItem> list = readItemFromPath(params[0]);
			return list;
		}
		
		@Override
		protected void onPostExecute(List<MyItem> result) {
			super.onPostExecute(result);
			
			addCluster2Map(result);
		}
		
	}
	
	private void addCluster2Map(List<MyItem> list) {
		mClusterManager.addItems(list);
		mMap.getBaiduMap().setOnMapStatusChangeListener(mClusterManager);
		mMap.getBaiduMap().setOnMarkerClickListener(mClusterManager);
		mClusterManager.cluster();
				
//		mClusterManager.setOnClusterClickListener(new OnClusterClickListener<MapCluster.MyItem>() {
//			
//			@Override
//			public boolean onClusterClick(Cluster<MapCluster.MyItem> cluster) {
//				
//				return false;
//			}
//		});
		
		mClusterManager.setOnClusterItemClickListener(new OnClusterItemClickListener<MapCluster.MyItem>() {
			
			@Override
			public boolean onClusterItemClick(MyItem item) {
				try {
					if (moduleContext != null) {
						JSONObject result = new JSONObject();
						result.put("customID", 1);
						moduleContext.success(result, false);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				return false;
			}
		});
		
		mClusterManager.getMarkerCollection().setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker mark) {
				
				ClusPoint clusPoint = mClusMap.get(mark.getZIndex() + "");
				
				int height = mark.getIcon().getBitmap().getHeight();
				View view = LayoutInflater.from(mUzBMap.context()).inflate(UZResourcesIDFinder.getResLayoutID("bmap_cluster_infowindow"), null);
				TextView title = (TextView)view.findViewById(UZResourcesIDFinder.getResIdID("tv_title"));
				title.setText(clusPoint.getTitle());
				TextView subTitle = (TextView)view.findViewById(UZResourcesIDFinder.getResIdID("tv_subtitle"));
				subTitle.setText(clusPoint.getSubTitle());
				InfoWindow infoWindow = new InfoWindow(view, mark.getPosition(), -(height + 2));
				
				mMap.getBaiduMap().showInfoWindow(infoWindow);
				
				try {
					if (moduleContext != null) {
						JSONObject result = new JSONObject();
						result.put("customID", mark.getZIndex());
						moduleContext.success(result, false);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}
	
	private List<MyItem> readItemFromPath(String path){
		List<MyItem> list = new ArrayList<MapCluster.MyItem>();
		try {
			InputStream inputStream = UZUtility.guessInputStream(path);
			if (inputStream != null) {
				String data = UZCoreUtil.readString(inputStream);
				if (!TextUtils.isEmpty(data)) {
					JSONArray dataArray = new JSONArray(data);
					for(int i = 0; i < dataArray.length(); i++) {
						JSONObject dataJson = dataArray.optJSONObject(i);
						double lat = dataJson.optDouble("lat");
						double lon = dataJson.optDouble("lon");
						
						String title = dataJson.optString("title");
						String subTitle = dataJson.optString("subtitle");
						String customID = dataJson.optString("customID");
						
						MyItem item = new MyItem(new LatLng(lat, lon), customID);
						list.add(item);
						mClusMap.put(customID, new ClusPoint(title, subTitle));
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	private class MyItem implements ClusterItem{
		
		private LatLng mPosition;
		
		private String id;
		
		public MyItem(LatLng latLng, String id) {
			this.mPosition = latLng;
			this.id = id;
		}
		
		public MyItem(LatLng latLng) {
			this.mPosition = latLng;
		}

		@Override
		public LatLng getPosition() {
			return mPosition;
		}

		@Override
		public BitmapDescriptor getBitmapDescriptor() {
			if (TextUtils.isEmpty(mPointBg)) {
				return BitmapDescriptorFactory.fromResource(UZResourcesIDFinder.getResDrawableID("mo_bmap_cluster_icon_gcoding"));
			}else {
				if (mPointBg.startsWith("fs://") || mPointBg.startsWith("widget://") || mPointBg.startsWith("/")) {
					String path = mUzBMap.makeRealPath(mPointBg);
					Bitmap bitmap = UZUtility.getLocalImage(path);
					BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
					return bitmapDescriptor;
				}else {
					View view = LayoutInflater.from(mUzBMap.context()).inflate(UZResourcesIDFinder.getResLayoutID("bmap_cluster_view"), null);
					ClusterView clusterView = (ClusterView) view.findViewById(UZResourcesIDFinder.getResIdID("cluster"));
					clusterView.setStyle(mPointBg, mWidth, mHeight);
					BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
					return bitmapDescriptor;
				}
			}
			
		}

		@Override
		public String getClusterItemId() {
			return id;
		}
		
	}

	@Override
	public void onMapLoaded() {
//		ms = new MapStatus.Builder().zoom(9).build();
//		mMap.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
	}
	
}
