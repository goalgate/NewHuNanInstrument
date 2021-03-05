package cn.cbdi.hunaninstrument.Config;


import cn.cbdi.hunaninstrument.Project_HuNan.HuNanService;
import cn.cbdi.hunaninstrument.Service.HuNanUpdateService;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.HuNanFaceImpl;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.IFace;

public class HuNanConfig extends BaseConfig {
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
        return "cjy/s/personInfo";
    }

    @Override
    public String getUpDataPrefix() {
        return "cjy/s/updata";
    }


    @Override
    public String getServerId() {
        return "http://129.204.110.143:8031/";
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
        return "HNMBY";        //湖南民爆云
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
        return true;
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
        return HuNanService.class;
    }

    @Override
    public String getMainActivity() {
        return ".Project_HuNan.HuNanMainActivity";
    }

    @Override
    public String getAddActivity() {
        return ".Project_HuNan.HuNanRegActivity";
    }

    @Override
    public boolean TouchScreen() {
        return false;
    }

    @Override
    public boolean MenKongSuo() {
        return false;
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
        this.hongWai= hongWai;
    }

    @Override
    public Class getUpdateService() {
        return HuNanUpdateService.class;
    }

    @Override
    public boolean Remote() {
        return true;
    }
}
