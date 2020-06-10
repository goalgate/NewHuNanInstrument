package cn.cbsd.cjyfunctionlib.Tools;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapTools {

    public static Bitmap scaleMatrix(Bitmap bitmap, int width, int height){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleW = width/w;
        float scaleH = height/h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

}
