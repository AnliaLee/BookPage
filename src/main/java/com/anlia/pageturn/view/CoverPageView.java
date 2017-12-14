package com.anlia.pageturn.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.anlia.pageturn.bean.MyPoint;
import com.anlia.pageturn.factory.PageFactory;
import com.anlia.pageturn.utils.ViewUtils;

/**
 * Created by anlia on 2017/12/11.
 */

public class CoverPageView extends View {
    private int defaultWidth;//默认宽度
    private int defaultHeight;//默认高度
    private int viewWidth;
    private int viewHeight;
    private int pageNum;//当前页数
    private float xDown;//记录初始触摸的x坐标
    private float scrollPageLeft;//滑动页左边界
    private int scrollTime;//滑动动画时间

    private PageFactory pageFactory;
    private MyPoint touchPoint;//触摸点
    private Scroller mScroller;
    private GradientDrawable shadowDrawable;

    private Bitmap previousPage;//上一页bitmap
    private Bitmap currentPage;//当前页bitmap
    private Bitmap nextPage;//下一页bitmap

    private int touchStyle;//触摸类型
    public static final int TOUCH_MIDDLE = 0;//点击中间区域
    public static final int TOUCH_LEFT = 1;//点击左边区域
    public static final int TOUCH_RIGHT = 2;//点击右边区域

    private int pageState;//翻页状态，用于限制翻页动画结束前的触摸操作
    public static final int PAGE_STAY = 0;//处于静止状态
    public static final int PAGE_NEXT = 1;//翻至下一页
    public static final int PAGE_PREVIOUS = 2;//翻至上一页

    public CoverPageView(Context context) {
        super(context);
        init(context);
    }

    public CoverPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        defaultWidth = 600;
        defaultHeight = 1000;
        pageNum = 1;
        scrollPageLeft = 0;
        scrollTime = 300;
        touchStyle = TOUCH_RIGHT;
        pageState = PAGE_STAY;

        touchPoint = new MyPoint(-1,-1);
        mScroller = new Scroller(context,new LinearInterpolator());

