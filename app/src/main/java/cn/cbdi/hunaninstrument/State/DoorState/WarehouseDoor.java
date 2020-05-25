package cn.cbdi.hunaninstrument.State.DoorState;

import org.greenrobot.eventbus.EventBus;

import cn.cbdi.hunaninstrument.EventBus.OpenDoorEvent;
import cn.cbdi.hunaninstrument.State.LockState.Lock;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;


/**
 * Created by zbsz on 2017/9/27.
 */

public class WarehouseDoor {

    public static final String DoorOpenString = "AAAAAA000000000000";

    public static final String DoorCloseString = "AAAAAA000001000000";

    private Door.DoorState mdoorState = Door.DoorState.State_Close;

    private WarehouseDoor() {
    }

    private static WarehouseDoor instance = null;

    public static WarehouseDoor getInstance() {
        if (instance == null)
            instance = new WarehouseDoor();
        return instance;
    }

    public void setMdoorState(Door.DoorState mdoorState) {
        this.mdoorState = mdoorState;
    }

    public Door.DoorState getMdoorState() {
        return mdoorState;
    }

    public void doNext() {
        switch (mdoorState) {
            case State_Open:
                if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Lockup)) {
                    EventBus.getDefault().post(new OpenDoorEvent(false));
                } else if (Lock.getInstance().getState().equals(Lock.LockState.STATE_Unlock)) {
                    EventBus.getDefault().post(new OpenDoorEvent(true));
                }
                Lock.getInstance().doNext();
                break;
            case State_Close:
                break;
            default:
                break;
        }
    }

//    private DoorState doorState;
//
//    private WarehouseDoor(DoorState doorState) {
//        this.doorState = doorState;
//    }
//
//    public DoorState getDoorState() {
//        return doorState;
//    }
//
//    public void setDoorState(DoorState doorState) {
//        this.doorState = doorState;
//    }
//
//
//    public void doNext(){
//        doorState.onHandle(this);
//    }
}
