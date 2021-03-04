package cn.cbsd.cjyfunctionlib.Tools;





import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Created by WZW on 2021-02-07 10:23.
 * @description Bitmap操作
 */

public class BitmapTools {

    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 根据传入的宽度根据bitmap的宽高进行适配
     *
     * @param bitmap
     * @param width
     * @return
     */
    public static Bitmap scaleMatrix(Bitmap bitmap, int width) {
        int before_width = bitmap.getWidth();
        int before_height = bitmap.getHeight();
        int after_width = width;
        int after_height = (before_height * after_width) / before_width;
        float scaleW = (float) after_width / before_width;
        float scaleH = (float) after_height / before_height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, before_width, before_height, matrix, true);
        if (bitmap != null & !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return newbm;
    }

    /**
     * 将bitmap按限制宽高缩放
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap scaleMatrix(Bitmap bitmap, int width,int height) {
        int before_width = bitmap.getWidth();
        int before_height = bitmap.getHeight();
        float scaleW = (float) width / before_width;
        float scaleH = (float) height / before_height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, before_width, before_height, matrix, true);
        if (bitmap != null & !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return newbm;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {

                int ix = bitmap.getWidth();
                if (ix > 1000) {
                    ix = (bitmap.getHeight() * 1000) / bitmap.getWidth();
                    Bitmap bmp = ThumbnailUtils.extractThumbnail(bitmap, 1000, ix);
                    baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    baos.flush();

                } else {
                    baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);

                    baos.flush();
                    //baos.close();
                }

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
