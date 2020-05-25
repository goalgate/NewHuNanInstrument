package cn.cbsd.cjyfunctionlib.Func_CollectionBox;


import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.INetDaSocketEvent;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.NetDAM0888Data;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.NetDAM0888Socket;
import cn.cbsd.cjyfunctionlib.Func_CollectionBox.CollectionBoxHelper.SensorAI;

public class SocketBuilder extends NetDAM0888Socket {

    DataBuilder data;

    public SocketBuilder setBuilderDATime(int time) {
        setDATime(time);
        return SocketBuilder.this;
    }


    public SocketBuilder setBuilderEvent(INetDaSocketEvent event) {
        setEvent(event);
        return SocketBuilder.this;
    }


    public SocketBuilder setBuilderNumber(int i) {
        setNumber(i);
        return SocketBuilder.this;
    }

    public SocketBuilder builder_open(String ip, int port) {
        open(ip, port);
        return SocketBuilder.this;
    }

    public void bindNetDAM0888Data(DataBuilder data) {
        this.data = data;
    }

    @Override
    protected void onAI() {
        super.onAI();
        data.setAI(data_ai);
        for (int i = 0; i <= 8; i++) {
            if (data.getAI(i)!=null){
                data.getAI(i).DataTrigger(data.getAI(i));
            }
        }
    }
}
