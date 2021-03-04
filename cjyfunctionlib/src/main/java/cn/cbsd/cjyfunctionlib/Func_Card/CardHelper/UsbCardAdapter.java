package cn.cbsd.cjyfunctionlib.Func_Card.CardHelper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;


import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderExceptionListener;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.util.HashMap;
import java.util.Map;


import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;

public class UsbCardAdapter implements ICardInfo {

    private static String TAG = UsbCardAdapter.class.getSimpleName();

    private Handler mHandler = new Handler();
    //国人
    //姓名
    private String name_ = "";
    //姓别
    private String sex_ = "";
    //民族
    private String nation_ = "";
    //出生
    private String birthday_ = "";
    //住址
    private String address_ = "";
    //公民身份号码
    private String cardId_ = "";
    //签发机关
    private String dept_ = "";
    //有效期截止日期
    private String validDate_ = "";


    //港澳同胞
    //通行证号
    private String passNum = "";

    //通行次数
    private int visaTimes = 0;

    //外国人
    //中文名称
    private String strCnName = "";
    //英文名称
    private String strEnName = "";
    //国家
    private String strCountry = "";
    //出生日期
    private String strBorn = "";
    //身份证ID
    private String strID = "";
    //有效期限
    private String strEffext = "";
    //签发机关
    private String strIssueAt = "";

    private int VID = 1024;    //IDR VID

    private int PID = 50010;     //IDR PID

    private UsbManager musbManager = null;

    ICardState iCardState_;  //事件接口

    private static String ACTION_USB_PERMISSION = "com.cbsd.USBReceiver";

    private IDCardReader idCardReader = null;

    private final int cardInfoget = 4;

    private int readType_ = 0;  //读卡类型

    private int readState_ = 0;  //返回状态

    private boolean bopen = false;

    private boolean bStoped = false;

    private Bitmap headPhoto;

    Context mContext;

    boolean readID = false;

    public UsbCardAdapter(Context context, int VID, int PID, ICardState iCardState) {
        if (VID != 0) {
            this.VID = VID;
        } else if (PID != 0) {
            this.PID = PID;
        }
        this.iCardState_ = iCardState;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        mContext = context;
        mContext.registerReceiver(mUsbReceiver, intentFilter);
    }

    @Override
    public void close() {
        if (!bopen) {
            return;
        }
        bStoped = true;
        try {
            idCardReader.close(0);
        } catch (IDCardReaderException e) {
            e.printStackTrace();
        }
        bopen = false;
    }

    @Override
    public void ReadID() {
        readID = true;

    }

    @Override
    public void stopReadID() {
        readID = false;

    }

    @Override
    public Bitmap getBmp() {
        return headPhoto;
    }


    @Override
    public void ReadIC() {

    }

    @Override
    public void stopReadIC() {

    }


