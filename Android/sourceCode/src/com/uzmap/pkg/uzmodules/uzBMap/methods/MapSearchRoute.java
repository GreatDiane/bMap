/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.methods;

import java.util.List;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.callback.BusRoutePlanCallBack;
import com.uzmap.pkg.uzmodules.uzBMap.callback.DriveRoutePlanCallBack;
import com.uzmap.pkg.uzmodules.uzBMap.callback.WalkRoutePlanCallBack;
import com.uzmap.pkg.uzmodules.uzBMap.geocoder.GeoCoderInterface;
import com.uzmap.pkg.uzmodules.uzBMap.geocoder.GeoCoderUtil;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class MapSearchRoute implements OnGetRoutePlanResultListener,
		GeoCoderInterface {
	private JsParamsUtil mJsParamsUtil;
	private UZModuleContext mModuleContext;
	private List<DrivingRouteLine> mCarPlans;
	private List<WalkingRouteLine> mWalkPlans;
	private List<TransitRouteLine> mBusPlans;
	private GeoCoderUtil mGeoCoderUtil;
	private String mSearchType;
	private int mSearchID;
	private PlanNode mStart;
	private PlanNode mEnd;

	public MapSearchRoute(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		mJsParamsUtil = JsParamsUtil.getInstance();
	}

	public void searchRoute() {
		mSearchID = mModuleContext.optInt("id");
		mSearchType = mJsParamsUtil.routeType(mModuleContext);
		PlanNode start = mJsParamsUtil.routePoint(mModuleContext, "start");
		PlanNode end = mJsParamsUtil.routePoint(mModuleContext, "end");
		search(mSearchType, start, end);
	}

	private void search(String type, PlanNode start, PlanNode end) {
		RoutePlanSearch search = createSearch();
		if (type.equals("drive")) {
			search.drivingSearch(new DrivingRoutePlanOption().from(start)
					.to(end)
					.policy(mJsParamsUtil.getDrivePolicy(mModuleContext)));
		} else if (type.equals("transit")) {
			this.mStart = start;
			this.mEnd = end;
			mGeoCoderUtil = new GeoCoderUtil(this);
			mGeoCoderUtil.coord2address(
					mJsParamsUtil.lat(mModuleContext, "start"),
					mJsParamsUtil.lon(mModuleContext, "start"));
		} else {
			search.walkingSearch(new WalkingRoutePlanOption().from(start).to(
					end));
		}
	}

	private RoutePlanSearch createSearch() {
		RoutePlanSearch search = RoutePlanSearch.newInstance();
		search.setOnGetRoutePlanResultListener(this);
		return search;
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		DriveRoutePlanCallBack callBack = new DriveRoutePlanCallBack();
		boolean flag = callBack.routePlanCallBack(mModuleContext, result);
		if (flag) {
			mCarPlans = callBack.getPlans();
		}
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
		BusRoutePlanCallBack callBack = new BusRoutePlanCallBack();
		boolean flag = callBack.routePlanCallBack(mModuleContext, result);
		if (flag) {
			mBusPlans = callBack.getPlans();
		}
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		WalkRoutePlanCallBack callBack = new WalkRoutePlanCallBack();
		boolean flag = callBack.routePlanCallBack(mModuleContext, result);
		if (flag) {
			mWalkPlans = callBack.getPlans();
		}
	}

	public List<DrivingRouteLine> getCarPlans() {
		return mCarPlans;
	}

	public List<WalkingRouteLine> getWalkPlans() {
		return mWalkPlans;
	}

	public List<TransitRouteLine> getBusPlans() {
		return mBusPlans;
	}

	public String getSearchType() {
		return mSearchType;
	}

	public int getSearchID() {
		return mSearchID;
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		RoutePlanSearch search = createSearch();
		search.transitSearch(new TransitRoutePlanOption().from(mStart).to(mEnd)
				.policy(mJsParamsUtil.getBusPolicy(mModuleContext))
				.city(result.getAddressDetail().city));
	}

	@Override
	public void onGetBikingRouteResult(BikingRouteResult arg0) {
		// TODO Auto-generated method stub  新增方法
		
	}

	@Override
	public void onGetIndoorRouteResult(IndoorRouteResult arg0) {
		// TODO Auto-generated method stub  新增方法
		
	}

	@Override
	public void onGetMassTransitRouteResult(MassTransitRouteResult arg0) {
		// TODO Auto-generated method stub  新增方法
		
	}
}
