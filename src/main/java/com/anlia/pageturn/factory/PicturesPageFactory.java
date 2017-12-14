package com.anlia.pageturn.factory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.anlia.pageturn.utils.BitmapUtils;
import com.anlia.pageturn.utils.ScreenUtils;

/**
 * 页面内容工厂类：制作图像集合型内容
 */
public class PicturesPageFactory extends PageFactory {
    private Context context;

    public int style;//集合类型
    public final static int STYLE_IDS = 1;//drawable目录图片集合类型
    public final static int STYLE_URIS = 2;//手机本地目录图片集合类型

    private int[] picturesIds;
    /**
     * 初始化drawable目录下的图片id集合
     * @param context
     * @param pictureIds
     */
    public PicturesPageFactory(Context context, int[] pictureIds){
        this.context = context;
        this.picturesIds = pictureIds;
        this.style = STYLE_IDS;
        if (pictureIds.length > 0){
            hasData = true;
            pageTotal = pictureIds.length;
        }
    }

    private String[] picturesUris;
    /**
     * 初始化本地目录下的图片uri集合
     * @param context
     * @param picturesUris
     */
    public PicturesPageFactory(Context context, String[] picturesUris){
        this.context = context;
        this.picturesUris = picturesUris;
        this.style = STYLE_URIS;
        if (picturesUris.length > 0){
            hasData = true;
            pageTotal = picturesUris.length;
        }
    }

    @Override
    public void drawPreviousBitmap(Bitmap bitmap, int pageNum) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(getBitmapByIndex(pageNum-2),0,0,null);
    }

    @Override
    public void drawCurrentBitmap(Bitmap bitmap, int pageNum) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(getBitmapByIndex(pageNum-1),0,0,null);
    }

    @Override
    public void drawNextBitmap(Bitmap bitmap, int pageNum) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(getBitmapByIndex(pageNum),0,0,null);
    }

    @Override
    public Bitmap getBitmapByIndex(int index) {
        if(hasData){
            switch (style){
                case STYLE_IDS:
                    return getBitmapFromIds(index);
                case STYLE_URIS:
                    return getBitmapFromUris(index);
                default:
                    return null;
            }
        }else {
            return null;
        }
    }

    /**
     * 从id集合获取bitmap
     * @param index
     * @return
     */
    private Bitmap getBitmapFromIds(int index){
        return BitmapUtils.drawableToBitmap(
                context.getResources().getDrawable(picturesIds[index]),
                ScreenUtils.getScreenWidth(context),
                ScreenUtils.getScreenHeight(context)
        );
    }

    /**
     * 从uri集合获取bitmap
     * @param index
     * @return
     */
    private Bitmap getBitmapFromUris(int index){
        return null;//这个有空再写啦，大家可自行补充完整
    }
}