        int[] mBackShadowColors = new int[] { 0x66000000,0x00000000};
        shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        shadowDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    /**
     * 设置工厂类
     * @param factory
     */
    public void setPageFactory(final PageFactory factory){
        //保证View已经完成了测量工作，各页bitmap已初始化
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if(factory.hasData){
                    pageFactory = factory;
                    pageFactory.drawCurrentBitmap(currentPage,pageNum);
                    postInvalidate();
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = ViewUtils.measureSize(defaultHeight, heightMeasureSpec);
        int width = ViewUtils.measureSize(defaultWidth, widthMeasureSpec);
        setMeasuredDimension(width, height);

        viewWidth = width;
        viewHeight = height;

        previousPage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565);
        currentPage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565);
        nextPage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.RGB_565);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(pageFactory !=null){
            if(touchPoint.x ==-1 && touchPoint.y ==-1){
                drawCurrentPage(canvas);
                pageState = PAGE_STAY;
            }else{
                if(touchStyle == TOUCH_RIGHT){
                    drawCurrentPage(canvas);
                    drawPreviousPage(canvas);
                    drawShadow(canvas);
                }else {
                    drawNextPage(canvas);
                    drawCurrentPage(canvas);
                    drawShadow(canvas);
                }
            }
        }
    }

    /**
     * 绘制上一页
     * @param canvas
     */
    private void drawPreviousPage(Canvas canvas){
        canvas.drawBitmap(previousPage, scrollPageLeft, 0,null);
    }

    /**
     * 绘制当前页
     * @param canvas
     */
    private void drawCurrentPage(Canvas canvas){
        if(touchStyle == TOUCH_RIGHT){
            canvas.drawBitmap(currentPage, 0, 0,null);
        }else if(touchStyle == TOUCH_LEFT){
            canvas.drawBitmap(currentPage, scrollPageLeft, 0,null);
        }
    }

    /**
     * 绘制下一页
     * @param canvas
     */
    private void drawNextPage(Canvas canvas){
        canvas.drawBitmap(nextPage, 0, 0, null);
    }

    /**
     * 绘制阴影
     * @param canvas
     */
    private void drawShadow(Canvas canvas){
        int left = (int)(viewWidth + scrollPageLeft);
        shadowDrawable.setBounds(left, 0, left + 30 , viewHeight);
        shadowDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        if(pageState == PAGE_STAY){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    xDown = x;
                    if(x<=viewWidth/3){//左
                        touchStyle = TOUCH_LEFT;
                        if(pageNum>1){
                            pageNum--;
                            pageFactory.drawCurrentBitmap(currentPage,pageNum);
                            pageFactory.drawNextBitmap(nextPage,pageNum);
                            pageNum++;
                        }
                    }else if(x>viewWidth*2/3){//右
                        touchStyle = TOUCH_RIGHT;
                        if(pageNum<pageFactory.pageTotal){
                            pageNum++;
                            pageFactory.drawPreviousBitmap(previousPage,pageNum);
                            pageFactory.drawCurrentBitmap(currentPage,pageNum);
                            pageNum--;
                        }

                    }else if(x>viewWidth/3 && x<viewWidth*2/3){//中
                        touchStyle = TOUCH_MIDDLE;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(touchStyle == TOUCH_LEFT){
                        if(pageNum>1){
                            scrollPage(x,y);
                        }
                    }else if(touchStyle == TOUCH_RIGHT){
                        if(pageNum<pageFactory.pageTotal){
                            scrollPage(x,y);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    autoScroll();
                    break;
            }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            scrollPageLeft = 0 - (viewWidth - x);

            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y){
                if(touchStyle == TOUCH_RIGHT){
                    pageNum++;
                }else if(touchStyle == TOUCH_LEFT){
                    pageNum--;
                }
                resetView();
            }
            postInvalidate();
        }
    }

    /**
     * 计算滑动页面左边界位置，实现滑动当前页效果
     * @param x
     * @param y
     */
    private void scrollPage(float x, float y){
        touchPoint.x = x;
        touchPoint.y = y;

        if(touchStyle == TOUCH_RIGHT){
            scrollPageLeft = touchPoint.x - xDown;
        }else if(touchStyle == TOUCH_LEFT){
            scrollPageLeft =touchPoint.x - xDown - viewWidth;
        }

        if(scrollPageLeft > 0){
            scrollPageLeft = 0;
        }
        postInvalidate();
    }

    /**
     * 自动完成滑动操作
     */
    private void autoScroll(){
        switch (touchStyle){
            case TOUCH_LEFT:
                if(pageNum>1){
                    autoScrollToPreviousPage();
                }
                break;
            case TOUCH_RIGHT:
                if(pageNum<pageFactory.pageTotal){
                    autoScrollToNextPage();
                }
                break;
        }
    }

    /**
     * 自动完成翻到下一页操作
     */
    private void autoScrollToNextPage(){
        pageState = PAGE_NEXT;

        int dx,dy;
        dx = (int) -(viewWidth+scrollPageLeft);
        dy = (int) (touchPoint.y);

        int time =(int) ((1+scrollPageLeft/viewWidth) * scrollTime);
        mScroller.startScroll((int) (viewWidth+scrollPageLeft), (int) touchPoint.y, dx, dy, time);
    }

    /**
     * 自动完成返回上一页操作
     */
    private void autoScrollToPreviousPage(){
        pageState = PAGE_PREVIOUS;

        int dx,dy;
        dx = (int) -scrollPageLeft;
        dy = (int) (touchPoint.y);

        int time =(int) (-scrollPageLeft/viewWidth * scrollTime);
        mScroller.startScroll((int) (viewWidth+scrollPageLeft), (int) touchPoint.y, dx, dy, time);
    }

    /**
     * 取消翻页动画,计算滑动位置与时间
     */
    private void startCancelAnim(){
        int dx,dy;
        dx = (int) (viewWidth-1-touchPoint.x);
        dy = (int) (touchPoint.y);
        mScroller.startScroll((int) touchPoint.x, (int) touchPoint.y, dx, dy, scrollTime);
    }

    /**
     * 重置操作
     */
    private void resetView(){
        scrollPageLeft = 0;
        touchPoint.x = -1;
        touchPoint.y = -1;
    }
}
