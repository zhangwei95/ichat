package com.example.q.xmppclient.activity;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by q on 2018/1/18.
 */

public class myViewPager extends ViewPager {
    float startY,startX;
    public myViewPager(Context context) {
        super(context);
    }
    public myViewPager(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY=ev.getRawY();
                startX=ev.getRawX();
                break;

            case MotionEvent.ACTION_MOVE:
                float endY = ev.getRawY();
                float endX = ev.getRawX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                if (distanceX < 50&& distanceY < 50) {
                    return false;
                } else {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
