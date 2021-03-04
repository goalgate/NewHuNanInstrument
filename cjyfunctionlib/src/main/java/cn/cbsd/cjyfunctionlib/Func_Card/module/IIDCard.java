package cn.cbsd.cjyfunctionlib.Func_Card.module;

import android.content.Context;
import android.graphics.Bitmap;

import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;


/**
 * Created by zbsz on 2017/6/4.
 */

public interface IIDCard {
    void onOpen(IIdCardListener mylistener, Context context);

    void onReadID();

    void onReadSAM();

    void onStopReadID();

    void onReadIC();

    void onStopReadIC();

    void onClose();

    interface IIdCardListener {
        void onSetImg(Bitmap bmp);

//        void onSetInfo(CardInfoRk123x cardInfo);

        void onSetInfo(ICardInfo cardInfo);

        void onSetICInfo(ICardInfo cardInfo);

        void onSetText(String Msg);
    }


}
