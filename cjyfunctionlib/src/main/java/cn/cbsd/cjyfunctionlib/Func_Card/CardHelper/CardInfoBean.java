package cn.cbsd.cjyfunctionlib.Func_Card.CardHelper;

import android.graphics.Bitmap;

public class CardInfoBean implements ICardInfo {

    public CardInfoBean() {
    }

    public CardInfoBean(String cardID, String name) {
        this.cardID = cardID;
        this.name = name;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setName(String name) {
        this.name = name;
    }

    String cardID;
    String name;

    @Override
    public String getUid() {
        return null;
    }

    @Override
    public String getSam() {
        return null;
    }

    @Override
    public Bitmap getBmp() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public String cardId() {
        return cardID;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void ReadID() {

    }

    @Override
    public void readSam() {

    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public void stopReadID() {

    }

    @Override
    public void ReadIC() {

    }

    @Override
    public void stopReadIC() {

    }
}
