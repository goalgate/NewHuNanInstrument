package cn.cbsd.cjyfunctionlib.Tools;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;

import com.cbsd.YuvUtils;


/**
 * @author Created by WZW on 2021-02-07 9:38.
 * @description yuv操作
 */
public class YuvTools {

    /**
     * NV21属于YUV420SP模式
     */

    /**
     * yuv数据转为Bitmap
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static Bitmap yuv2Bitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static Bitmap BGR2Bitmap(byte[] bytes, int width, int height) {
        // use Bitmap.Config.ARGB_8888 instead of type is OK
        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {
            byte b1 = bytes[i * 3 + 0];
            byte b2 = bytes[i * 3 + 1];
            byte b3 = bytes[i * 3 + 2];
            // set value
            rgba[i * 4 + 0] = b3;
            rgba[i * 4 + 1] = b2;
            rgba[i * 4 + 2] = b1;
            rgba[i * 4 + 3] = (byte) 255;
        }
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return stitchBmp;
    }

    public static Bitmap RGB2Bitmap(byte[] bytes, int width, int height) {
        // use Bitmap.Config.ARGB_8888 instead of type is OK
        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {
            byte b1 = bytes[i * 3 + 0];
            byte b2 = bytes[i * 3 + 1];
            byte b3 = bytes[i * 3 + 2];
            // set value
            rgba[i * 4 + 0] = b1;
            rgba[i * 4 + 1] = b2;
            rgba[i * 4 + 2] = b3;
            rgba[i * 4 + 3] = (byte) 255;
        }
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return stitchBmp;
    }

    /**
     * yuv旋转90度
     *
     * @param src
     * @param des
     * @param width
     * @param height
     */
    public static void rotateYuv90(byte[] src, byte[] des, int width, int height) {

        int wh = width * height;
        //旋转Y
        int k = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                des[k] = src[width * j + i];
                k++;
            }
        }

        for (int i = 0; i < width; i += 2) {
            for (int j = 0; j < height / 2; j++) {
                des[k] = src[wh + width * j + i];
                des[k + 1] = src[wh + width * j + i + 1];
                k += 2;
            }
        }
    }

    /**
     * bitmap转为yuv数据
     * rk系列不可用
     *
     * @param inputWidth
     * @param inputHeight
     * @param srcBitmap
     * @return
     */
    public static byte[] getNV21(int inputWidth, int inputHeight, Bitmap srcBitmap) {
        int[] argb = new int[inputWidth * inputHeight];
        if (null != srcBitmap) {
            try {
                srcBitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            // byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
            // encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
            if (null != srcBitmap && !srcBitmap.isRecycled()) {
                srcBitmap.recycle();
                srcBitmap = null;
            }
            return encodeYUV420SP(argb, inputWidth, inputHeight);
        } else return null;
    }

    /**
     * int[]转为yuv字节数组
     *
     * @param argb
     * @param width
     * @param height
     */
    public static byte[] encodeYUV420SP(int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        byte[] yuv = new byte[width * height * 3 / 2];

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                /* NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2 				meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is 					every otherpixel AND every other scanline.*/
                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }
                index++;
            }
        }
        return yuv;
    }

    /**
     * yuv数据转为rgb数据
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public static byte[] NV21toRGBA(byte[] data, int width, int height) {
        int size = width * height;
        byte[] bytes = new byte[size * 4];
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
        return bytes;
    }

    /**
     * yuv裁剪
     *
     * @param src
     * @param width
     * @param height
     * @param left
     * @param top
     * @param clip_w
     * @param clip_h
     * @return
     */
    public static byte[] clipNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height || left + clip_w > width || top + clip_h > height) {
            return null;
        }
        //取偶
        int x = left / 4 * 4, y = top / 4 * 4;
        int w = clip_w / 4 * 4, h = clip_h / 4 * 4;
        int y_unit = w * h;
        int uv = y_unit / 2;
        byte[] nData = new byte[y_unit + uv];
        int uv_index_dst = w * h - y / 2 * w;
        int uv_index_src = width * height + x;
        for (int i = y; i < y + h; i++) {
            System.arraycopy(src, i * width + x, nData, (i - y) * w, w);//y内存块复制
            if (i % 2 == 0) {
                System.arraycopy(src, uv_index_src + (i >> 1) * width, nData, uv_index_dst + (i >> 1) * w, w);//uv内存块复制
            }
        }
        return nData;
    }

    static int yStart = 0;

    static int yStop = 0;

    /**
     * rk3399竖屏摄像头图像适配成横屏，耗时较多，高温下卡顿严重
     *
     * @param src
     * @param view
     * @param desWidth
     * @param desHeight
     * @return
     */

    @Deprecated
    public static byte[] yuv_revert90(byte[] src, View view, int desWidth, int desHeight) {
        long timeStart = System.currentTimeMillis();
        byte[] src_rotate90 = new byte[src.length];
        rotateYuv90(src, src_rotate90, desWidth, desHeight);
        if (yStart == 0 && yStop == 0) {
            int scale = 2;
            int previewWidth = view.getWidth();
            int previewHeight = view.getHeight();
            if (previewWidth == 0 || previewHeight == 0) {
                return src_rotate90;
            }
            int scaledChildHeight = desWidth * previewWidth / desHeight;
            int act_left = 0;
            int act_top = (previewHeight - scaledChildHeight) / scale;
            int act_right = previewWidth;
            int act_bottom = (previewHeight + scaledChildHeight) / scale;
            int act_width = act_right - act_left;
            int act_height = act_bottom - act_top;
            yStart = Math.round(Math.abs(act_top / ((float) act_height / desWidth)));
            yStop = Math.round(Math.abs(act_bottom / ((float) act_height / desWidth)));
        }
        byte[] result1 = clipNV21(src_rotate90, desHeight, desWidth, 0, yStart, desHeight, yStop - yStart);
        Bitmap bitmap = yuv2Bitmap(result1, desHeight, yStop - yStart);
        Bitmap bitmap2 = BitmapTools.scaleMatrix(bitmap, desWidth, desHeight);
        byte[] result = getNV21(desWidth, desHeight, bitmap2);
        long timeStop = System.currentTimeMillis();
        Log.d("timePast", (timeStop - timeStart) + " ms");
        return result;
    }


    /**
     * rk3399竖屏摄像头图像适配成横屏，耗时较少
     *
     * @param src
     * @param view
     * @param desWidth
     * @param desHeight
     * @param mirror
     * @return
     */
    public static byte[] yuv_revert(byte[] src, View view, int desWidth, int desHeight, boolean mirror) {
        long timeStart = System.currentTimeMillis();
        //计算裁剪开始及结束的距离
        if (yStart == 0 && yStop == 0) {
            int scale = 2;
            int previewWidth = view.getWidth();
            int previewHeight = view.getHeight();
            if (previewWidth == 0 || previewHeight == 0) {
                return src;
            }
            int scaledChildHeight = desWidth * previewWidth / desHeight;
            int act_left = 0;
            int act_top = (previewHeight - scaledChildHeight) / scale;
            int act_right = previewWidth;
            int act_bottom = (previewHeight + scaledChildHeight) / scale;
            int act_width = act_right - act_left;
            int act_height = act_bottom - act_top;
            yStart = Math.round(Math.abs(act_top / ((float) act_height / desWidth)));
            yStop = Math.round(Math.abs(act_bottom / ((float) act_height / desWidth)));

        }

//        //原图像nv21转i420
//        byte[] src_i420 = new byte[desWidth * desHeight * 3 / 2];
//        YuvUtils.yuvNV21ToI420(src, desWidth, desHeight, src_i420);
//        //原图像i420旋转90度
//        byte[] rotate90_i420 = new byte[desWidth * desHeight * 3 / 2];
//        YuvUtils.yuvRotateI420(src_i420, desWidth, desHeight, rotate90_i420, 90);
//        //裁剪图像
//        byte[] clip_i420 = new byte[desHeight * (yStop - yStart) * 3 / 2];
//        YuvUtils.yuvCropI420(rotate90_i420, desHeight, desWidth, clip_i420, desHeight, yStop - yStart, 0, yStart);
//        //图像缩放到固定尺寸
//        byte[] scale_i420 = new byte[desWidth * desHeight * 3 / 2];
//        YuvUtils.yuvScaleI420(clip_i420, desHeight, yStop - yStart, scale_i420, desWidth, desHeight, 0);
//
//        byte[] result = new byte[desWidth * desHeight * 3 / 2];
//        //是否需要镜像转换
//        if (mirror) {
//            byte[] mirror_i420 = new byte[desWidth * desHeight * 3 / 2];
//            YuvUtils.yuvMirrorI420LeftRight(scale_i420, desWidth, desHeight, mirror_i420);
//            YuvUtils.yuvI420ToNV21(mirror_i420, desWidth, desHeight, result);
//            mirror_i420 = null;
//        } else {
//            YuvUtils.yuvI420ToNV21(scale_i420, desWidth, desHeight, result);
//        }
//        src_i420 = null;
//        rotate90_i420 = null;
//        clip_i420 = null;
//        scale_i420 = null;

        byte[] result = new byte[desWidth * desHeight * 3 / 2];
        YuvUtils.yuvRevert3399(src, desWidth, desHeight, result, 90, yStart, yStop, mirror, 0);
        long timeStop = System.currentTimeMillis();
        Log.d("timePast", (timeStop - timeStart) + " ms");
        return result;
    }
}


