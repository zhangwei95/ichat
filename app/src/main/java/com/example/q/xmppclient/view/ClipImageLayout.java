package com.example.q.xmppclient.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.example.q.xmppclient.R;

/**
 * Created by q on 2018/3/3.
 */

public class ClipImageLayout extends RelativeLayout {
    private ZoomImageView mZoomImageView;
    private ClipBorderView mClipImageView;
    /**
     * 这里测试，直接写死了大小，真正使用过程中，可以提取为自定义属性
     */
    private int mHorizontalPadding = 20;
    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mZoomImageView = new ZoomImageView(context);
        mClipImageView = new ClipBorderView(context);
        android.view.ViewGroup.LayoutParams lp = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipImageLayout);
        //a.getIndexCount()获取属性个数
        for (int i = 0; i < a.getIndexCount(); i++) {
            int index = a.getIndex(i);//所有属性对应的id
            switch (index) {
//                case R.styleable.ClipImageLayout_HorizontalPadding://水平边距
//                    mHorizontalPadding = a.getDimension(R.styleable.ClipImageLayout_HorizontalPadding,20f);
//                    setHorizontalPadding(mHorizontalPadding);
//                    break;
                case R.styleable.ClipImageLayout_Drawable://图片id
                    //拿到图片，Drawable类型
                    Drawable drawable = a.getDrawable(i);
//                    //我们需要的bitmap类型的图片
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    setImageDrawable(drawable);
                    break;
                default:
                    break;
            }
        }

        /**
         * 这里测试，直接写死了图片，真正使用过程中，可以提取为自定义属性
         */
//        mZoomImageView.setImageDrawable(getResources().getDrawable(
//                R.drawable.avatar_testthree));

        this.addView(mZoomImageView, lp);
        this.addView(mClipImageView, lp);

        // 计算padding的px
//        mHorizontalPadding = (int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
//                        .getDisplayMetrics());
        mZoomImageView.setHorizontalPadding(mHorizontalPadding);
        mClipImageView.setHorizontalPadding(mHorizontalPadding);
    }
    /**
     * 对外公布设置边距的方法,单位为dp
     *
     * @param mHorizontalPadding
     */
    public void setHorizontalPadding(int mHorizontalPadding)
    {
        this.mHorizontalPadding = mHorizontalPadding;
    }
    /**
     * 对外公布设置图片的方法,单位为dp
     *
     * @param drawable
     */
    public void setImageDrawable(Drawable drawable)
    {
        mZoomImageView.setImageDrawable(drawable);
    }

    /**
     * 裁切图片
     *
     * @return
     */
    public Bitmap clip()
    {
        return mZoomImageView.clip();
    }
}
