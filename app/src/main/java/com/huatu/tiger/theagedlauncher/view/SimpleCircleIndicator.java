package com.huatu.tiger.theagedlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;

public class SimpleCircleIndicator extends View {
    Paint circlePaint;

    private int pageNum;
    private float scrollPercent = 0f;
    private int currentPosition;
    private int gapSize;

    private float radius;
    private int colorOn;
    private int colorOff;

    public SimpleCircleIndicator(Context context) {
        super(context);
        init();
    }

    public SimpleCircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        radius = DisplayUtil.dp2px(3, getContext());
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorOn = Color.WHITE;
        colorOff = Color.parseColor("#888888");
        gapSize = (int) DisplayUtil.dp2px(10, getContext());
    }

    public void setSelectDotColor(int colorOn) {
        this.colorOn = colorOn;
    }

    public void setUnSelectDotColor(int colorOff) {
        this.colorOff = colorOff;
    }


    public void onPageScrolled(int position, float percent, int pixels) {
        scrollPercent = percent;
        currentPosition = position;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pageNum <= 0) {
            return;
        }
        float left = (getWidth() - (pageNum - 1) * gapSize) * 0.5f;
        float height = getHeight() * 0.5f;
        circlePaint.setColor(colorOff);
        for (int i = 0; i < pageNum; i++) {
            canvas.drawCircle(left + i * gapSize, height, radius, circlePaint);
        }
        circlePaint.setColor(colorOn);
        canvas.drawCircle(left + currentPosition * gapSize + gapSize * scrollPercent, height, radius, circlePaint);
    }

    public void setPageNum(int nums) {
        pageNum = nums;
    }

}
