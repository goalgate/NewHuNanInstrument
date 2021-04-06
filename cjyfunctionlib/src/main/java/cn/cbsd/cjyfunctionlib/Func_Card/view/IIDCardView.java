package cn.cbsd.cjyfunctionlib.Func_Card.view;
import android.graphics.Bitmap;

import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;


/**
 * Created by zbsz on 2017/6/9.
 */
public interface IIDCardView {
    void onsetCardInfo(ICardInfo cardInfo);

    void onsetCardImg(Bitmap bmp);

    void onSetText(String Msg);

    void onsetICCardInfo(ICardInfo cardInfo);

    void onSetInfoAndImg(ICardInfo cardInfo,Bitmap bmp);

}
