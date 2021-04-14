package cn.cbdi.hunaninstrument.Config;


import cn.cbdi.hunaninstrument.Service.UpdateService;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.FaceImpl2;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.IFace;

/**
 * Created by zbsz on 2017/12/19.
 */

public abstract class BaseConfig {

    public abstract boolean isTemHum();

    public abstract boolean isFace();

    public abstract String getServerId();

    public abstract String getPrefix();

    public abstract String getUpDataPrefix();

    public abstract String getPersonInfoPrefix();

    public abstract String getDev_prefix();

    public abstract int getCheckOnlineTime();

    public abstract String getName();

    public abstract String getModel();

    public abstract String getProject();

    public abstract String getPower();

    public abstract boolean isCheckTime();

    public abstract boolean disAlarm();

    public abstract boolean collectBox();

    public abstract boolean noise();

    public abstract boolean doubleCheck();

    public abstract void readCard();

    public abstract void stopReadCard();

    public abstract Class getServiceName();

    public abstract boolean fingerprint();

    public abstract String getMainActivity();

    public abstract String getAddActivity();

    public abstract boolean TouchScreen();

    public abstract boolean MenKongSuo();

    public abstract boolean XungengCanOpen();

    public abstract boolean DoorMonitorChosen();

    public boolean Remote(){
        return false;
    }; // 是否开启远程

    boolean hongWai = false;

    public abstract boolean isHongWai();

    public abstract void setHongWai(boolean hongWai);

    public boolean useServer(){
        return false;
    }

    public Class getUpdateService(){
        return UpdateService.class;
    }

    public IFace getFaceImpl(){
        return new FaceImpl2();
    }

}
