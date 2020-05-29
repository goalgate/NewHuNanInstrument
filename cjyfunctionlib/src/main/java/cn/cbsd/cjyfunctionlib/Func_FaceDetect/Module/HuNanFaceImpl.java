package cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.api.FaceApi;
import com.baidu.idl.main.facesdk.callback.FaceDetectCallBack;
import com.baidu.idl.main.facesdk.callback.FaceFeatureCallBack;
import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.camera.Camera1PreviewManager;
import com.baidu.idl.main.facesdk.camera.Camera2PreviewManager;
import com.baidu.idl.main.facesdk.db.DBManager;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.manager.FaceTrackManager;
import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Feature;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.model.User;
import com.baidu.idl.main.facesdk.utils.BitmapUtils;
import com.baidu.idl.main.facesdk.utils.FaceOnDrawTexturViewUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO;

public class HuNanFaceImpl implements IFace {

    private IFaceListener listener;

    private static final int mWidth = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();

    private static final int mHeight = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    private AutoTexturePreviewView mPreviewView;

    private AutoTexturePreviewView mPreviewView1;

    private TextureView mDrawDetectFaceView;

    private boolean useRGBCamera = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private ExecutorService es = Executors.newSingleThreadExecutor();

    private static FacePresenter.FaceAction action = FacePresenter.FaceAction.Normal;

    Disposable VerifyTimeOut;

    Disposable IdentifyTimeOut;

//    private Bitmap global_bitmap;

    private byte[] global_BitmapBytes;

    private Bitmap headphotoIR;

    private Bitmap headphotoRGB;

    private Bitmap VerifyBitmap;

    private String userName;

    private String userInfo;

    boolean IDENTITYING = false;

    String mGroupId = "cbsd";

    Context mContext;

    @Override
    public void FaceInit(Context context, SdkInitListener listener) {

        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        mContext = context;
        FaceSDKManager.getInstance().init(context, listener);
    }

