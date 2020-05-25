package cn.cbsd.cjyfunctionlib.Func_FingerPrint.view;

import android.graphics.Bitmap;


/**
 * Created by zbsz on 2017/6/9.
 */

public interface IFingerPrintView {
    void onSetImg(Bitmap bmp);

    void onText(String msg);

    void onFpSucc(String msg);
}
