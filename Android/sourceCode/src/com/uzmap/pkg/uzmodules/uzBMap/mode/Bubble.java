/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.uzmap.pkg.uzmodules.uzBMap.mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.a.l;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.util.OtherUtils;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.uzmap.pkg.uzmodules.uzBMap.BMapConfig;
import com.uzmap.pkg.uzmodules.uzBMap.methods.MapOverlay;
import com.uzmap.pkg.uzmodules.uzBMap.utils.JsParamsUtil;

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
	private boolean isWebBubble;
	private int bWidth;
	private int bHeight;

	public Bubble(UZModuleContext moduleContext, Context context, boolean isWebBubble, int id,
			Bitmap bgImg, String title, String subTitle, Bitmap icon,
			String iconStr, int titleSize, int subTitleSize, int titleColor,
			int subTitleColor, String iconAlign, int maxWidth,
			MapOverlay mapOverlay, int width, int height) {
		this.moduleContext = moduleContext;
		this.context = context;
		this.id = id;
		this.isWebBubble = isWebBubble;
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
		this.bWidth = width;
		this.bHeight = height;
	}
	
	private String url;
	private String data;
	private int wWidth;
	private int wHeight;
	private String bg;
	public Bubble(UZModuleContext moduleContext, Context context, boolean isWebBubble, int id, String url, String data, int width, int height, String bg) {
		this.moduleContext = moduleContext;
		this.context = context;
		this.id = id;
		this.isWebBubble = isWebBubble;
		this.url = url;
		this.data = data;
		this.wWidth = width;
		this.wHeight = height;
		this.bg = bg;
	}

	@SuppressWarnings("deprecation")
	public View bubbleView() {
		if (!isWebBubble) {
			LinearLayout bubbleLayout = new LinearLayout(context);
			LayoutParams layoutParams = null;
			if (bgImg != null) {
				if (bWidth == -1 || bHeight == -1) {
					bubbleLayout.setBackgroundDrawable(new BitmapDrawable(bgImg));
					layoutParams = new LayoutParams(UZUtility.dipToPix(width), UZUtility.dipToPix(height));
				}else {
					Bitmap newBitmap = JsParamsUtil.getInstance().createNewBitmap(bgImg, bWidth, bHeight);
					if (newBitmap != null) {
						bubbleLayout.setBackgroundDrawable(new BitmapDrawable(newBitmap));
						layoutParams = new LayoutParams(0, 0);
					}
				}
			} else {
				if (moduleContext.isNull("bgImg")) {
					bubbleLayout.setBackgroundResource(UZResourcesIDFinder
							.getResDrawableID("mo_bmap_popupmap"));
				}
				layoutParams = new LayoutParams(-2, -2);
			}
			bubbleLayout.setLayoutParams(layoutParams);
			bubbleLayout.setGravity(Gravity.CENTER);
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
		}else {
			RelativeLayout bubbleLayout = new RelativeLayout(context);
			RelativeLayout.LayoutParams bubbleParams = new RelativeLayout.LayoutParams(UZUtility.dipToPix(wWidth), UZUtility.dipToPix(wHeight));
			WebView webView = new WebView(context);
			RelativeLayout.LayoutParams webParams = new RelativeLayout.LayoutParams(-1, -1);
			if (UZUtility.isHtmlColor(bg)) {
				bubbleLayout.setBackgroundColor(UZUtility.parseCssColor(bg));
				webView.setBackgroundColor(UZUtility.parseCssColor(bg));
			}else {
				Bitmap bitmap = UZUtility.getLocalImage(moduleContext.makeRealPath(bg));
				if (bitmap != null) {
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					float scaleWidth = ((float) UZUtility.dipToPix(wWidth)) / width;
				    float scaleHeight = ((float) UZUtility.dipToPix(wHeight)) / height;
					Matrix matrix = new Matrix();  
				    matrix.postScale(scaleWidth, scaleHeight);
				    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
					bubbleLayout.setBackgroundDrawable(new BitmapDrawable(newBitmap));
					webView.setBackgroundDrawable(new BitmapDrawable(newBitmap));
				}else {
					BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(UZResourcesIDFinder.getResDrawableID("mo_bmap_popupmap"));
					Bitmap newBit = bitmapDescriptor.getBitmap();
					bubbleLayout.setBackgroundDrawable(new BitmapDrawable(newBit));
					webView.setBackgroundDrawable(new BitmapDrawable(newBit));
				}
			}
			//bubbleLayout.setLayoutParams(bubbleParams);
			//webView.setLayoutParams(webParams);
			
			url = moduleContext.makeRealPath(url);
			// 这里需要判断url是否/绝对路径开头，如果是，则加上file://
			if (url != null && url.startsWith("/")) {
				url = "file://" + url;
			}
			if (TextUtils.isEmpty(data)) {// 如果data是空就加载网页
				webView.loadUrl(url);
				webView.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return true;
					}
				});
			} else {// 否则就加载data数据的片段
				webView.getSettings().setJavaScriptEnabled(true);
				webView.getSettings().setDefaultTextEncodingName("utf-8");
				webView.loadDataWithBaseURL(url, data, "text/html", "utf-8", null);// TODO
			}
			webView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (BMapConfig.getInstance().getAddWebBubble() != null) {
							try {
								JSONObject result = new JSONObject();
								result.put("id", id);
								BMapConfig.getInstance().getAddWebBubble().success(result, false);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
						}
						return false;
					}else {
						return false;
					}
					
				}
			});
			bubbleLayout.addView(webView);
			return bubbleLayout;
		}
		
	}

	private LinearLayout titleLayout() {
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mapOverlay.bubbleClickCallBack(getId(), "clickContent");
			}
		});
		LayoutParams layoutParams = new LayoutParams(-2, -2);
		//layoutParams.setMargins(UZCoreUtil.dipToPix(10), 0, UZCoreUtil.dipToPix(10), 0);
		//titleLayout.setGravity(Gravity.CENTER);
		titleLayout.setLayoutParams(layoutParams);
		titleLayout.setOrientation(LinearLayout.VERTICAL);
		titleLayout.addView(title());
		if (!TextUtils.isEmpty(getSubTitle())) {
			titleLayout.addView(subTitle());
		}
		return titleLayout;
	}

	private TextView title() {
		TextView title = new TextView(context);
		title.setSingleLine();
		//title.setEllipsize(TruncateAt.END);
		//title.setMaxWidth(UZCoreUtil.dipToPix(maxWidth) - 2 * iconMarginLeft + iconW);
		title.setText(getTitle());
		title.setTextColor(getTitleColor());
		title.setTextSize(getTitleSize());
		//title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		return title;
	}

	private TextView subTitle() {
		TextView title = new TextView(context);
		title.setSingleLine();
		//title.setEllipsize(TruncateAt.END);
		//title.setMaxWidth(UZCoreUtil.dipToPix(maxWidth) - 2 * iconMarginLeft + iconW);
		title.setText(getSubTitle());
		title.setTextColor(getSubTitleColor());
		title.setTextSize(getSubTitleSize());
		//title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
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
		layoutParams.setMargins(iconMarginLeft, iconMarginTop, iconMarginLeft, iconMarginTop);
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
