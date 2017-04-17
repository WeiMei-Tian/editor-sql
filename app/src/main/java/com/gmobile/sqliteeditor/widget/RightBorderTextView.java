package com.gmobile.sqliteeditor.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;

/**
 * Created by admin on 2016/9/20.
 */
public class RightBorderTextView extends TextView {

    /**
     * 四周是否带有边框【true:四周带有边框】【false:四周不带边框】
     */
    private Paint paint;
    private Paint rectPaint;
    public boolean isLastColumn = false;

    public RightBorderTextView(Context context,boolean isLastColumn) {
        this(context, null);
        this.isLastColumn = isLastColumn;
    }

    public RightBorderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public RightBorderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 创建画笔
        paint = new Paint();
        // 获取该画笔颜色

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5);
//        rectPaint.setColor(getResources().getColor(SkinHandler.getResourceId(context, R.attr.minorText)));
        rectPaint.setColor(getResources().getColor(R.color.table_border));

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if(isLastColumn){
        }else {
            canvas.drawLine(getMeasuredWidth(), 0, getMeasuredWidth(), getMeasuredHeight(), rectPaint);
        }
//        canvas.drawLine(0,0,0,getMeasuredHeight(),rectPaint);
    }

}
