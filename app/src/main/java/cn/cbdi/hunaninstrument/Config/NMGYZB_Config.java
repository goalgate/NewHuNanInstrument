//package cn.cbdi.hunaninstrument.Config;
//
//import cn.cbdi.hunaninstrument.Project_NMGYZB.NMGService;
//import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
//import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.HuNanFaceImpl;
//import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.IFace;
//
//public class NMGYZB_Config extends BaseConfig {
//
//    @Override
//    public boolean isFace() {
//        return true;
//    }
//
//    @Override
//    public boolean isTemHum() {
//        return false;
//    }
//
//    @Override
//    public String getPrefix() {
//        return "";
//    }
//
//    @Override
//    public String getDev_prefix() {
//        return "800100";
//    }
//
//    @Override
//    public String getPersonInfoPrefix() {
//        return "cjy/s/fbcjy_updata?";
//    }
//
//    @Override
//    public String getUpDataPrefix() {
//        return "cjy/s/fbcjy_updata?";
//    }
//
//    @Override
//    public String getServerId() {
//        return "http://113.140.1.138:8890/";
//    }
//
//    @Override
//    public int getCheckOnlineTime() {
//        return 60;
//    }
//
//    @Override
//    public String getModel() {
//        return "CBDI-P-IC";
//    }
//
//    @Override
//    public String getName() {
//        return "库房采集器";
//    }
//
//    @Override
//    public String getProject() {
//        return "NMGYZB";        //新危管
//    }
//
//    @Override
//    public String getPower() {
//        return "12-18V 2A";
//    }
//
//    @Override
//    public boolean isCheckTime() {
//        return false;
//    }
//
//    @Override
//    public boolean disAlarm() {
//        return true;
//    }
//
//    @Override
//    public boolean collectBox() {
//        return false;
//    }
//
//    @Override
//    public boolean noise() {
//        return false;
//    }
//
//    @Override
//    public boolean doubleCheck() {
//        return true;
//    }
//
//    @Override
//    public void readCard() {
//        IDCardPresenter.getInstance().ReadIC();
//    }
//
//    @Override
//    public void stopReadCard() {
//        IDCardPresenter.getInstance().StopReadIC();
//    }
//
//    @Override
//    public boolean fingerprint() {
//        return true;
//    }
//
//    @Override
//    public Class getServiceName() {
//        return NMGService.class;
//    }
//
//    @Override
//    public String getMainActivity() {
//        return ".Project_NMGYZB.NMGMainActivity";
//    }
//
//    @Override
//    public String getAddActivity() {
//        return ".Project_NMGYZB.NMGAddActivity";
//    }
//
//    @Override
//    public boolean TouchScreen() {
//        return true;
//    }
//
//    @Override
//    public boolean MenKongSuo() {
//        return false;
//    }
//
//    @Override
//    public IFace getFaceImpl() {
//        return new HuNanFaceImpl();
//    }
//
//    @Override
//    public boolean XungengCanOpen() {
//        return false;
//    }
//
//
//    @Override
//    public boolean DoorMonitorChosen() {
//        return false;
//    }
//
//    @Override
//    public boolean isHongWai() {
//        return hongWai;
//    }
//
//    @Override
//    public void setHongWai(boolean hongWai) {
//        this.hongWai = hongWai;
//
//    }
//}
