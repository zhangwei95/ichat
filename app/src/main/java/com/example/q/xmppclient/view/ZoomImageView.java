package com.example.q.xmppclient.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by q on 2018/3/3.
 */

public class ZoomImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = ZoomImageView.class.getSimpleName();

    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float mInitScale;
    /**
     * 图片的最大比例
     */
    private float mMaxScale;
    /**
     * 双击图片放大的比例
     */
    private float mMidScale;

    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    /**
     * 第一次加载图片时调整图片缩放比例，使图片的宽或者高充满屏幕
     */
    private boolean mFirst=true;

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    /**
     * 监听手势
     */
    private GestureDetector mGestureDetector;
    private final Matrix mScaleMatrix = new Matrix();
    /**
     * 上一次触控点的数量
     */
    private int mLastPointerCount;
    /**
     * 是否可以拖动
     */
    private boolean isCanDrag;
    /**
     * 上一次滑动的x和y坐标
     */
    private float mLastX;
    private float mLastY;
    /**
     * 可滑动的临界值
     */
    private int mTouchSlop;
    /**
     * 是否用检查左右边界
     */
    private boolean isCheckLeftAndRight;
    /**
     * 是否用检查上下边界
     */
    private boolean isCheckTopAndBottom;
    /**
     * 是否正在自动放大或者缩小
     */
    private boolean isAutoScale;
    /**
     * 水平方向与View的边距
     */
    private int mHorizontalPadding = 20;
    /**
     * 垂直方向与View的边距
     */
    private int mVerticalPadding;

    public ZoomImageView(Context context)
    {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //一定要将图片的ScaleType设置成Matrix类型的
        super.setScaleType(ScaleType.MATRIX);
        //初始化缩放手势监听器
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setOnTouchListener(this);
        //初始化手势检测器，监听双击事件
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener()
                {
                    @Override
                    public boolean onDoubleTap(MotionEvent e)
                    {
                        //如果是正在自动缩放，则直接返回，不进行处理
                        if (isAutoScale) return true;
                        //得到点击的坐标
                        float x = e.getX();
                        float y = e.getY();
                        //如果当前图片的缩放值小于指定的双击缩放值
                        if (getScale() < mMidScale){
                            //进行自动放大
                            post(new AutoScaleRunnable(mMidScale,x,y));
                        }else{
                            //当前图片的缩放值大于初试缩放值，则自动缩小
                            post(new AutoScaleRunnable(mInitScale,x,y));
                        }
                        return true;

                    }
                });
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //当我们两个手指进行分开操作时，说明我们想要放大，这个scaleFactor是一个稍微大于1的数值
        //当我们两个手指进行闭合操作时，说明我们想要缩小，这个scaleFactor是一个稍微小于1的数值
        float scaleFactor = detector.getScaleFactor();
        //获得我们图片当前的缩放值
        float scale = getScale();
        //如果当前没有图片，则直接返回
        if (getDrawable() == null){
            return true;
        }

        //如果scaleFactor大于1，说明想放大，当前的缩放比例乘以scaleFactor之后小于
        //最大的缩放比例时，允许放大
        //如果scaleFactor小于1，说明想缩小，当前的缩放比例乘以scaleFactor之后大于
        //最小的缩放比例时，允许缩小
        if ((scaleFactor > 1.0f && scale * scaleFactor < mMaxScale)
                || scaleFactor < 1.0f && scale * scaleFactor > mInitScale){
            //边界控制，如果当前缩放比例乘以scaleFactor之后大于了最大的缩放比例
            if (scale * scaleFactor > mMaxScale + 0.01f){
                //则将scaleFactor设置成mMaxScale/scale
                //当再进行matrix.postScale时
                //scale*scaleFactor=scale*(mMaxScale/scale)=mMaxScale
                //最后图片就会放大至mMaxScale缩放比例的大小
                scaleFactor = mMaxScale / scale;
            }
            //边界控制，如果当前缩放比例乘以scaleFactor之后小于了最小的缩放比例
            //我们不允许再缩小
            if (scale * scaleFactor < mInitScale + 0.01f){
                //计算方法同上
                scaleFactor = mInitScale / scale;

            }
            //前两个参数是缩放的比例，是一个稍微大于1或者稍微小于1的数，形成一个随着手指放大
            //或者缩小的效果
            //detector.getFocusX()和detector.getFocusY()得到的是多点触控的中点
            //这样就能实现我们在图片的某一处局部放大的效果
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            //因为图片的缩放点不是图片的中心点了，所以图片会出现偏移的现象，所以进行一次边界的检查和居中操作
            checkBorderAndCenterWhenScale();
            //将矩阵作用到图片上
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }
    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale()
    {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //当双击操作时，不允许移动图片，直接返回true
        if (mGestureDetector.onTouchEvent(event)){
            return true;
        }
        //将事件传递给ScaleGestureDetector
        mScaleGestureDetector.onTouchEvent(event);
        //用于存储多点触控产生的坐标
        float x = 0.0f;
        float y = 0.0f;
        //得到多点触控的个数
        int pointerCount = event.getPointerCount();
        //将所有触控点的坐标累加起来
        for(int i=0 ; i<pointerCount ; i++){
            x += event.getX(i);
            y += event.getY(i);
        }
        //取平均值，得到的就是多点触控后产生的那个点的坐标
        x /= pointerCount;
        y /= pointerCount;
        //如果触控点的数量变了，则置为不可滑动
        if (mLastPointerCount != pointerCount){
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                isCanDrag = false;
                //当图片处于放大状态时，禁止ViewPager拦截事件，将事件传递给图片，进行拖动
                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f){
                    if (getParent() instanceof ViewPager){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //当图片处于放大状态时，禁止ViewPager拦截事件，将事件传递给图片，进行拖动
                if (rectF.width() > getWidth() + 0.01f || rectF.height() > getHeight() + 0.01f){
                    if (getParent() instanceof ViewPager){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                //得到水平和竖直方向的偏移量
                float dx = x - mLastX;
                float dy = y - mLastY;
                //如果当前是不可滑动的状态，判断一下是否是滑动的操作
                if (!isCanDrag){
                    isCanDrag = isMoveAction(dx,dy);
                }
                //如果可滑动
                if (isCanDrag){
                    if (getDrawable() != null){
                        isCheckLeftAndRight = true;
                        isCheckTopAndBottom = true;
                        //如果图片宽度小于控件宽度
                        if (rectF.width() < getWidth()){
                            //左右不可滑动
                            dx = 0;
                            //左右不可滑动，也就不用检查左右的边界了
                            isCheckLeftAndRight = false;
                        }
                        //如果图片的高度小于控件的高度
                        if (rectF.height()+mVerticalPadding < getHeight()){
                            //上下不可滑动
                            dy = 0;
                            //上下不可滑动，也就不用检查上下边界了
                            isCheckTopAndBottom = false;
                        }
                    }
                    mScaleMatrix.postTranslate(dx,dy);
                    //当平移时，检查上下左右边界
                    checkBorder();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                //当手指抬起时，将mLastPointerCount置0，停止滑动
                mLastPointerCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;

    }
    /**
     * 判断是否是移动的操作
     */
    private boolean isMoveAction(float dx , float dy){
        //勾股定理，判断斜边是否大于可滑动的一个临界值
        return Math.sqrt(dx*dx + dy*dy) > mTouchSlop;
    }
    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkBorderWhenTranslate()
    {
//        RectF rect = getMatrixRectF();
//
//        float deltaX = 0, deltaY = 0;
//        final float viewWidth = getWidth();
//        final float viewHeight = getHeight();
//        // 判断移动或缩放后，图片显示是否超出屏幕边界
//        if (rect.top > 0 && isCheckTopAndBottom)
//        {
//            deltaY = -rect.top;
//        }
//        if (rect.bottom < viewHeight && isCheckTopAndBottom)
//        {
//            deltaY = viewHeight - rect.bottom;
//        }
//        if (rect.left > 0 && isCheckLeftAndRight)
//        {
//            deltaX = -rect.left;
//        }
//        if (rect.right < viewWidth && isCheckLeftAndRight)
//        {
//            deltaX = viewWidth - rect.right;
//        }
//        mScaleMatrix.postTranslate(deltaX, deltaY);
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width)
        {
            if (rect.left > 0)
            {
                deltaX = -rect.left;
            }
            if (rect.right < width)
            {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height)
        {
            if (rect.top > 0)
            {
                deltaY = -rect.top;
            }
            if (rect.bottom < height)
            {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width)
        {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height)
        {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
        Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }
    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
//
//    private boolean isCanDrag(float dx, float dy)
//    {
//        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
//    }


    /**
     * 当view添加到window时调用，早于onGlobalLayout，因此可以在这里注册监听器
     */
    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }
    /**
     * 当view从window上移除时调用，因此可以在这里移除监听器
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }
    /**
     * 当布局树发生变化时会调用此方法，我们可以在此方法中获得控件的宽和高
     */
    @Override
    public void onGlobalLayout()
    {
        if (mFirst)
        {
            Drawable d = getDrawable();
            if (d == null)
                return;
            Log.e(TAG, d.getIntrinsicWidth() + " , " + d.getIntrinsicHeight());
            // 计算padding的px
            mHorizontalPadding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding,
                    getResources().getDisplayMetrics());
            // 垂直方向的边距
            mVerticalPadding = (getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;

            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = 1.0f;
            if (dw < getWidth() - mHorizontalPadding * 2
                    && dh > getHeight() - mVerticalPadding * 2)
            {
                scale = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw;
            }

            if (dh < getHeight() - mVerticalPadding * 2
                    && dw > getWidth() - mHorizontalPadding * 2)
            {
                scale = (getHeight() * 1.0f - mVerticalPadding * 2) / dh;
            }

            if (dw < getWidth() - mHorizontalPadding * 2
                    && dh < getHeight() - mVerticalPadding * 2)
            {
                float scaleW = (getWidth() * 1.0f - mHorizontalPadding * 2)
                        / dw;
                float scaleH = (getHeight() * 1.0f - mVerticalPadding * 2) / dh;
                scale = Math.max(scaleW, scaleH);
            }
            if (dw > getWidth() - mHorizontalPadding * 2
                    && dh > getHeight() - mVerticalPadding * 2)
            {
                float scaleW = (getWidth() * 1.0f - mHorizontalPadding * 2)
                        / dw;
                float scaleH = (getHeight() * 1.0f - mVerticalPadding * 2) / dh;
                scale = Math.max(scaleW, scaleH);
            }

            mInitScale = scale;
            mMidScale = mInitScale * 2;
            mMaxScale = mInitScale * 16;
            Log.e(TAG, "initScale = " + mInitScale);

            mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2,
                    getHeight() / 2);
            // 图片移动至屏幕中心
            checkBorder();
            setImageMatrix(mScaleMatrix);
            mFirst = false;
        }

    }
    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF()
    {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d)
        {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }
    /**
     * 边界检测
     */
    private void checkBorder()
    {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width - 2 * mHorizontalPadding)
        {
            if (rect.left > mHorizontalPadding)
            {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding)
            {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }
        if (rect.height() >= height - 2 * mVerticalPadding)
        {
            if (rect.top > mVerticalPadding)
            {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding)
            {
                deltaY = height - mVerticalPadding - rect.bottom;
            }
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);

    }
    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale()
    {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;
        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width)
        {
            if (rect.left > 0)
            {
                deltaX = -rect.left;
            }
            if (rect.right < width)
            {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height)
        {
            if (rect.top > 0)
            {
                deltaY = -rect.top;
            }
            if (rect.bottom < height)
            {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width)
        {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height)
        {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }
        Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);

        mScaleMatrix.postTranslate(deltaX, deltaY);

    }
    /**
     * 自动放大缩小，自动缩放的原理是使用View.postDelay()方法，每隔16ms调用一次
     * run方法，给人视觉上形成一种动画的效果
     */
    private class AutoScaleRunnable implements Runnable{
        //放大或者缩小的目标比例
        private float mTargetScale;
        //可能是BIGGER,也可能是SMALLER
        private float tempScale;
        //放大缩小的中心点
        private float x;
        private float y;
        //比1稍微大一点，用于放大
        private final float BIGGER = 1.07f;
        //比1稍微小一点，用于缩小
        private final float SMALLER = 0.93f;
        //构造方法，将目标比例，缩放中心点传入，并且判断是要放大还是缩小
        public AutoScaleRunnable(float targetScale , float x , float y){
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            //如果当前缩放比例小于目标比例，说明要自动放大
            if (getScale() < mTargetScale){
                //设置为Bigger
                tempScale = BIGGER;
            }
            //如果当前缩放比例大于目标比例，说明要自动缩小
            if (getScale() > mTargetScale){
                //设置为Smaller
                tempScale = SMALLER;
            }
        }
        @Override
        public void run() {
            //这里缩放的比例非常小，只是稍微比1大一点或者比1小一点的倍数
            //但是当每16ms都放大或者缩小一点点的时候，动画效果就出来了
            mScaleMatrix.postScale(tempScale, tempScale, x, y);
            //每次将矩阵作用到图片之前，都检查一下边界
            checkBorderAndCenterWhenScale();
            //将矩阵作用到图片上
            setImageMatrix(mScaleMatrix);
            //得到当前图片的缩放值
            float currentScale = getScale();
            //如果当前想要放大，并且当前缩放值小于目标缩放值
            //或者 当前想要缩小，并且当前缩放值大于目标缩放值
            if ((tempScale > 1.0f) && currentScale < mTargetScale
                    ||(tempScale < 1.0f) && currentScale > mTargetScale){
                //每隔16ms就调用一次run方法
                postDelayed(this,16);
            }else {
                //current*scale=current*(mTargetScale/currentScale)=mTargetScale
                //保证图片最终的缩放值和目标缩放值一致
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                //自动缩放结束，置为false
                isAutoScale = false;
            }
        }
    }
    /**
     * 剪切图片，返回剪切后的bitmap对象
     *
     * @return
     */
    public Bitmap clip()
    {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, mHorizontalPadding,
                mVerticalPadding, getWidth() - 2*mHorizontalPadding,
                getWidth() - 2*mHorizontalPadding);
    }
    public void setHorizontalPadding(int mHorizontalPadding)
    {
        this.mHorizontalPadding = mHorizontalPadding;
    }
    public void setmFirst(boolean mFirst)
    {
        this.mFirst=mFirst;
    }
}
