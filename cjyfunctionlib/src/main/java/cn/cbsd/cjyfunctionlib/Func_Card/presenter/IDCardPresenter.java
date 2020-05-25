package cn.cbsd.cjyfunctionlib.Func_Card.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_Card.module.IDCardImpl;
import cn.cbsd.cjyfunctionlib.Func_Card.module.IIDCard;
import cn.cbsd.cjyfunctionlib.Func_Card.view.IIDCardView;


/**
 * Created by zbsz on 2017/6/9.
 */

public class IDCardPresenter {
    private IIDCardView view;

    private static IDCardPresenter instance = null;

    private IDCardPresenter() {}


    public static IDCardPresenter getInstance() {
        if (instance == null)
            instance = new IDCardPresenter();
        return instance;
    }

    public void IDCardPresenterSetView(IIDCardView view) {
        this.view = view;
    }

    IIDCard idCardModule = new IDCardImpl();

    public void idCardOpen() {
        try {
            idCardModule.onOpen(new IIDCard.IIdCardListener() {
                @Override
                public void onSetImg(Bitmap bmp) {
                    view.onsetCardImg(bmp);
                }

                @Override
                public void onSetInfo(ICardInfo cardInfo) {
                    view.onsetCardInfo(cardInfo);
                }

                @Override
                public void onSetICInfo(ICardInfo cardInfo) {
                    view.onsetICCardInfo(cardInfo);
                }

                @Override
                public void onSetText(String Msg) {
                    view.onSetText(Msg);
                }
            });
        }catch (Exception e){
            Log.e("idCardOpen",e.toString());
        }

    }

    public void ReadID() {
        try {
            idCardModule.onReadID();

        }catch (Exception e){
            Log.e("readCard",e.toString());
        }
    }

    public void StopReadID() {
        try {
            idCardModule.onStopReadID();

        }catch (Exception e){
            Log.e("stopReadCard",e.toString());
        }
    }

    public void idCardClose() {
        try {
            idCardModule.onClose();

        }catch (Exception e){
            Log.e("idCardClose",e.toString());
        }
    }

    public void readSam() {
        try {
            idCardModule.onReadSAM();
        }catch (Exception e){
            Log.e("readSam",e.toString());
        }
    }

    public void StopReadIC() {
        try {
            idCardModule.onStopReadIC();

        }catch (Exception e){
            Log.e("StopReadIC",e.toString());
        }
    }

    public void ReadIC() {
        try {
            idCardModule.onReadIC();

        }catch (Exception e){
            Log.e("ReadIC",e.toString());
        }
    }
}