    @Override
    public void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView, IFaceListener listener) {
        this.listener = listener;
        startCameraPreview(context, previewView, previewView1, textureView);
    }

    @Override
    public void FaceIdentify() {
        action = FacePresenter.FaceAction.Identify;
        IdentifyTimeOut = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    action = FacePresenter.FaceAction.Normal;
                    listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "尝试获取人脸超时,请重试");
                });
    }

    @Override
    public void FaceIdentify_model() {
        action = FacePresenter.FaceAction.Identify_Model;

    }

    @Override
    public void FaceVerify(String userName, String userInfo, Bitmap bitmap) {
        useRGBCamera(true);
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    action = FacePresenter.FaceAction.Verify;
                });
        VerifyTimeOut = Observable.timer(20, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    listener.onText(action, FacePresenter.FaceResultType.verify_failed, "尝试获取人脸超时,请重试");
                    action = FacePresenter.FaceAction.Normal;
                });
        this.VerifyBitmap = bitmap;
        this.userName = userName;
        this.userInfo = userInfo;
    }

    @Override
    public void FaceReg(String userName, String userInfo) {
        this.userName = userName;
        this.userInfo = userInfo;
        action = FacePresenter.FaceAction.Reg;
    }

    @Override
    public boolean FaceRegByBase64(String userName, String userInfo, String base64) {
        Bitmap mBitmap = FileUtils.base64ToBitmap(base64);
        byte[] bytes = new byte[512];
        float ret = syncFeature(mBitmap, bytes);
        if (ret == 128) {
            String imageName = mGroupId + "-" + userInfo + ".jpg";
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(mGroupId, userName, imageName,
                    userInfo, bytes);
            if (isSuccess) {
                FaceApi.getInstance().initDatabases(true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean IMG_to_IMG(Bitmap bmp1, Bitmap bmp2, boolean IDCard_HeadPhoto, boolean useThread) {
        if (useThread) {
            es.submit(() -> {
                byte[] bytes1 = new byte[512];
                byte[] bytes2 = new byte[512];
                float ret1 = syncFeature(bmp1, bytes1);
                if (ret1 == 128) {
                    float ret2 = syncFeature(bmp2, bytes2);
                    if (ret2 == 128) {
                        if (IDCard_HeadPhoto) {
                            handler.post(() -> {
                                float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                        BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                                        bytes1, bytes2, true);
                                listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, String.valueOf((int) score));
                                listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "人证比对通过");
                                listener.onBitmap(action, FacePresenter.FaceResultType.headphotoRGB, bmp1);
                            });
                        } else {
                            float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                    BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                                    bytes1, bytes2, true);
                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, String.valueOf((int) score));
                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "比对通过");
                        }

                    } else {
                        handler.post(() -> {
                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "输入照片无法提取人脸特征,请更改照片");
                        });
                    }
                } else {
                    handler.post(() -> {
                        listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
                        listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "输入照片无法提取人脸特征,请更改照片");
                    });
                }
            });
            return true;
        } else {
            byte[] bytes1 = new byte[512];
            byte[] bytes2 = new byte[512];
            float ret1 = syncFeature(bmp1, bytes1);
            if (ret1 == 128) {
                float ret2 = syncFeature(bmp2, bytes2);
                if (ret2 == 128) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }


    @Override
    public void FaceSetNoAction() {
        action = FacePresenter.FaceAction.Normal;

    }

    @Override
    public void setIdentifyStatus(int i) {

    }

    @Override
    public void FaceIdentifyReady() {
        es.submit(() -> {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            int count = FaceApi.getInstance().getUserList(mGroupId).size();
            Log.e("人脸库内人脸数目:", String.valueOf(count));
            handler.post(() ->
                    Toast.makeText(mContext, "人脸库内人脸数目为 ：" + count, Toast.LENGTH_LONG).show()
            );
        });
    }

    @Override
    public void PreviewCease(CeaseListener listener) {
        DBManager.getInstance().release();
        Camera1PreviewManager.getInstance().stopPreview();
        Camera1PreviewManager.getInstance().release();
        Camera2PreviewManager.getInstance().stopPreview();
        Camera2PreviewManager.getInstance().release();
        if (mPreviewView != null) {
            mPreviewView = null;
        }
        if (mPreviewView != null) {
            mPreviewView1 = null;
        }
        if (mDrawDetectFaceView != null) {
            mDrawDetectFaceView = null;
        }
        System.gc();
        listener.CeaseCallBack();
    }

    @Override
    public void useRGBCamera(boolean status) {
        useRGBCamera = status;

    }

    @Override
    public Bitmap getGlobalBitmap() {
        Bitmap global_bitmap = byteToBitmap(global_BitmapBytes, mWidth, mHeight);
        return global_bitmap;
    }

    @Override
    public void SetGroupID(String groupId) {
        mGroupId = groupId;
    }

    @Override
    public void FaceDeleteByUserName(String userName) {
        if (FaceApi.getInstance().userDeleteByName(userName, mGroupId)) {
            Toast.makeText(mContext, userName + "删除成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, userName + "不存在", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void FaceDeleteByUserId(String userId) {
        if (FaceApi.getInstance().userDelete(userId, mGroupId)) {
            Toast.makeText(mContext, userName + "删除成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, userName + "不存在", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void FaceVerifyAndReg(String userName, String userInfo, Bitmap bitmap) {
        useRGBCamera(true);
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    action = FacePresenter.FaceAction.VerifyAndReg_Verify;
                });
        VerifyTimeOut = Observable.timer(20, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((l) -> {
                    listener.onText(action, FacePresenter.FaceResultType.verify_failed, "尝试获取人脸超时,请重试");
                    action = FacePresenter.FaceAction.Normal;
                });
        this.VerifyBitmap = bitmap;
        this.userName = userName;
        this.userInfo = userInfo;
    }

    @Override
    public void FaceUpdate(Bitmap bitmap, String userName, UserInfoManager.UserInfoListener userInfoListener) {
        if (IDENTITYING) {
            String imageName = mGroupId + "-" + userName + ".jpg";
            UserInfoManager.getInstance().updateImage(bitmap, mGroupId, userName, imageName, new UserInfoManager.UserInfoListener() {
                @Override
                public void updateImageSuccess(Bitmap bitmap) {
                    super.updateImageSuccess(bitmap);
                    FaceApi.getInstance().initDatabases(true);
                    userInfoListener.updateImageSuccess(bitmap);

                }

                @Override
                public void updateImageFailure(String message) {
                    super.updateImageFailure(message);
                    userInfoListener.updateImageFailure(message);
                }
            });
        }

    }

    @Override
    public void FaceGroupDelete(String groupId) {
        UserInfoManager.getInstance().deleteUserGroupListInfo(groupId, new UserInfoManager.UserInfoListener());
    }

    @Override
    public User GetUserByUserName(String userName) {
        return FaceApi.getInstance().getUserListByUserName(mGroupId, userName).get(0);
    }

    @Override
    public User GetUserByIdInTable(int IdInTable) {
        return FaceApi.getInstance().getUserListById(IdInTable);
    }

    @Override
    public void FaceRegOrUpdateByFeature(String userName, String userInfo, byte[] feature, boolean Reg) {
        if (Reg) {
            String imageName = mGroupId + "-" + userInfo + ".jpg";
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(mGroupId, userName, imageName,
                    userInfo, feature);
            if (isSuccess) {
                FaceApi.getInstance().initDatabases(true);

            } else {

            }

        } else {
            String imageName = mGroupId + "-" + userName + ".jpg";
            boolean update = FaceApi.getInstance().userUpdate(mGroupId, userName, imageName, feature);
            if (update) {
                FaceApi.getInstance().initDatabases(true);
            }
        }


    }

    private void startCameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView) {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        this.mPreviewView = previewView;
        this.mPreviewView1 = previewView1;
        this.mDrawDetectFaceView = textureView;
        this.mDrawDetectFaceView.setOpaque(false);
        // 不需要屏幕自动变黑。
        this.mDrawDetectFaceView.setKeepScreenOn(true);
        this.mPreviewView1.getTextureView().setScaleX(-1);
//        this.textureView.setScaleX(-1);
        Camera2PreviewManager.getInstance().setCameraFacing(Camera2PreviewManager.CAMERA_FACING_FRONT);//彩色
        Camera2PreviewManager.getInstance().startPreview(context, mPreviewView, mWidth, mHeight,
                (data, camera, width, height) -> {
                    global_BitmapBytes = data;
//                    Observable.just(data)
//                            .subscribeOn(Schedulers.computation())
//                            .unsubscribeOn(Schedulers.computation())
//                            .flatMap(new Function<byte[], ObservableSource<Bitmap>>() {
//                                @Override
//                                public ObservableSource<Bitmap> apply(byte[] bytes) throws Exception {
//                                    Bitmap bmp = byteToBitmap(data, width, height);
//                                    return Observable.just(bmp);
//                                }
//                            }).observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Consumer<Bitmap>() {
//                                @Override
//                                public void accept(Bitmap bitmap) throws Exception {
//                                    global_bitmap = bitmap;
//                                }
//                            });
                    if (useRGBCamera) {
                        faceDetect(data, width, height);

                    }
                });
        Camera1PreviewManager.getInstance().setCameraFacing(Camera1PreviewManager.CAMERA_FACING_BACK);//红外
        Camera1PreviewManager.getInstance().startPreview(context, mPreviewView1, mWidth, mHeight,
                (data, camera, width, height) -> {
                    if (!useRGBCamera) {
                        if (action.equals(FacePresenter.FaceAction.Identify) || action.equals(FacePresenter.FaceAction.Identify_Model)) {
                            IDENTITYING = true;
                            FaceSDKManager.getInstance().onDetectCheck(data, null, null,
                                    height, width, SingleBaseConfig.getBaseConfig().getType(), new FaceDetectCallBack() {
                                        @Override
                                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                                checkResult(livenessModel);
                                            }
                                        }

                                        @Override
                                        public void onTip(int code, String msg) {
                                        }

                                        @Override
                                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                            if (livenessModel == null || livenessModel.getTrackFaceInfo() == null
                                                    || livenessModel.getTrackFaceInfo().length == 0) {
                                                OutputControlPresenter.getInstance().WhiteLight(false);
                                            }
                                            if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                                                showFrame(livenessModel);
                                            }

                                        }
                                    });
                        } else {
                            faceDetect(data, width, height);
                        }
                    }
                });
    }

    private void faceDetect(byte[] data, int width, int height) {
        // 摄像头预览数据进行人脸检测
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        if (Integer.valueOf(liveType) == 1) { // 无活体检测
            FaceTrackManager.getInstance().setAliving(false);
        } else if (Integer.valueOf(liveType) == 2) { // 活体检测
            FaceTrackManager.getInstance().setAliving(true);
        }
        IDENTITYING = false;

        FaceTrackManager.getInstance().faceTrack(data, width, height, new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                if (livenessModel == null || livenessModel.getTrackFaceInfo() == null
                        || livenessModel.getTrackFaceInfo().length == 0) {
                    if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                        if (mDrawDetectFaceView == null) {
                            return;
                        }
                        Canvas canvas = mDrawDetectFaceView.lockCanvas();
                        if (canvas == null) {
                            mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                            return;
                        }
                        // 清空canvas
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        OutputControlPresenter.getInstance().WhiteLight(false);

                        mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    }
                    return;
                }

                if (SingleBaseConfig.getBaseConfig().getDetectFrame().equals("wireframe")) {
                    showFrame(livenessModel);
                    checkResult(livenessModel);
                }

            }

            @Override
            public void onTip(int code, final String msg) {

            }

            @Override
            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {

            }
        });


    }


    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
        paint.setStrokeWidth(5);
    }

    private RectF rectF = new RectF();

    private void showFrame(final LivenessModel model) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mDrawDetectFaceView == null) {
                    return;
                }
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);

                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mPreviewView, model.getBdFaceImageInstance());
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制框
                canvas.drawRect(rectF, paint);
                OutputControlPresenter.getInstance().WhiteLight(true);

                mDrawDetectFaceView.unlockCanvasAndPost(canvas);

            }
        });
    }

    private void checkResult(LivenessModel model) {
        if (model == null) {
            return;
        }

        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // 无活体
        if (Integer.valueOf(liveType) == 1) {
            BDFaceImageInstance image = model.getBdFaceImageInstance();
            switch (action) {
                case Reg:
                    action = FacePresenter.FaceAction.Normal;
                    register(model);
                    break;
                case Verify:
                    if (VerifyTimeOut != null) {
                        VerifyTimeOut.dispose();
                    }
                    headphotoRGB = BitmapUtils.getInstaceBmp(image);
                    IMG_to_IMG(headphotoRGB, VerifyBitmap, true, true);
                    action = FacePresenter.FaceAction.Normal;
                    useRGBCamera(false);
                    break;
                case Identify:
                    identity(model, false);
                    break;
                case Identify_Model:
                    identity(model, true);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case VerifyAndReg_Verify:
                    if (VerifyTimeOut != null) {
                        VerifyTimeOut.dispose();
                    }
                    headphotoRGB = BitmapUtils.getInstaceBmp(image);
                    if (IMG_to_IMG(headphotoRGB, VerifyBitmap, true, false)) {
                        useRGBCamera(false);
                        action = FacePresenter.FaceAction.Reg;
                        handler.post(() -> {
                            listener.onBitmap(action, FacePresenter.FaceResultType.headphotoRGB, headphotoRGB);
                            listener.onText(action, FacePresenter.FaceResultType.verify_success, "人证比对通过,等待抽取特征值");
                        });
                    } else {
                        handler.post(() -> listener.onText(action, FacePresenter.FaceResultType.verify_failed, "人证比对分数过低"));
                    }
                    break;
                case Normal:
                    break;
            }
        }
    }

    private void register(LivenessModel model) {
        if (model == null) {
            return;
        }
        if (userInfo == null || mGroupId == null) {
            return;
        }
        if (IDENTITYING) {
            return;
        }
        BDFaceImageInstance image = model.getBdFaceImageInstance();
        headphotoIR = BitmapUtils.getInstaceBmp(image);
        handler.post(() -> {
            listener.onBitmap(action, FacePresenter.FaceResultType.headphotoIR, headphotoIR);
        });
        // 获取选择的特征抽取模型
        int modelType = SingleBaseConfig.getBaseConfig().getActiveModel();
        if (modelType == 1) {
            // 生活照
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            displayCompareResult(featureSize, feature);
                            Log.e("qing", String.valueOf(feature.length));
                        }

                    });

        } else if (Integer.valueOf(modelType) == 2) {
            // 证件照
            FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                    BDFACE_FEATURE_TYPE_ID_PHOTO, new FaceFeatureCallBack() {
                        @Override
                        public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                            displayCompareResult(featureSize, feature);
                        }
                    });
        }
    }

    private void displayCompareResult(float ret, byte[] faceFeature) {
        // 特征提取成功
        if (ret == 128) {
            String imageName = mGroupId + "-" + userName + ".jpg";
            // 注册到人脸库
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(mGroupId, userName, imageName,
                    userInfo, faceFeature);
            if (isSuccess) {
                Log.e("qing", "注册成功");
                handler.post(() -> {
                    listener.onText(action, FacePresenter.FaceResultType.Reg_success, "人脸数据录入成功");
                    listener.onUser(action, FacePresenter.FaceResultType.Reg_success, GetUserByUserName(userName));

                });
                // 数据变化，更新内存
                FaceApi.getInstance().initDatabases(true);
            } else {
                handler.post(() -> {
                    listener.onText(action, FacePresenter.FaceResultType.Reg_failed, "人员数据录入失败");
                });
            }
        } else if (ret == -1) {
            handler.post(() -> {
                listener.onText(action, FacePresenter.FaceResultType.Reg_failed, "抽取特征失败");
            });
        } else {
            handler.post(() -> {
                listener.onText(action, FacePresenter.FaceResultType.Reg_failed, "抽取特征失败");
            });
        }
    }


    private void identity(LivenessModel livenessModel, boolean isN) {

        if (!isN) {
            FaceSetNoAction();
            IdentifyTimeOut.dispose();
        }
        User user = livenessModel.getUser();
        if (user == null) {
            handler.post(() -> {
                listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "系统没有找到相关人脸信息");
                listener.onLivenessModel(action, FacePresenter.FaceResultType.Identify_failed, livenessModel);

            });
        } else {
            BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
            headphotoIR = BitmapUtils.getInstaceBmp(image);
            Bitmap global_bitmap = byteToBitmap(global_BitmapBytes, mWidth, mHeight);
            handler.post(() -> {
                listener.onBitmap(action, FacePresenter.FaceResultType.Identify_success, global_bitmap);
                listener.onBitmap(action, FacePresenter.FaceResultType.headphotoIR, headphotoIR);
                listener.onText(action, FacePresenter.FaceResultType.Identify_success, String.valueOf((int) livenessModel.getFeatureScore()));
                listener.onUser(action, FacePresenter.FaceResultType.Identify_success, user);
                listener.onLivenessModel(action, FacePresenter.FaceResultType.Identify_success, livenessModel);
            });

        }
    }


    private float syncFeature(final Bitmap bitmap, final byte[] feature) {

        float ret = -1;
        if (!IDENTITYING) {
            BDFaceImageInstance rgbInstance = new BDFaceImageInstance(bitmap);

            FaceInfo[] faceInfos = null;
            int count = 10;
            // 现在人脸检测加入了防止多线程重入判定，假如之前线程人脸检测未完成，本次人脸检测有可能失败，需要多试几次
            while (count != 0) {
                faceInfos = FaceSDKManager.getInstance().getFaceDetect()
                        .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
                count--;
                if (faceInfos != null) {
                    break;
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 检测结果判断
            if (faceInfos != null && faceInfos.length > 0) {

                // 判断质量检测，针对模糊度、遮挡、角度
                if (qualityCheck(faceInfos[0])) {
                    ret = FaceSDKManager.getInstance().getFaceFeature().feature(BDFaceSDKCommon.FeatureType.
                            BDFACE_FEATURE_TYPE_LIVE_PHOTO, rgbInstance, faceInfos[0].landmarks, feature);
                    Log.i("qing", "ret:" + ret);
                }

            }
        }

        return ret;
    }


    public boolean qualityCheck(final FaceInfo faceInfo) {

        // 不是相册选的图片，不必再次进行质量检测，因为采集图片的时候已经做过了

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (faceInfo != null) {

            // 模糊结果过滤
            float blur = faceInfo.bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                return false;
            }

            // 光照结果过滤
            float illum = faceInfo.illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                return false;
            }

            // 遮挡结果过滤
            if (faceInfo.occlusion != null) {
                BDFaceOcclusion occlusion = faceInfo.occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                } else {
                    return true;
                }
            }
        }
        return false;
    }


    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    public Bitmap byteToBitmap(byte[] imgByte, int width, int height) {


//        YuvImage image = new YuvImage(imgByte, ImageFormat.NV21, width, height, null);
//        ByteArrayOutputStream os = new ByteArrayOutputStream(imgByte.length);
//        if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)) {
//            return null;
//        }
//        byte[] tmp = os.toByteArray();
//
//
//        InputStream input = null;
//        Bitmap bitmap = null;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        input = new ByteArrayInputStream(tmp);
//        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
//                input, null, options));
//        bitmap = (Bitmap) softRef.get();
//        if (imgByte != null) {
//            imgByte = null;
//        }
//        try {
//            if (input != null) {
//                input.close();
//                os.close();
//
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return bitmap;


        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(imgByte.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(imgByte);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);

        return bmpout;

    }

}