    @Override
    public int open() {
        int result = -1;
        musbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        for (UsbDevice device : musbManager.getDeviceList().values()) {
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
                musbManager.requestPermission(device, pendingIntent);
                result = 0;
            }
        }
        return result;
    }

    @Override
    public void readSam() {

    }

    @Override
    public String getSam() {
        return null;
    }

    @Override
    public String cardId() {
        return cardId_;
    }

    @Override
    public String name() {
        return name_;
    }

    @Override
    public String getUid() {
        return null;
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (mContext) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        OpenDevice();
                    } else {
//                        ToastUtils.showLong("USB未授权");
                    }
                }
            }
        }
    };


    public void OpenDevice() {
        if (bopen) {
            return;
        }
        try {
            startIDCardReader();
            IDCardReaderExceptionListener listener = (() -> {
                //出现异常，关闭设备
                close();
                //当前线程为工作线程，若需操作界面，请在UI线程处理
                Log.e(TAG, "设备发生异常，断开连接！");

            });
            idCardReader.open(0);
            idCardReader.setIdCardReaderExceptionListener(listener);
            bStoped = false;
            bopen = true;
            new Thread(() -> {
                while (!bStoped) {
                    if (!readID) {
                        continue;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean ret = false;
                    final long nTickstart = System.currentTimeMillis();
                    try {
                        idCardReader.findCard(0);
                        idCardReader.selectCard(0);
                    } catch (IDCardReaderException e) {
                        continue;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int retType = 0;
                    try {
                        retType = idCardReader.readCardEx(0, 0);
                    } catch (IDCardReaderException e) {
                        Log.e(TAG, "读卡失败，错误信息：" + e.getMessage());
                    }
                    if ((retType == 1 || retType == 2 || retType == 3)) {
                        final long nTickUsed = (System.currentTimeMillis() - nTickstart);
                        final int final_retType = retType;
                        if (final_retType == 1) {
                            final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                            //姓名adb
                            name_ = idCardInfo.getName();
                            //民族
                            nation_ = idCardInfo.getNation();
                            //出生日期
                            birthday_ = idCardInfo.getBirth();
                            //住址
                            address_ = idCardInfo.getAddress();
                            //身份证号
                            cardId_ = idCardInfo.getId();
                            //有效期限
                            validDate_ = idCardInfo.getValidityTime();
                            //签发机关
                            dept_ = idCardInfo.getDepart();

                            if (idCardInfo.getPhotolength() > 0) {
                                byte[] buf = new byte[WLTService.imgLength];
                                if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                    headPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
//                                                imageView.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                                }
                            }
                        } else if (final_retType == 2) {
                            final IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                            //中文名
                            strCnName = idprpCardInfo.getCnName();
                            //英文名
                            strEnName = idprpCardInfo.getEnName();
                            //国家/国家地区代码
                            strCountry = idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode();//国家/国家地区代码
                            //出生日期
                            strBorn = idprpCardInfo.getBirth();
                            //身份证号
                            strID = idprpCardInfo.getId();
                            //有效期限
                            strEffext = idprpCardInfo.getValidityTime();
                            //签发机关
                            strIssueAt = "公安部";
                            if (idprpCardInfo.getPhotolength() > 0) {
                                byte[] buf = new byte[WLTService.imgLength];
                                if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                                    headPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                }
                            }
                        } else {
                            final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                            //姓名
                            name_ = idCardInfo.getName();
                            //民族,港澳台不支持该项
                            nation_ = "";
                            //出生日期
                            birthday_ = idCardInfo.getBirth();
                            //住址
                            address_ = idCardInfo.getAddress();
                            //身份证号
                            cardId_ = idCardInfo.getId();
                            //有效期限
                            validDate_ = idCardInfo.getValidityTime();
                            //签发机关
                            dept_ = idCardInfo.getDepart();
                            //通行证号
                            passNum = idCardInfo.getPassNum();
                            //签证次数
                            visaTimes = idCardInfo.getVisaTimes();
                            if (idCardInfo.getPhotolength() > 0) {
                                byte[] buf = new byte[WLTService.imgLength];
                                if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                    headPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                }
                            }
                        }

                    }
                    readType_ = cardInfoget;
                    mHandler.post(() -> {
                        iCardState_.onCardState(readType_, 1);
                    });
                }
            }).start();
        } catch (IDCardReaderException e) {
            Log.e(TAG, "开始读卡失败，错误码：" + e.getErrorCode() + "\n错误信息：" + e.getMessage() + "\n内部代码=" + e.getInternalErrorCode());
        }

    }

    private void startIDCardReader() {
        if (null != idCardReader) {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        // Start fingerprint sensor
        Map idrparams = new HashMap();
        idrparams.put(ParameterHelper.PARAM_KEY_VID, VID);
        idrparams.put(ParameterHelper.PARAM_KEY_PID, PID);
        idCardReader = IDCardReaderFactory.createIDCardReader(mContext, TransportType.USB, idrparams);
        idCardReader.setLibusbFlag(true);
    }
}
