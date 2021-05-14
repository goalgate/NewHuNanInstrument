package cn.cbsd.cjyfunctionlib.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;

/**
 * @author Created by WZW on 2021-05-10 14:36.
 * @description
 */
public class YUV2RGBForRK3288 {

    private static YUV2RGBForRK3288 instance;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private YUV2RGBForRK3288() {
    }

    private YUV2RGBForRK3288(Context mContext) {
        rs = RenderScript.create(mContext);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public static YUV2RGBForRK3288 getInstance(Context mContext) {
        if (instance == null) {
            synchronized (YUV2RGBForRK3288.class) {
                if (instance == null) {
                    instance = new YUV2RGBForRK3288(mContext);
                }
            }
        }
        return instance;
    }

    public Bitmap convertYUVtoRGB(byte[] yuvData, int width, int height) {
        long timeStart = System.currentTimeMillis();

        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuvData.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        in.copyFrom(yuvData);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        long timeStop = System.currentTimeMillis();
        Log.d("timePast", (timeStop - timeStart) + " ms");
        return bmpout;
    }

}
