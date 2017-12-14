package com.anlia.pageturn.factory;

import android.graphics.Bitmap;

/**
 * 页面内容工厂类
 */
public abstract class PageFactory {
    public boolean hasData = false;//是否含有数据
    public int pageTotal = 0;//页面总数

    public PageFactory(){}

    /**
     * 绘制上一页bitmap
     * @param bitmap
     * @param pageNum
     */
    public abstract void drawPreviousBitmap(Bitmap bitmap, int pageNum);

    /**
     * 绘制当前页bitmap
     * @param bitmap
     * @param pageNum
     */
    public abstract void drawCurrentBitmap(Bitmap bitmap, int pageNum);

    /**
     * 绘制下一页bitmap
     * @param bitmap
     * @param pageNum
     */
    public abstract void drawNextBitmap(Bitmap bitmap, int pageNum);

    /**
     * 通过索引在集合中获取相应内容
     * @param index
     * @return
     */
    public abstract Bitmap getBitmapByIndex(int index);
}
