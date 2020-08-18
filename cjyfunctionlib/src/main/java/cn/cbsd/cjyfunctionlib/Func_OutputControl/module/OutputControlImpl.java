package cn.cbsd.cjyfunctionlib.Func_OutputControl.module;

import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPort;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class OutputControlImpl implements IOutputControl {


    private byte[] buf_ = new byte[2048];
    private int bufCount = 0;
    private int checkCount_ = 0;
    private String testStr = "";
    private byte[] switchingValue = new byte[8]; //开关量状态
    private Calendar switchingTime = Calendar.getInstance(); //取开关时状态时间
    private Calendar temHumTime = Calendar.getInstance(); //取温湿度时间
    private int temperature = 0;  //温度
    private int humidity = 0;   //湿度

    IOutputControlListener listener;
    private byte[] dt_temHum_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x55, 0x63, 0x7E, 0x6B};
    //温湿度命令

    private byte[] dt_greenLightBlink = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00,
            (byte) 0x0B, 0x01, 0x01, (byte) 0xD1, (byte) 0x92, (byte) 0x93, 0x63};
    private byte[] dt_redLightBlink = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00,
            (byte) 0x0B, 0x02, 0x02, (byte) 0x91, (byte) 0x63, (byte) 0x93, 0x63};
    private byte[] dt_whiteLightOn = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00, 0x0A,
            0x01, 0x01, (byte) 0x80, 0x52, (byte) 0x93, 0x63};
    private byte[] dt_whiteLightOff = {0x02, 0x02, (byte) 0x0B, 0x00, (byte) 0xA2, 0x00, 0x02, 0x02, 0x03, 0x00, 0x0A
            , 0x01, 0x00, (byte) 0x41, (byte) 0x92, (byte) 0x93, 0x63};

    private byte[] dt_buzz2 = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x22, 0x45, 0x35, (byte) 0xDF};
    private byte[] dt_buzz_ = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x0B, 0x0B, 0x02, 0x33, (byte) 0x7B,
            0x23};

    //    新命令20190812

    //    12V继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_12Vrelay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x01,
            (byte) 0x00, (byte) 0x58, (byte) 0xF1};
    private byte[] dt_12Vrelay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x00,
            (byte) 0x11, (byte) 0x84, (byte) 0x66};
    private byte[] dt_12Vrelay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x12, (byte) 0x21, (byte) 0x00,
            (byte) 0x22, (byte) 0xC4, (byte) 0x73};


    //    继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x01,
            (byte) 0x00, (byte) 0x4C, (byte) 0xF0};
    private byte[] dt_relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x00,
            (byte) 0x11, (byte) 0x85, (byte) 0xF9};
    private byte[] dt_relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x11, (byte) 0x51, (byte) 0x00,
            (byte) 0x22, (byte) 0xC5, (byte) 0xEC};


    //    D10继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_D10relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x01,
            (byte) 0x00, (byte) 0xC3, (byte) 0x3E};
    private byte[] dt_D10relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x00,
            (byte) 0x11, (byte) 0xD1, (byte) 0xFF};
    private byte[] dt_D10relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x04, (byte) 0x70, (byte) 0x00,
            (byte) 0x22, (byte) 0x91, (byte) 0xEA};


    //    D5继电器  (第六位0x0Y  Y 1~A 代表100MS~1S)
    private byte[] dt_D5relay = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x01,
            (byte) 0x00, (byte) 0x22, (byte) 0x7D};
    private byte[] dt_D5relay_open = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x00,
            (byte) 0x11, (byte) 0x61, (byte) 0x45};
    private byte[] dt_D5relay_close = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x03, (byte) 0x47, (byte) 0x00,
            (byte) 0x22, (byte) 0x21, (byte) 0x50};


    private int light_devfd = -1;

    private int switch_devfd = -1;

    private SerialPort light_port;

    private SerialPort switch_port;

    private InputStream light_InputStream;

    private OutputStream light_OutputStream;

    private InputStream switch_InputStream;

    private OutputStream switch_OutputStream;

    private ReadThread mReadThread;

    @Override
    public void onOpen(IOutputControlListener listener) {
        this.listener = listener;
        light_devOpen(115200, "/dev/ttyS2");
        switch_devOpen(115200, "/dev/ttyS0");
    }

    Disposable disposable;

    @Override
    public void onReadHum(int CircleTime, boolean status) {
        if (status) {
            disposable = Observable.interval(0, CircleTime, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> sendData(dt_temHum_));
        } else {
            if (disposable != null) {
                disposable.dispose();
            }
        }


    }

    @Override
    public void on12V_Alarm(boolean status) {
        if (status) {
            sendData(dt_12Vrelay_open);
        } else {
            sendData(dt_12Vrelay_close);
        }
    }

    @Override
    public void onBuzz(Hex hex) {
        sendData(dt_buzz_);

    }


    @Override
    public void onElectricLock(Hex hex, boolean status) {
        if (!status) {
            sendData(dt_relay_close);
        } else {
            if (hex == Hex.H0) {
                sendData(dt_relay_open);
            } else {
                sendData(adjust(dt_relay, hex));
            }
        }
    }

    @Override
    public void onRedLightBlink() {
        try {
            light_OutputStream.write(dt_whiteLightOff);
            light_OutputStream.write(dt_redLightBlink);
        } catch (Exception ex) {
            Log.e("onRedLightBlink", ex.toString());
        }
    }

    @Override
    public void onGreenLightBlink() {
        try {
            light_OutputStream.write(dt_whiteLightOff);

            light_OutputStream.write(dt_greenLightBlink);
        } catch (Exception ex) {
            Log.e("M121_sendData", ex.toString());
        }
    }

    @Override
    public void onWhiteLight(boolean status) {

        try {
            if (status) {
                light_OutputStream.write(dt_whiteLightOn);
            } else {
                light_OutputStream.write(dt_whiteLightOff);
            }
        } catch (Exception ex) {
            Log.e("onWhiteLight", ex.toString());
        }
    }


    @Override
    public void onClose() {
        thread_continuous = false;
    }

    private void sendData(byte[] bs) {
        try {
            switch_OutputStream.write(bs);
        } catch (Exception ex) {
            Log.e("M121_sendData", ex.toString());
        }
    }

    private int light_devOpen(int sp, String devName_) {
        try {
            light_port = new SerialPort(new File(devName_), sp, 0);
            light_InputStream = light_port.getInputStream();
            light_OutputStream = light_port.getOutputStream();
            Log.e("switch_dev", "open  SerialPort ok");
            light_devfd = 1;
        } catch (Exception e) {
            Log.e("switch_dev", e.toString());
        }
        return light_devfd;
    }

    private int switch_devOpen(int sp, String devName_) {
        try {
            switch_port = new SerialPort(new File(devName_), sp, 0);
            switch_InputStream = switch_port.getInputStream();
            switch_OutputStream = switch_port.getOutputStream();
            Log.e("switch_dev", "open  SerialPort ok");
            switch_devfd = 1;
        } catch (Exception e) {
            Log.e("switch_dev", e.toString());
        }
        if (mReadThread == null) {
            mReadThread = new ReadThread();
            thread_continuous = true;
            mReadThread.start();
        }
        return switch_devfd;
    }

    boolean thread_continuous = false;
    private byte[] readerbuffer = new byte[20];
    byte[] by_copy;
    String testStrTemp;

    class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (thread_continuous) {
                try {
                    int size = switch_InputStream.read(readerbuffer);

                    by_copy = new byte[size];

                    System.arraycopy(readerbuffer, 0, by_copy, 0, size);

                    testStrTemp = "";

                    for (int i = 0; i < size; i++) {
                        testStrTemp += byteToHex(by_copy[i]);
                    }

                    if ((bufCount += size) >= 9) {
                        bufCount = 0;
                        testStr += testStrTemp;
                        if (testStr.contains("AAAAAA")) {
                            testStr = testStr.substring(testStr.lastIndexOf("AAAAAA"),
                                    testStr.lastIndexOf("AAAAAA") + 18);

                            mhandler.sendEmptyMessage(0x123);
                        }
                        if (testStr.contains("BBBBBB")) {
                            testStr = testStr.substring(testStr.indexOf("BBBBBB"),
                                    testStrTemp.indexOf("BBBBBB") + 18);
                            temperature = (int) hexStr2Bytes(testStr.substring(10, 12))[0];
                            humidity = (int) hexStr2Bytes(testStr.substring(6, 8))[0];
                            mhandler.sendEmptyMessage(0x234);
                        }
                        Thread.sleep(300);
                        testStr = "";
                    } else {
                        testStr += testStrTemp;
                    }
                } catch (Exception e) {
                    Log.e("switch_dev", e.toString());
                }
            }

        }
    }

    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    public String byteToHex(byte b) {
        String s = "";
        s = Integer.toHexString(0xFF & b).trim();
        if (s.length() < 2) {
            s = "0" + s;
        }

        return s.toUpperCase();
    }

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                try {
                    Log.e("switch", testStr);
                    listener.onSwitchValue(testStr);
                    if (testStr.equals("AAAAAA000001000000") || testStr.equals("AAAAAA000000000000")) {
                        if (testStr.substring(10, 12).equals("01")) {
                            listener.onDoorState(Door.DoorState.State_Close);
                        } else {
                            listener.onDoorState(Door.DoorState.State_Open);
                        }
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e("OutputControlImpl", e.toString());
                } catch (Exception e) {
                    Log.e("OutputControlImpl", e.toString());
                }


            } else if (msg.what == 0x234) {
                listener.onTemHum(temperature, humidity, testStr);
            }
        }
    };

    private byte[] adjust(byte[] order, Hex hex) {
        switch (hex) {
            case H1:
                order[5] = 0x01;
                break;
            case H2:
                order[5] = 0x02;
                break;
            case H3:
                order[5] = 0x03;
                break;
            case H4:
                order[5] = 0x04;
                break;
            case H5:
                order[5] = 0x05;
                break;
            case H6:
                order[5] = 0x06;
                break;
            case H7:
                order[5] = 0x07;
                break;
            case H8:
                order[5] = 0x08;
                break;
            case H9:
                order[5] = 0x09;
                break;
            case HA:
                order[5] = 0x0A;
                break;
            default:
                break;
        }
        return order;
    }

}
