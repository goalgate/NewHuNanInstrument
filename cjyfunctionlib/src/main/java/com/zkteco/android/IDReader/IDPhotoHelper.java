package com.zkteco.android.IDReader;

import android.graphics.Bitmap;


/**
 * Created by scarx on 2015/12/3.
 */
public class IDPhotoHelper {
    public static Bitmap Bgr2Bitmap(byte[] bgrbuf)
    {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int row = 0, col = width-1;
        for (int i = bgrbuf.length-1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i-1] << 8) & 0xFF00;
            color += ((bgrbuf[i-2]) << 16) & 0xFF0000;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width-1;
                row++;
            }
        }
        return bmp;
    }

    public static Bitmap Bgr2BitmapARGB8888(byte[] bgrbuf)
    {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int row = 0, col = width-1;
        for (int i = bgrbuf.length-1; i >= 3; i -= 3) {
            //int color = 0;
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i-1] << 8) & 0xFF00;
            color += ((bgrbuf[i-2]) << 16) & 0xFF0000;
            color += 0xFF000000;
           // color = (bgrbuf[i] << 24) | (bgrbuf[i-1] << 16) | bgrbuf[i-2]<<8 | 0x000000FF;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width-1;
                row++;
            }
        }
        return bmp;
    }
	
}
