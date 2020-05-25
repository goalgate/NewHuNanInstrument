package cn.cbsd.cjyfunctionlib.Func_FingerPrint.module;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by zbsz on 2017/6/2.
 */

public interface IFingerPrint {
    void onInit(Context context,IFPListener listener);

    void onOpen(IFPListener listener);

    void onClose(IFPListener listener);

    void onCancel(boolean status);

    void onEnroll(String id, IFPListener listener);

    void onVerify(String id, IFPListener listener);

    void onIdentify(IFPListener listener);

    void onGetEnrollCount(IFPListener listener);

    int onGetEmptyID(IFPListener listener);

    void onCaptureImg(IFPListener listener);

    void onRemoveTmpl(String TmplId, IFPListener listener);

    void onRemoveAll(IFPListener listener);

    String onUpTemplate(String id, IFPListener listener);

    void onDownTemplate(String id, String temp, IFPListener listener);

    interface IFPListener {

        void onSetImg(Bitmap bmp);

        void onText(String msg);

        void onFpSucc(String msg);
    }
}
