/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.util.OtherUtils;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOverlay;

public class Bubble {
	private static final int width = 160;
	private static final int height = 80;
	private static final int iconW = 30;
	private static final int iconH = 40;
	private static final int iconMarginLeft = 5;
	private static final int iconMarginTop = 5;
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

	public Bubble(UZModuleContext moduleContext, Context context, int id,
			Bitmap bgImg, String title, String subTitle, Bitmap icon,
			String iconStr, int titleSize, int subTitleSize, int titleColor,
			int subTitleColor, String iconAlign, int maxWidth,
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
	}

	@SuppressWarnings("deprecation")
	public View bubbleView() {
		LinearLayout bubbleLayout = new LinearLayout(context);
		LayoutParams layoutParams = null;
		if (bgImg != null) {
			bubbleLayout.setBackgroundDrawable(new BitmapDrawable(bgImg));
			layoutParams = new LayoutParams(UZCoreUtil.dipToPix(width),
					UZCoreUtil.dipToPix(height));
		} else {
			if (moduleContext.isNull("bgImg")) {
				bubbleLayout.setBackgroundResource(UZResourcesIDFinder
						.getResDrawableID("mo_bmap_popupmap"));
			}
			layoutParams = new LayoutParams(UZCoreUtil.dipToPix(width),
					UZCoreUtil.dipToPix(height));
		}
		bubbleLayout.setLayoutParams(layoutParams);
		bubbleLayout.setOrientation(LinearLayout.HORIZONTAL);
		if (!getIconAlign().equals("right")) {
			if (!TextUtils.isEmpty(iconStr)) {
				bubbleLayout.addView(icon());
			}
			bubbleLayout.addView(titleLayout());
		} else {
			bubbleLayout.addView(titleLayout());
			if (!TextUtils.isEmpty(iconStr)) {
				bubbleLayout.addView(icon());
			}
		}
		return bubbleLayout;
	}

	private LinearLayout titleLayout() {
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapOverlay.bubbleClickCallBack(getId(), "clickContent");
			}
		});
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				UZCoreUtil.dipToPix(height));
		layoutParams.setMargins(UZCoreUtil.dipToPix(10), 0,
				UZCoreUtil.dipToPix(10), 0);
		titleLayout.setGravity(Gravity.CENTER);
		titleLayout.setLayoutParams(layoutParams);
		titleLayout.setOrientation(LinearLayout.VERTICAL);
		titleLayout.addView(title());
		titleLayout.addView(subTitle());
		return titleLayout;
	}

	private TextView title() {
		TextView title = new TextView(context);
		title.setSingleLine();
		title.setEllipsize(TruncateAt.END);
		title.setMaxWidth(UZCoreUtil.dipToPix(maxWidth) - 2 * iconMarginLeft
				+ iconW);
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
				+ iconW);
		title.setText(getSubTitle());
		title.setTextColor(getSubTitleColor());
		title.setTextSize(getSubTitleSize());
		title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		return title;
	}

	private ImageView icon() {
		ImageView icon = new ImageView(context);
		icon.setScaleType(ScaleType.FIT_XY);
		icon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapOverlay.bubbleClickCallBack(getId(), "clickIllus");
			}
		});
		LayoutParams layoutParams = new LayoutParams(
				UZCoreUtil.dipToPix(iconW), UZCoreUtil.dipToPix(iconH));
		layoutParams.setMargins(iconMarginLeft, iconMarginTop, iconMarginLeft,
				iconMarginTop);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		icon.setLayoutParams(layoutParams);
		if (getIcon() != null) {
			icon.setImageBitmap(getIcon());
		} else {
			getImgShowUtil().display(icon, iconStr);
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
}
