package cn.cbdi.hunaninstrument.Config;

import cn.cbdi.hunaninstrument.Project_YanCheng.YanChengService;
import cn.cbdi.hunaninstrument.Service.RK3399UpdateService;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;


public class YanChengConfig extends BaseConfig {
    @Override
    public boolean isFace() {
        return true;
    }

    @Override
    public boolean isTemHum() {
        return true;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getDev_prefix() {
        return "";
    }

    @Override
    public String getPersonInfoPrefix() {
        return "caijiyiDuijie/s/kaiguanmen";
    }

    @Override
    public String getUpDataPrefix() {
        return "caijiyiDuijie/s/kaiguanmen";
    }


    @Override
    public String getServerId() {
        return "http://124.172.232.83:8007/";
    }

    @Override
    public int getCheckOnlineTime() {
        return 60;
    }

    @Override
    public String getModel() {
        return "CBDI-DA-01";
    }

    @Override
    public String getName() {
        return "防爆采集器";
    }

    @Override
    public String getProject() {
        return "YanChengMB";        //盐城
    }

    @Override
    public String getPower() {
        return "12-18V 2A";
    }

    @Override
    public boolean isCheckTime() {
        return false;
    }

    @Override
    public boolean disAlarm() {
        return true;
    }

    @Override
    public boolean collectBox() {
        return false;
    }

    @Override
    public boolean noise() {
        return false;
    }

    @Override
    public boolean doubleCheck() {
        return true;
    }

    @Override
    public void readCard() {
        IDCardPresenter.getInstance().ReadID();
        IDCardPresenter.getInstance().ReadIC();

    }

    @Override
    public void stopReadCard() {
        IDCardPresenter.getInstance().StopReadID();
        IDCardPresenter.getInstance().StopReadIC();
    }

    @Override
    public boolean fingerprint() {
        return false;
    }

    @Override
    public Class getServiceName() {
        return YanChengService.class;
    }

    @Override
    public String getMainActivity() {
        return ".Project_YanCheng.YanChengMainActivity";
    }

    @Override
    public String getAddActivity() {
        return "";
    }

    @Override
    public boolean TouchScreen() {
        return false;
    }

    @Override
    public boolean MenKongSuo() {
        return true;
    }

    @Override
    public boolean XungengCanOpen() {
        return true;
    }

    @Override
    public boolean DoorMonitorChosen() {
        return false;
    }

    @Override
    public boolean isHongWai() {
        return hongWai;
    }

    @Override
    public void setHongWai(boolean hongWai) {
        this.hongWai = hongWai;
    }

    @Override
    public boolean Remote() {
        return false;
    }

    @Override
    public Class getUpdateService() {
        return RK3399UpdateService.class;
    }
}

