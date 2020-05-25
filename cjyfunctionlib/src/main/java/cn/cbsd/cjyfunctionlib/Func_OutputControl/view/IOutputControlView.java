package cn.cbsd.cjyfunctionlib.Func_OutputControl.view;

import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;

/**
 * Created by zbsz on 2017/8/23.
 */

public interface IOutputControlView {

    void onDoorState(Door.DoorState state);

    void onTemHum(int temperature, int humidity,String THSwitchValue);

}
