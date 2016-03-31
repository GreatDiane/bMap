/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapBusLine;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapDrawRoute;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapEventListener;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGeoCoder;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGeometry;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapGoogleCoords;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapLocation;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOpen;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapPoi;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSearchRoute;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSetcenter;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapSimple;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class UzBMap extends UZModule {
	public static boolean INITIALIZED = false;
	private MapOpen mMap;
	private MapLocation mLocation;
	private MapOverlay mMapOverlay;
	private MapGeometry mMapGeometry;
	private Map<Integer, MapSearchRoute> mSearchRouteMap;
	private Map<Integer, OverlayManager> mRouteMap;
	private Map<Integer, BusLineOverlay> mBusLineMap;

	public UzBMap(UZWebView webView) {
		super(webView);
	}

	public void jsmethod_open(UZModuleContext moduleContext) {
		if (mMap == null) {
			if(!INITIALIZED){
				SDKInitializer.initialize(mContext.getApplication());
				INITIALIZED = true;
			}
			mMap = new MapOpen(this, moduleContext, mContext);
			mMap.open();
			new MapEventListener(moduleContext, mMap, false)
					.addDefaultEventListener();
		} else {
			if (mMap.getMapView().getVisibility() == View.GONE) {
				mMap.getMapView().setVisibility(View.VISIBLE);
			}
		}
	}

	public void jsmethod_close(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.close();
			mMap = null;
			mMapOverlay = null;
		}
	}

	public void jsmethod_show(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.show();
		}
	}

	public void jsmethod_hide(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.hide();
		}
	}

	public void jsmethod_getLocation(UZModuleContext moduleContext) {
		if (mLocation != null) {
			mLocation.stopLocation();
		}
		mLocation = new MapLocation(moduleContext, mContext);
		mLocation.startLocation();
	}

	public void jsmethod_stopLocation(UZModuleContext moduleContext) {
		if (mLocation != null) {
			mLocation.stopLocation();
		}
	}

	public void jsmethod_getCoordsFromName(UZModuleContext moduleContext) {
		new MapGeoCoder(moduleContext).address2coord();
	}

	public void jsmethod_getNameFromCoords(UZModuleContext moduleContext) {
		new MapGeoCoder(moduleContext).coord2address();
	}

	public void jsmethod_getDistance(UZModuleContext moduleContext) {
		new MapSimple(moduleContext).getDistance();
	}

	public void jsmethod_showUserLocation(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.showUserLocation(moduleContext);
		}
	}

	public void jsmethod_setCenter(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapSetcenter(moduleContext, mContext, mMap).setCenter();
		}
	}

	public void jsmethod_setZoomLevel(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setZoomLevel(JsParamsUtil.getInstance().level(moduleContext));
		}
	}

	public void jsmethod_setMapAttr(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setMapType(jsParamsUtil.mapType(moduleContext));
			mMap.setZoomEnable(jsParamsUtil.zoomEnable(moduleContext));
			mMap.setScrollEnable(jsParamsUtil.scrollEnable(moduleContext));
		}
	}

	public void jsmethod_setRotation(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setRotation(jsParamsUtil.rotateDegree(moduleContext));
		}
	}

	public void jsmethod_setOverlook(UZModuleContext moduleContext) {
		if (mMap != null) {
			JsParamsUtil jsParamsUtil = JsParamsUtil.getInstance();
			mMap.setOverlook(jsParamsUtil.overlookDegree(moduleContext));
		}
	}

	public void jsmethod_setScaleBar(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setScaleBar(moduleContext);
		}
	}

	public void jsmethod_setCompass(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setCompassPosition(moduleContext);
		}
	}

	public void jsmethod_setTraffic(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean traffic = moduleContext.optBoolean("traffic", true);
			mMap.getBaiduMap().setTrafficEnabled(traffic);
		}
	}

	public void jsmethod_setHeatMap(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean heatMap = moduleContext.optBoolean("heatMap", true);
			if (heatMap) {
				mMap.addHeatMap();
			} else {
				mMap.removeHeatMap();
			}
		}
	}

	public void jsmethod_setBuilding(UZModuleContext moduleContext) {
		if (mMap != null) {
			boolean building = moduleContext.optBoolean("building", true);
			mMap.setBuilding(building);
		}
	}

	public void jsmethod_setRegion(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.setRegion(moduleContext);
		}
	}

	public void jsmethod_getRegion(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.getRegion(moduleContext);
		}
	}

	public void jsmethod_transCoords(UZModuleContext moduleContext) {
		new MapGoogleCoords(moduleContext, this).translateCoord();
	}

	public void jsmethod_zoomIn(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.zoomIn();
		}
	}

	public void jsmethod_zoomOut(UZModuleContext moduleContext) {
		if (mMap != null) {
			mMap.zoomOut();
		}
	}

	public void jsmethod_addEventListener(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapEventListener(moduleContext, mMap, true).addEventListener();
		}
	}

	public void jsmethod_removeEventListener(UZModuleContext moduleContext) {
		if (mMap != null) {
			new MapEventListener(moduleContext, mMap, false).addEventListener();
		}
	}

	public void jsmethod_addAnnotations(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.addAnnotations(moduleContext);
		}
	}

	public void jsmethod_getAnnotationCoords(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.getAnnotationCoords(moduleContext);
			}
		}
	}

	public void jsmethod_setAnnotationCoords(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.setAnnotationCoords(moduleContext);
			}
		}
	}

	public void jsmethod_setBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.setBubble(moduleContext);
		}
	}

	public void jsmethod_popupBubble(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.popupBubble(moduleContext);
			}
		}
	}

	public void jsmethod_addBillboard(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay == null) {
				mMapOverlay = new MapOverlay(this, mMap);
			}
			mMapOverlay.addBillboard(moduleContext);
		}
	}

	public void jsmethod_removeAnnotations(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapOverlay != null) {
				mMapOverlay.removeOverlay(moduleContext);
			}
		}
	}

	public void jsmethod_addLine(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addLine(moduleContext);
		}
	}

	public void jsmethod_addPolygon(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addPolygon(moduleContext);
		}
	}

	public void jsmethod_addArc(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addArc(moduleContext);
		}
	}

	public void jsmethod_addCircle(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addCircle(moduleContext);
		}
	}

	public void jsmethod_addImg(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry == null) {
				mMapGeometry = new MapGeometry(this, mMap.getBaiduMap());
			}
			mMapGeometry.addImg(moduleContext);
		}
	}

	public void jsmethod_removeOverlay(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mMapGeometry != null) {
				mMapGeometry.removeOverlays(moduleContext);
			}
		}
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_searchRoute(UZModuleContext moduleContext) {
		if (mSearchRouteMap == null) {
			mSearchRouteMap = new HashMap<Integer, MapSearchRoute>();
		}
		MapSearchRoute mapSearchRoute = new MapSearchRoute(moduleContext);
		mapSearchRoute.searchRoute();
		mSearchRouteMap.put(mapSearchRoute.getSearchID(), mapSearchRoute);
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_drawRoute(UZModuleContext moduleContext) {
		if (mMap != null && mSearchRouteMap != null) {
			if (mRouteMap == null) {
				mRouteMap = new HashMap<Integer, OverlayManager>();
			}
			int id = moduleContext.optInt("id");
			OverlayManager routeOverlay = new MapDrawRoute(this, moduleContext,
					mSearchRouteMap, mMap.getBaiduMap())
					.drawRoute(moduleContext);
			mRouteMap.put(id, routeOverlay);
		}
	}

	public void jsmethod_removeRoute(UZModuleContext moduleContext) {
		if (mMap != null && mRouteMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(
					moduleContext);
			if (ids != null && ids.size() > 0) {
				OverlayManager routeOverlay = null;
				for (int id : ids) {
					routeOverlay = mRouteMap.get(id);
					if (routeOverlay != null) {
						routeOverlay.removeFromMap();
					}
				}
			}
		}
	}

	public void jsmethod_searchBusRoute(UZModuleContext moduleContext) {
		new MapBusLine().busLine(moduleContext);
	}

	@SuppressLint("UseSparseArrays")
	public void jsmethod_drawBusRoute(UZModuleContext moduleContext) {
		if (mMap != null) {
			if (mBusLineMap == null) {
				mBusLineMap = new HashMap<Integer, BusLineOverlay>();
			}
			new MapBusLine().drawBusLine(moduleContext, mBusLineMap,
					mMap.getBaiduMap());
		}
	}

	public void jsmethod_removeBusRoute(UZModuleContext moduleContext) {
		if (mBusLineMap != null) {
			List<Integer> ids = JsParamsUtil.getInstance().removeOverlayIds(
					moduleContext);
			if (ids != null && ids.size() > 0) {
				OverlayManager routeOverlay = null;
				for (int id : ids) {
					routeOverlay = mBusLineMap.get(id);
					if (routeOverlay != null) {
						routeOverlay.removeFromMap();
					}
				}
			}
		}
	}

	public void jsmethod_searchInCity(UZModuleContext moduleContext) {
		new MapPoi().searchInCity(moduleContext);
	}

	public void jsmethod_searchNearby(UZModuleContext moduleContext) {
		new MapPoi().searchNearby(moduleContext);
	}

	public void jsmethod_searchInBounds(UZModuleContext moduleContext) {
		new MapPoi().searchInBounds(moduleContext);
	}

	public void jsmethod_autocomplete(UZModuleContext moduleContext) {
		new MapPoi().autoComplete(moduleContext);
	}

	@Override
	protected void onClean() {
		super.onClean();
		cleanMap();
		cleanLocation();
	}

	private void cleanMap() {
		if (mMap != null) {
			mMap.onDestory();
			mMap = null;
		}
	}

	private void cleanLocation() {
		if (mLocation != null) {
			mLocation.onDestory();
			mLocation = null;
		}
	}

	// 解决当openWin，fixed为false时，部分手机地图不显示问题
	@SuppressLint("NewApi")
	private void makeMapShowSometimes() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View view = (View) mMap.getMapView().getParent();
				if (view != null) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			}
		});
	}
}
