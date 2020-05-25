package cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper;




public class Door {

    public enum DoorState {
        State_Open, State_Close
    }

    private DoorState mdoorState = DoorState.State_Close;

    private Door() {
    }

    private static Door instance = null;

    public static Door getInstance() {
        if (instance == null)
            instance = new Door();
        return instance;
    }

    public void setMdoorState(DoorState mdoorState) {
        this.mdoorState = mdoorState;
    }

    public DoorState getMdoorState() {
        return mdoorState;
    }

    public void doNext() {

    }

}
