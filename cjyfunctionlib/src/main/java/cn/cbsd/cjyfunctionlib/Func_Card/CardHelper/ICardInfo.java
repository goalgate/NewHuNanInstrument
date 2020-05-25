package cn.cbsd.cjyfunctionlib.Func_Card.CardHelper;

import android.graphics.Bitmap;

public interface ICardInfo {

    int open();

    void close();

    void ReadID();

    void stopReadID();

    void ReadIC();

    void stopReadIC();

    void readSam();

    String getSam();

    Bitmap getBmp();

    String cardId();

    String name();

    String getUid();
}
