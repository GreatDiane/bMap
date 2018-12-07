/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.mode;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.baidu.mapapi.map.Marker;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.util.OtherUtils;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

public class Billboard {
	private static final int width = 165;
	private static final int height = 75;
	private static final int iconSize = 40;
	private static final int iconMarginLeft = 5;
	private static final int iconMarginTop = 18;
	private UZModuleContext moduleContext;
	private Context context;
	private int id;
	private Bitmap bgImg;
	private String title;
	private String subTitle;
	private Bitmap icon;
	private String iconStr;
	private int titleSize;
	private int subTitleSize;
	private int titleColor;
	private int subTitleColor;
	private String iconAlign;
	private int maxWidth;
	private MapOverlay mapOverlay;
	private Marker marker;
	private int aWidth;
	private int aHeight;

	public Billboard(UZModuleContext moduleContext, Context context, int id,
			Bitmap bgImg, String title, String subTitle, Bitmap icon,
			String iconStr, int titleSize, int subTitleSize, int titleColor,
			int subTitleColor, String iconAlign, int maxWidth,int w, int h,
			MapOverlay mapOverlay) {
		this.moduleContext = moduleContext;
		this.context = context;
		this.id = id;
		this.bgImg = bgImg;
		this.title = title;
		this.subTitle = subTitle;
		this.icon = icon;
		this.iconStr = iconStr;
		this.titleSize = titleSize;
		this.subTitleSize = subTitleSize;
		this.titleColor = titleColor;
		this.subTitleColor = subTitleColor;
		this.iconAlign = iconAlign;
		this.maxWidth = maxWidth;
		this.mapOverlay = mapOverlay;
		this.aWidth = w;
		this.aHeight = h;
	}

