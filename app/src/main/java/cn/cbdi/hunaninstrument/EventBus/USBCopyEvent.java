package cn.cbdi.hunaninstrument.EventBus;

public class USBCopyEvent {
    private int status;

    public int getStatus() {
        return status;
    }

    public USBCopyEvent(int status) {
        this.status = status;
    }
}
