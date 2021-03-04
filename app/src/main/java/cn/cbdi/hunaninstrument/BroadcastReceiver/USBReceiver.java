package cn.cbdi.hunaninstrument.BroadcastReceiver;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.User;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.Config.HuNanConfig;
import cn.cbdi.hunaninstrument.Config.YanChengConfig;
import cn.cbdi.hunaninstrument.EventBus.FaceIdentityEvent;
import cn.cbdi.hunaninstrument.EventBus.USBCopyEvent;
import cn.cbdi.hunaninstrument.Service.UpdateService;
import cn.cbdi.hunaninstrument.greendao.DaoSession;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.ApkUtils;
import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import jxl.Sheet;
import jxl.Workbook;

import static cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant.NEW_APK_PATH;

public class USBReceiver extends BroadcastReceiver {
    private static final String TAG = USBReceiver.class.getSimpleName();
    private static final String MOUNTS_FILE = "/proc/mounts";
    private StorageManager mStorageManager;

    DaoSession mdaosession = AppInit.getInstance().getDaoSession();

    boolean founded = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String mountPath = intent.getData().getPath();
            Uri data = intent.getData();
            Log.d(TAG, "mountPath = " + mountPath);
            if (!TextUtils.isEmpty(mountPath)) {
                //读取到U盘路径再做其他业务逻辑
                SPUtils.getInstance().put("UsbPath", mountPath);
                boolean mounted = isMounted(mountPath);
                Log.d(TAG, "onReceive: " + "U盘挂载" + mounted);
//                getUName();
                if (AppInit.getInstrumentConfig().getClass().getName().equals(YanChengConfig.class.getName())
                        ||AppInit.getInstrumentConfig().getClass().getName().equals(HuNanConfig.class.getName())) {
                    founded = false;
                    if (FacePresenter.getInstance().isReady()) {
                        getAllFiles(new File(mountPath));
                        if (!founded) ToastUtils.showLong("没有找到离线数据包");
                    } else {
                        handler.post(() -> ToastUtils.showLong("程序还没准备好,请稍后再插入U盘"));
                    }

                }
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Log.d(TAG, "onReceive: " + "U盘移除了");
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            //如果是开机完成，则需要调用另外的方法获取U盘的路径
        }
    }


    /**
     * 判断是否有U盘插入,当U盘开机之前插入使用该方法.
     *
     * @param path
     * @return
     */
    public static boolean isMounted(String path) {
        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));

            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }


    /**
     * 获取U盘的路径和名称
     */
    private void getUName() {
        Class<?> volumeInfoClazz = null;
        Method getDescriptionComparator = null;
        Method getBestVolumeDescription = null;
        Method getVolumes = null;
        Method isMountedReadable = null;
        Method getType = null;
        Method getPath = null;
        List<?> volumes = null;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");
            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            volumes = (List<?>) getVolumes.invoke(mStorageManager);

            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    String p1 = (String) getBestVolumeDescription.invoke(mStorageManager, vol);
                    String p2 = path2.getPath();
                    Log.d(TAG, "-----------path1-----------------" + p1);               //打印U盘卷标名称
                    Log.d(TAG, "-----------path2 @@@@@-----------------" + p2);         //打印U盘路径
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x123) {
                EventBus.getDefault().post(new USBCopyEvent(1));
            } else if (msg.what == 0x234) {
                EventBus.getDefault().post(new USBCopyEvent(2));

            }
            super.handleMessage(msg);
        }
    };

    private void getAllFiles(File path) {
        File files[] = path.listFiles();
        if (files != null) {
            for (File f : files) {
                Log.e("name", f.getName().toString());
                if (f.getName().toString().equals("offline_data")) {
                    Observable.create(e -> {
                        handler.sendEmptyMessage(0x123);
                        Thread.sleep(1000);
                        e.onNext(f);
                    }).subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe((o) -> {
                                faceDataOperation((File) o);
                                handler.sendEmptyMessage(0x234);
                            });
                    Log.e("offline_data", "founded");
                    founded = true;
                    return;
                } else if (f.getName().toString().equals("YC_update.apk")) {
                    ApkUtils.installApk(AppInit.getContext(), f.getAbsolutePath());
                }
                if (f.isDirectory()) {
                    getAllFiles(f);
                }
            }
        }
    }

    private void faceDataOperation(File offline_data) {
        try {
            String excel_filepath = offline_data.getAbsolutePath() + File.separator + "人员数据表.xls";
            File excel_file = new File(excel_filepath);
            if (!excel_file.exists()) {
                handler.post(() -> ToastUtils.showLong("没有找到离线数据包"));
                return;
            }
            FileInputStream inputStream = new FileInputStream(excel_file);
            Workbook workbook = Workbook.getWorkbook(inputStream);
            mdaosession.deleteAll(Employer.class);
            List<Keeper> keeperList = mdaosession.getKeeperDao().loadAll();
            for (Keeper keeper : keeperList) {
                try {
                    mdaosession.queryRaw(Employer.class, "where CARD_ID = '" + keeper.getCardID() + "'").get(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("delete", keeper.getName());

                    mdaosession.delete(keeper);
                    FacePresenter.getInstance().FaceDeleteByUserName(keeper.getName());

                }
            }
            for (Sheet sheet : workbook.getSheets()) {
                int sheetRows = sheet.getRows();
                Log.e(TAG + sheetRows, String.valueOf(sheetRows));
                for (int i = 1; i < sheetRows; i++) {
                    String idnum = sheet.getCell(1, i).getContents();
                    if (TextUtils.isEmpty(idnum)) {
                        continue;
                    }
                    String name = sheet.getCell(2, i).getContents();
                    String type = sheet.getCell(3, i).getContents();
                    String photo_path = sheet.getCell(4, i).getContents();
                    Bitmap photo = BitmapFactory.decodeFile(offline_data.getAbsolutePath() + File.separator + File.separator + photo_path);
                    String ps = FileUtils.bitmapToBase64(photo);
                    mdaosession.insertOrReplace(new Employer(idnum.toUpperCase(), Integer.parseInt(type)));
                    try {
                        Keeper keeper = mdaosession.queryRaw(Keeper.class, "where CARD_ID = '" + idnum.toUpperCase() + "'").get(0);
                        if (!TextUtils.isEmpty(ps) && keeper.getHeadphoto().length() != ps.length()) {

                            FacePresenter.getInstance().FaceUpdate(photo, name, new UserInfoManager.UserInfoListener() {
                                public void updateImageSuccess(Bitmap bitmap) {
                                    keeper.setHeadphoto(ps);
                                    keeper.setHeadphotoBW(null);
                                    mdaosession.getKeeperDao().insertOrReplace(keeper);
                                }

                                public void updateImageFailure(String message) {
                                    Log.e(TAG, message);
                                }
                            });
                        }
                    } catch (IndexOutOfBoundsException e) {
                        if (!TextUtils.isEmpty(ps)) {
                            if (FacePresenter.getInstance().FaceRegByBase64(name, idnum.toUpperCase(), ps)) {
                                User user = FacePresenter.getInstance().GetUserByUserName(name);
                                Keeper keeper = new Keeper(idnum.toUpperCase(),
                                        name, ps, null, null,
                                        user.getUserId(), user.getFeature());
                                mdaosession.getKeeperDao().insertOrReplace(keeper);
                                Log.e("myface", name + "人脸特征已存");

                            }
                        }
                    }
                }

            }
            workbook.close();
            showFinalUser();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            handler.post(() -> ToastUtils.showLong("无法解析离线数据包，请按照格式制作离线格式包"));
        }
    }

    private void showFinalUser() {
        try {
            StringBuffer logMen = new StringBuffer();

            List<Keeper> keeperList = mdaosession.loadAll(Keeper.class);
            if (keeperList.size() > 0) {
                Set<String> list = new HashSet<>();
                for (Keeper keeper : keeperList) {
                    list.add(keeper.getName());
                }
                for (String name : list) {
                    logMen.append(name + "、");
                }
                logMen.deleteCharAt(logMen.length() - 1);
                handler.post(() -> ToastUtils.showLong(logMen.toString() + "人脸特征已准备完毕"));

                Log.e(TAG, logMen.toString());

            } else {
                handler.post(() -> ToastUtils.showLong("该设备没有可使用的人脸特征"));

                Log.e(TAG, logMen.toString());

            }
        } catch (Exception e) {
            handler.post(() -> ToastUtils.showLong(e.toString()));
        }
    }
}