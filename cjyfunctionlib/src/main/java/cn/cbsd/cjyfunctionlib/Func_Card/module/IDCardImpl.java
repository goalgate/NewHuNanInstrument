package cn.cbsd.cjyfunctionlib.Func_Card.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardState;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ReadCard2;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.UsbCardAdapter;


/**
 * Created by zbsz on 2017/6/4.
 */

public class IDCardImpl implements IIDCard {
    private static final String TAG = "信息提示";
    private int cdevfd = -1;
    private static ICardInfo cardInfo = null;
    IIdCardListener mylistener;


    @Override
    public void onOpen(IIdCardListener listener ,Context context) {
        mylistener = listener;
        try {
//            cardInfo = new BoyaCardAdapter(115200, "/dev/ttyS0", m_onCardState);
            if (Build.DEVICE.startsWith("Apollo7")) {
                cardInfo = new UsbCardAdapter(context, 0, 0, m_onCardState);
            } else {
                cardInfo = new ReadCard2(115200, "/dev/ttyS1", m_onCardState);
            }
            cdevfd = cardInfo.open();
            if (cdevfd >= 0) {
                Log.e(TAG, "打开身份证读卡器成功");
            } else {
                cdevfd = -1;
                Log.e(TAG, "打开身份证读卡器失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReadID() {
        cardInfo.ReadID();
    }


    @Override
    public void onReadIC() {
        cardInfo.ReadIC();
    }

    @Override
    public void onStopReadID() {
        cardInfo.stopReadID();
    }

    @Override
    public void onStopReadIC() {
        cardInfo.stopReadIC();
    }

    @Override
    public void onReadSAM() {
        cardInfo.readSam();
    }

    private ICardState m_onCardState = new ICardState() {
        @Override
        public void onCardState(int itype, int value) {
            if (itype == 4 && value == 1) {
                mylistener.onSetInfo(cardInfo);
                Bitmap bmp = cardInfo.getBmp();
                if (bmp != null) {
                    mylistener.onSetImg(bmp);
                    mylistener.onSetInfoAndImg(cardInfo,bmp);
                } else {
                    mylistener.onSetImg(null);
                    Log.e("信息提示", "没有照片");
                }
            } else if (itype == 20) {
                mylistener.onSetText("SAM:" + cardInfo.getSam());
            } else if (itype == 14) {
                mylistener.onSetICInfo(cardInfo);
            }

        }

    };

    @Override
    public void onClose() {
        cardInfo.close();
    }
}
