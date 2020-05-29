package cn.cbdi.hunaninstrument.Tool;


public class UDPRun implements Runnable {
    private UDPState uds_ = null;

    public UDPRun(UDPState uds) {
        uds_ = uds;
    }

    public void run() {
        uds_.send();
    }

}