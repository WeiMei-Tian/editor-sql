package com.gmobile.sqliteeditor.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.gmobile.sqliteeditor.R;


public class CircleLayout extends View {

    /**
     * 画笔对象的引用
     */
    private Paint paint;

    /**
     * 圆环的颜色
     */
    private int roundColor;

    public CircleLayout(Context context) {
        this(context, null);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout);

        // 获取自定义属性和默认值
        roundColor = typedArray.getColor(R.styleable.CircleLayout_bgColor,
                context.getResources().getColor(R.color.thumb_cir_bg));

        typedArray.recycle();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centre = getWidth() / 2; // 获取圆心的x坐标
        paint.setColor(roundColor); // 设置圆环的颜色
        paint.setStyle(Paint.Style.FILL_AND_STROKE); // 设置空心
        paint.setAntiAlias(true); // 消除锯齿
        canvas.drawCircle(centre, centre, centre, paint); // 画出圆环
    }

    public void setColor(int resId) {
        this.roundColor = resId;
        postInvalidate();
    }
}