	@SuppressWarnings("deprecation")
	public View billboardView() {
		LinearLayout billboardLayout = new LinearLayout(context);
		LayoutParams layoutParams = null;
		if (bgImg != null) {
			if (aWidth == -1 || aHeight == -1) {
				billboardLayout.setBackgroundDrawable(new BitmapDrawable(bgImg));
				layoutParams = new LayoutParams(UZCoreUtil.dipToPix(width), UZCoreUtil.dipToPix(height));
			}else {
				Bitmap newBitmap = JsParamsUtil.getInstance().createNewBitmap(bgImg, aWidth, aHeight);
				if (newBitmap != null) {
					billboardLayout.setBackgroundDrawable(new BitmapDrawable(newBitmap));
					layoutParams = new LayoutParams(UZCoreUtil.dipToPix(width), UZCoreUtil.dipToPix(height));
				}
			}
			
		} else {
			billboardLayout.setBackgroundResource(UZResourcesIDFinder
					.getResDrawableID("mo_bmap_popupmap"));
			layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
					UZCoreUtil.dipToPix(height));
		}
		billboardLayout.setLayoutParams(layoutParams);
		billboardLayout.setOrientation(LinearLayout.HORIZONTAL);
		if (getIconAlign().equals("left")) {
			if (iconStr != null && !iconStr.isEmpty())
				billboardLayout.addView(icon());
			billboardLayout.addView(titleLayout());
		} else {
			billboardLayout.addView(titleLayout());
			if (iconStr != null && !iconStr.isEmpty())
				billboardLayout.addView(icon());
		}
		return billboardLayout;
	}

	@SuppressWarnings("deprecation")
	public View billboardView(ImageView icon) {
		LinearLayout billboardLayout = new LinearLayout(context);
		LayoutParams layoutParams = null;
		if (bgImg != null) {
			if (aWidth == -1 || aHeight == -1) {
				billboardLayout.setBackgroundDrawable(new BitmapDrawable(bgImg));
				layoutParams = new LayoutParams(UZCoreUtil.dipToPix(100),
						UZCoreUtil.dipToPix(100));
			}else {
				Bitmap newBitmap = JsParamsUtil.getInstance().createNewBitmap(bgImg, aWidth, aHeight);
				if (newBitmap != null) {
					billboardLayout.setBackgroundDrawable(new BitmapDrawable(newBitmap));
					layoutParams = new LayoutParams(UZCoreUtil.dipToPix(100),
							UZCoreUtil.dipToPix(100));
				}
			}
		} else {
			billboardLayout.setBackgroundResource(UZResourcesIDFinder
					.getResDrawableID("mo_bmap_popupmap"));
			layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
					UZCoreUtil.dipToPix(height));
		}
		//billboardLayout.setLayoutParams(layoutParams);
		billboardLayout.setOrientation(LinearLayout.HORIZONTAL);
		if (getIconAlign().equals("left")) {
			if (iconStr != null && !iconStr.isEmpty())
				billboardLayout.addView(icon);
			billboardLayout.addView(titleLayout());
		} else {
			billboardLayout.addView(titleLayout());
			if (iconStr != null && !iconStr.isEmpty())
				billboardLayout.addView(icon());
		}
		return billboardLayout;
	}

	private LinearLayout titleLayout() {
		LinearLayout titleLayout = new LinearLayout(context);
		LayoutParams layoutParams = new LayoutParams(UZCoreUtil.dipToPix(width
				- iconSize - 30), UZCoreUtil.dipToPix(height));
		layoutParams.setMargins(UZCoreUtil.dipToPix(10), 0,
				UZCoreUtil.dipToPix(10), 0);
		titleLayout.setLayoutParams(layoutParams);
		titleLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout titleLayout2 = new LinearLayout(context);
		LayoutParams layoutParams2 = new LayoutParams(UZCoreUtil.dipToPix(width
				- iconSize - 30), UZCoreUtil.dipToPix(height-10));
		titleLayout2.setGravity(Gravity.CENTER_VERTICAL);
		titleLayout2.setOrientation(LinearLayout.VERTICAL);
		titleLayout2.setLayoutParams(layoutParams2);
		titleLayout.addView(titleLayout2);
		titleLayout2.addView(title());
		titleLayout2.addView(subTitle());
		return titleLayout;
	}

	private TextView title() {
		TextView title = new TextView(context);
		title.setSingleLine();
		title.setEllipsize(TruncateAt.END);
		title.setMaxWidth(UZCoreUtil.dipToPix(maxWidth) - 2 * iconMarginLeft
				+ iconSize);
		title.setText(getTitle());
		title.setTextColor(getTitleColor());
		title.setTextSize(getTitleSize());
		title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		return title;
	}

	private TextView subTitle() {
		TextView title = new TextView(context);
		title.setSingleLine();
		title.setEllipsize(TruncateAt.END);
		title.setMaxWidth(UZCoreUtil.dipToPix(maxWidth) - 2 * iconMarginLeft
				+ iconSize);
		title.setText(getSubTitle());
		title.setTextColor(getSubTitleColor());
		title.setTextSize(getSubTitleSize());
		title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		return title;
	}

	public void startLoadIcon() {
		icon();
	}

	private ImageView icon() {
		ImageView icon = new ImageView(context);
		LayoutParams layoutParams = new LayoutParams(
				UZCoreUtil.dipToPix(iconSize), UZCoreUtil.dipToPix(iconSize));
		layoutParams.setMargins(UZCoreUtil.dipToPix(iconMarginLeft),
				UZCoreUtil.dipToPix(iconMarginTop),
				UZCoreUtil.dipToPix(iconMarginLeft),
				UZCoreUtil.dipToPix(iconMarginTop));
		icon.setLayoutParams(layoutParams);
		if (getIcon() != null) {
			icon.setImageBitmap(getIcon());
		} else {
			getImgShowUtil().display(icon, iconStr, getLoadCallBack());
		}
		return icon;
	}

	private BitmapUtils getImgShowUtil() {
		BitmapUtils bitmapUtils = new BitmapUtils(context,
				OtherUtils.getDiskCacheDir(context, ""));
		bitmapUtils.configDiskCacheEnabled(true);
		bitmapUtils.configMemoryCacheEnabled(true);
		return bitmapUtils;
	}

	private BitmapLoadCallBack<View> getLoadCallBack() {
		return new BitmapLoadCallBack<View>() {
			@Override
			public void onLoadCompleted(View container, String uri,
					Bitmap bitmap, BitmapDisplayConfig displayConfig,
					BitmapLoadFrom from) {
				icon = bitmap;
				((ImageView) container).setImageBitmap(bitmap);
				mapOverlay.addBillboard(moduleContext, Billboard.this,
						(ImageView) container);
			}

			@Override
			public void onLoading(View container, String uri,
					BitmapDisplayConfig config, long total, long current) {
			}

			@Override
			public void onLoadFailed(View container, String uri,
					Drawable failedDrawable) {
			}
		};
	}

	public UZModuleContext getModuleContext() {
		return moduleContext;
	}

	public void setModuleContext(UZModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Bitmap getBgImg() {
		return bgImg;
	}

	public void setBgImg(Bitmap bgImg) {
		this.bgImg = bgImg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public String getIconStr() {
		return iconStr;
	}

	public void setIconStr(String iconStr) {
		this.iconStr = iconStr;
	}

	public int getTitleSize() {
		return titleSize;
	}

	public void setTitleSize(int titleSize) {
		this.titleSize = titleSize;
	}

	public int getSubTitleSize() {
		return subTitleSize;
	}

	public void setSubTitleSize(int subTitleSize) {
		this.subTitleSize = subTitleSize;
	}

	public int getTitleColor() {
		return titleColor;
	}

	public void setTitleColor(int titleColor) {
		this.titleColor = titleColor;
	}

	public int getSubTitleColor() {
		return subTitleColor;
	}

	public void setSubTitleColor(int subTitleColor) {
		this.subTitleColor = subTitleColor;
	}

	public String getIconAlign() {
		return iconAlign;
	}

	public void setIconAlign(String iconAlign) {
		this.iconAlign = iconAlign;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

}