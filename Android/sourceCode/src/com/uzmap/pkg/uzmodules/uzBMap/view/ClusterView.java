package com.uzmap.pkg.uzmodules.uzBMap.view;

import com.uzmap.pkg.uzkit.UZUtility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ClusterView extends View{

	private String mColor = "#000000";
	private int mWidth = 22;
	private int mHeight = 22;
	private Paint mPaint;
	
	public ClusterView(Context context, String color, int width, int height) {
		super(context);
		
		this.mColor = color;
		this.mWidth = width;
		this.mHeight = height;
		
		initPaint();
	}
	
	public ClusterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}
	
	public void setStyle(String color, int width, int height) {
		mPaint.setColor(UZUtility.parseCssColor(color));
		this.mWidth = width;
		this.mHeight = height;
		requestLayout();
		invalidate();
	}
	
	
	private void initPaint() {
		mPaint = new Paint();
		mPaint.setColor(UZUtility.parseCssColor(mColor));
		mPaint.setStyle(Paint.Style.FILL);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(UZUtility.dipToPix(mWidth), UZUtility.dipToPix(mHeight));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Rect rect = new Rect(0,  0, UZUtility.dipToPix(mWidth), UZUtility.dipToPix(mHeight));
		canvas.drawRect(rect, mPaint);
	}

}
