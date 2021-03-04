package cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter;


import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.OutputControlImpl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.view.IOutputControlView;

/**
 * Created by zbsz on 2017/8/23.
 */

public class OutputControlPresenter {

    private IOutputControlView view;

    private OutputControlPresenter() {
    }

    private static OutputControlPresenter instance = null;

    public static OutputControlPresenter getInstance() {
        if (instance == null)
            instance = new OutputControlPresenter();
        return instance;
    }

    public void SwitchPresenterSetView(IOutputControlView view) {
        this.view = view;
    }

    IOutputControl switchingModule = new OutputControlImpl();

    public void Open() {
        switchingModule.onOpen(new IOutputControl.IOutputControlListener() {

            @Override
            public void onDoorState(Door.DoorState state) {
                if (view != null) {
                    view.onDoorState(state);
                }
            }

            @Override
            public void onTemHum(int temperature, int humidity, String THSwitchValue) {
                if (view != null) {
                    view.onTemHum(temperature, humidity, THSwitchValue);
                }
            }

            @Override
            public void onSwitchValue(String Value) {
                if (view != null) {
                    view.onSwitchValue(Value);
                }
            }
        });
    }

    public void readHum(int CircleTime, boolean status) {
        switchingModule.onReadHum(CircleTime, status);
    }

    public void on12V_Alarm(boolean isOn) {
        switchingModule.on12V_Alarm(isOn);
    }

    public void buzz(IOutputControl.Hex hex) {
        switchingModule.onBuzz(hex);
    }

    public void greenLight() {
        switchingModule.onGreenLightBlink();
    }

    public void redLight() {
        switchingModule.onRedLightBlink();
    }

    public void WhiteLight(boolean status) {
        switchingModule.onWhiteLight(status);
    }

    public void onElectricLock(IOutputControl.Hex hex, boolean status) {
        switchingModule.onElectricLock(hex, status);
    }

    public void Close() {
        switchingModule.onClose();
    }

    public int getTemperature() {
        return switchingModule.getTem();
    }

    public int getHumidity() {
        return switchingModule.getHum();
    }

}
