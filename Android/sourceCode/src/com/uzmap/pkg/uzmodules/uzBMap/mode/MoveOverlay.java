package com.uzmap.pkg.uzmodules.uzBMap.mode;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class MoveOverlay {
	private Marker maker;
	private double duration;
	private LatLng startPoint;
	private LatLng endPoint;
	private float unitAngle;
	private double currTime;
	private double currRotateTime;
	private UZModuleContext moduleContext;

	public MoveOverlay(UZModuleContext moduleContext, Marker maker,
			double duration, LatLng endPoint) {
		this.moduleContext = moduleContext;
		this.maker = maker;
		this.startPoint = maker.getPosition();
		this.duration = duration;
		this.endPoint = endPoint;
	}

	public UZModuleContext getModuleContext() {
		return moduleContext;
	}

	public void setModuleContext(UZModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public Marker getMaker() {
		return maker;
	}

	public void setMaker(Marker maker) {
		this.maker = maker;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public LatLng getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(LatLng endPoint) {
		this.endPoint = endPoint;
	}

	public double getCurrTime() {
		return currTime;
	}

	public void setCurrTime(double currTime) {
		this.currTime = currTime;
	}

	public double getCurrRotateTime() {
		return currRotateTime;
	}

	public void setCurrRotateTime(double currRotateTime) {
		this.currRotateTime = currRotateTime;
	}

	public float getUnitAngle() {
		return unitAngle;
	}

	public void setUnitAngle(float unitAngle) {
		this.unitAngle = unitAngle;
	}

	public LatLng getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(LatLng startPoint) {
		this.startPoint = startPoint;
	}
}