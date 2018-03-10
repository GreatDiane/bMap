/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.mode;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

public class Annotation {
	private UZModuleContext moduleContext;
	private int id;
	private LatLng latLng;
	private BitmapDescriptor icon;
	private Marker marker;
	private boolean draggable;

	public Annotation(UZModuleContext moduleContext, int id, LatLng latLng,
			BitmapDescriptor icon, boolean draggable) {
		this.moduleContext = moduleContext;
		this.id = id;
		this.latLng = latLng;
		this.icon = icon;
		this.draggable = draggable;
	}

	public UZModuleContext getModuleContext() {
		return moduleContext;
	}

	public void setModuleContext(UZModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

	public BitmapDescriptor getIcon() {
		return icon;
	}

	public void setIcon(BitmapDescriptor icon) {
		this.icon = icon;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}
}