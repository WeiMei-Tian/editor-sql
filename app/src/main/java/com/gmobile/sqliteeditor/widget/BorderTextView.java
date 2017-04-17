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
public class BorderTextView extends TextView {

    /**
     * 四周是否带有边框【true:四周带有边框】【false:四周不带边框】
     */
    private Paint paint;
    private Paint rectPaint;

    public BorderTextView(Context context) {
        this(context, null);
    }

    public BorderTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public BorderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // 创建画笔
        paint = new Paint();
        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3);
//        rectPaint.setColor(getResources().getColor(SkinHandler.getResourceId(context,R.attr.minorText)));
        rectPaint.setColor(getResources().getColor(R.color.table_border));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), rectPaint);

    }

}
