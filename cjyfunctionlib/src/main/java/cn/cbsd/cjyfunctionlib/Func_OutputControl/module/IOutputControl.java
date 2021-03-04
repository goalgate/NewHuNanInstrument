package cn.cbsd.cjyfunctionlib.Func_OutputControl.module;

import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;

/**
 * Created by zbsz on 2017/8/23.
 */

public interface IOutputControl {

    enum Hex {
        H0, H1, H2, H3, H4, H5, H6, H7, H8, H9, HA
    }

    void onOpen(IOutputControlListener listener);

    void onReadHum(int CircleTime,boolean status);

    void on12V_Alarm(boolean status);

    void onBuzz(Hex hex);

    void onElectricLock(Hex hex, boolean status);

    void onRedLightBlink();

    void onGreenLightBlink();

    void onWhiteLight(boolean status);

    void onClose();

    int getTem();

    int getHum();

    interface IOutputControlListener{

        void onDoorState(Door.DoorState state);

        void onTemHum(int temperature, int humidity,String THSwitchValue);

        void onSwitchValue(String Value);

    }

}
