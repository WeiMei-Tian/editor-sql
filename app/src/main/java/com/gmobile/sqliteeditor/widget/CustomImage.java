package com.gmobile.sqliteeditor.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;


public class CustomImage extends TextView {

	private Paint mPaint;
	private Paint mTextPaint;
	private Context mContext;
	private Rect mTextBound;
	private Bitmap mImage;
	private Rect rect;
	private int mWidth,mHeight;
	private boolean isImage;
	private boolean hasWidth,hasHeight;
	private String mTitle;

	public CustomImage(Context context,boolean isImage,String title) {
		super(context);
		mContext = context;
		this.isImage = isImage;
		this.mTitle = title;
		init();
	}

	private void init() {
		mPaint = new Paint();
		mTextPaint = new Paint();
		mTextPaint.setTextSize(50);
		mPaint.setStrokeWidth(3);
		mPaint.setStyle(Paint.Style.STROKE);
        rect = new Rect();
		mImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_check);

//		mTextPaint.setColor(getResources().getColor(SkinHandler.getResourceId(mContext,R.attr.minorText)));
//		mPaint.setColor(getResources().getColor(SkinHandler.getResourceId(mContext,R.attr.minorText)));
		mTextPaint.setColor(getResources().getColor(R.color.table_text_color));
		mPaint.setColor(getResources().getColor(R.color.table_border));
	}

	@Override
	public void setWidth(int pixels) {
		super.setWidth(pixels);
		hasWidth = true;
		mWidth = pixels;
		invalidate();
	}

	@Override
	public void setHeight(int pixels) {
		super.setHeight(pixels);
		hasHeight = true;
		mHeight = pixels;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		if(hasWidth){

		}else {
			mWidth = getPaddingLeft() + getPaddingRight() + mImage.getWidth();
		}

		if(hasHeight){

		}else {
			mHeight = getPaddingTop() + getPaddingBottom() + mImage.getHeight();
		}


		setMeasuredDimension(mWidth, mHeight);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRect(0, 0, mWidth,mHeight, mPaint);

		mTextBound = new Rect();

		if(isImage){
			rect.left = mWidth / 2 - mImage.getWidth() / 2;
			rect.right = mWidth / 2 + mImage.getWidth() / 2;
			rect.top = mHeight/2 - mImage.getHeight() / 2;
			rect.bottom = mHeight/2  + mImage.getHeight() / 2;
			canvas.drawBitmap(mImage, null, rect, mPaint);
		}else if(!TextUtils.isEmpty(mTitle)){
			if(!TextUtils.isEmpty(mTitle)){
				mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), mTextBound);
				mPaint.setStrokeWidth(5);
				canvas.drawText(mTitle, mWidth / 2 - mTextBound.width() * 1.0f / 2, mHeight / 2, mTextPaint);
			}
		}

	}
}
