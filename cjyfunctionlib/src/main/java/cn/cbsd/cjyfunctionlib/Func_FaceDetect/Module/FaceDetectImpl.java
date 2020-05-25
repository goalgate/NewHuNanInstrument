//package cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.TextureView;
//import android.widget.Toast;
//
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
//import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
//import cn.cbsd.cjyfunctionlib.Tools.FileUtils;
//import io.reactivex.Observable;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//
//public class FaceDetectImpl implements IFace {
//
//    private static final String TYPE_LIVENSS = "TYPE_LIVENSS";
//
//    private static final int TYPE_RGB_LIVENSS = 2;
//
//    private IFaceListener listener;
//
//    private static final int mWidth = 640;
//
//    private static final int mHeight = 480;
//
//    private Bitmap global_bitmap;
//
//    private Bitmap headphotoIR;
//
//    private Bitmap headphotoRGB;
//
//    private Bitmap VerifyBitmap;
//
//    private String userId;
//
//    private String userInfo;
//
//    private AutoTexturePreviewView mPreviewView;
//
//    private AutoTexturePreviewView mPreviewView1;
//
//    private TextureView textureView;
//
//    private final static double livnessScore = 0.0;
//
//    private static FacePresenter.FaceAction action = FacePresenter.FaceAction.Normal;
//
//    private boolean useRGBCamera = false;
//
//    private boolean mirror = true;
//
//    private ExecutorService es = Executors.newSingleThreadExecutor();
//
//    private Handler handler = new Handler(Looper.getMainLooper());
//
//    public static final int FEATURE_DATAS_UNREADY = 1;
//
//    public static final int IDENTITY_IDLE = 2;
//
//    public static final int IDENTITYING = 3;
//
//    private volatile int identityStatus = FEATURE_DATAS_UNREADY;
//
//    Disposable VerifyTimeOut;
//
//    Disposable IdentifyTimeOut;
//
//    String mGroupId = "1";
//
//    Context mContext;
//
//    @Override
//    public void FaceInit(Context context, FaceSDKManager.SdkInitListener listener) {
//        mContext = context;
//        PreferencesUtil.initPrefs(context);
//
//        DBManager.getInstance().init(context);
//        FaceSDKManager.getInstance().init(context, listener);
//        livnessTypeTip();
//    }
//
//    @Override
//    public void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView, IFaceListener listener) {
//        this.listener = listener;
//        startCameraPreview(context, previewView, previewView1, textureView);
//    }
//
//    @Override
//    public void FaceIdentify() {
//        action = FacePresenter.FaceAction.Identify;
//        IdentifyTimeOut = Observable.timer(10, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((l) -> {
//                    action = FacePresenter.FaceAction.Normal;
//                    listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "尝试获取人脸超时,请重试");
//                });
//    }
//
//    @Override
//    public void FaceIdentify_model() {
//        action = FacePresenter.FaceAction.Identify_Model;
//    }
//
//
//    @Override
//    public void FaceVerify(String userId, String userInfo, Bitmap bitmap) {
//        useRGBCamera(true);
//        Observable.timer(1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((l) -> {
//                    action = FacePresenter.FaceAction.Verify;
//                });
//        VerifyTimeOut = Observable.timer(20, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((l) -> {
//                    listener.onText(action, FacePresenter.FaceResultType.verify_failed, "尝试获取人脸超时,请重试");
//                    action = FacePresenter.FaceAction.Normal;
//                });
//        this.VerifyBitmap = bitmap;
//        this.userId = userId;
//        this.userInfo = userInfo;
//    }
//
//    @Override
//    public void FaceReg(String userId, String userInfo) {
//        this.userId = userId;
//        this.userInfo = userInfo;
//        action = FacePresenter.FaceAction.Reg;
//    }
//
//    @Override
//    public boolean FaceRegByBase64(String userId, String userInfo, String base64) {
//        Bitmap mBitmap = FileUtils.base64ToBitmap(base64);
//        User user = new User();
//        user.setUserId(userId);
//        user.setUserInfo(userInfo);
//        user.setGroupId(mGroupId);
//        byte[] bytes = new byte[512];
//        float ret = FaceApi.getInstance().extractVisFeature(mBitmap, bytes, 20);
//        if (ret != -1) {
//            Feature feature = new Feature();
//            feature.setGroupId(mGroupId);
//            feature.setUserId(userId);
//            feature.setFeature(bytes);
//            user.getFeatureList().add(feature);
//            if (FaceApi.getInstance().userAdd(user)) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean IMG_to_IMG(final Bitmap bmp1, final Bitmap bmp2, boolean IDCard_HeadPhoto, boolean useThread) {
//        if (useThread) {
//            es.submit(() -> {
//                byte[] bytes1 = new byte[512];
//                byte[] bytes2 = new byte[512];
//                float ret1 = FaceApi.getInstance().extractVisFeature(bmp1, bytes1, 50);
//                if (ret1 == 128) {
//                    float ret2 = FaceApi.getInstance().extractVisFeature(bmp2, bytes2, 50);
//                    if (ret2 == 128) {
//                        if (IDCard_HeadPhoto) {
//                            handler.post(() -> {
//                                listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, String.valueOf((int) FaceApi.getInstance().match(bytes1, bytes2)));
//                                listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "人证比对通过");
//                                listener.onBitmap(action, FacePresenter.FaceResultType.headphotoRGB, bmp1);
//                            });
//                        } else {
//                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, String.valueOf((int) FaceApi.getInstance().match(bytes1, bytes2)));
//                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_True, "比对通过");
//                        }
//
//                    } else {
//                        handler.post(() -> {
//                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
//                            listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "输入照片无法提取人脸特征,请更改照片");
//                        });
//                    }
//                } else {
//                    handler.post(() -> {
//                        listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_Score, "0");
//                        listener.onText(action, FacePresenter.FaceResultType.IMG_MATCH_IMG_False, "输入照片无法提取人脸特征,请更改照片");
//                    });
//                }
//            });
//            return true;
//        } else {
//            byte[] bytes1 = new byte[512];
//            byte[] bytes2 = new byte[512];
//            float ret1 = FaceApi.getInstance().extractVisFeature(bmp1, bytes1, 50);
//            if (ret1 == 128) {
//                float ret2 = FaceApi.getInstance().extractVisFeature(bmp2, bytes2, 50);
//                if (ret2 == 128) {
//                    return true;
//                } else {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        }
//
//
//    }
//
//
//    @Override
//    public void FaceSetNoAction() {
//        action = FacePresenter.FaceAction.Normal;
//    }
//
//    @Override
//    public void setIdentifyStatus(int i) {
//        identityStatus = i;
//    }
//
//    @Override
//    public void FaceIdentifyReady() {
//        es.submit(() -> {
//            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//            FaceApi.getInstance().loadFacesFromDB(mGroupId);
//            int count = FaceApi.getInstance().getGroup2Facesets().get(mGroupId).size();
//            handler.post(() ->
//                    Toast.makeText(mContext, "人脸库内人脸数目为 ：" + count, Toast.LENGTH_LONG).show()
//            );
//            Log.e("人脸库内人脸数目:", String.valueOf(count));
//            identityStatus = IDENTITY_IDLE;
//        });
//    }
//
//    @Override
//    public void PreviewCease(CeaseListener listener) {
//        CameraPreviewManager.getInstance().stopPreview();
//        CameraPreviewManager.getInstance().release();
//        AnotherPreviewManager.getInstance().stopPreview();
//        AnotherPreviewManager.getInstance().release();
//        if (mPreviewView != null) {
//            mPreviewView = null;
//        }
//        if (mPreviewView != null) {
//            mPreviewView1 = null;
//        }
//        if (textureView != null) {
//            textureView = null;
//        }
//        System.gc();
//        listener.CeaseCallBack();
//    }
//
//    @Override
//    public void FaceVerifyAndReg(String userId, String userInfo, Bitmap bitmap) {
//        useRGBCamera(true);
//        Observable.timer(1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((l) -> {
//                    action = FacePresenter.FaceAction.VerifyAndReg_Verify;
//                });
//        VerifyTimeOut = Observable.timer(20, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((l) -> {
//                    listener.onText(action, FacePresenter.FaceResultType.verify_failed, "尝试获取人脸超时,请重试");
//                    action = FacePresenter.FaceAction.Normal;
//                });
//        this.VerifyBitmap = bitmap;
//        this.userId = userId;
//        this.userInfo = userInfo;
//    }
//
//    @Override
//    public Bitmap getGlobalBitmap() {
//        return global_bitmap;
//    }
//
//
//    @Override
//    public void useRGBCamera(boolean status) {
//        useRGBCamera = status;
//    }
//
//    @Override
//    public void SetGroupID(String groupId) {
//        mGroupId = groupId;
//    }
//
//    @Override
//    public void FaceDelete(String userId) {
//        if(FaceApi.getInstance().userDelete(userId, mGroupId)){
//            Toast.makeText(mContext, userId + "删除成功", Toast.LENGTH_LONG).show();
//        }else {
//            Toast.makeText(mContext, userId + "不存在", Toast.LENGTH_LONG).show();
//
//        }
//    }
//
//    private void startCameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView) {
//        // 设置前置摄像头
//        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
//        // 设置后置摄像头
//        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
//        // 设置USB摄像头
//        this.mPreviewView = previewView;
//        this.mPreviewView1 = previewView1;
//        this.textureView = textureView;
//        this.textureView.setOpaque(false);
//        // 不需要屏幕自动变黑。
//        this.textureView.setKeepScreenOn(true);
//        this.mPreviewView1.getTextureView().setScaleX(-1);
////        this.textureView.setScaleX(-1);
//        AnotherPreviewManager.getInstance().setCameraFacing(AnotherPreviewManager.CAMERA_FACING_FRONT);//彩色
//        AnotherPreviewManager.getInstance().startPreview(context, mPreviewView, mWidth, mHeight,
//                (data, camera, width, height) -> {
//                    global_bitmap = Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
//                    if (useRGBCamera) {
//                        dealCameraData(data, width, height);
//                    }
//                });
//        FaceTrackManager.getInstance().setAliving(true); // 活体检测
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);//红外
//        CameraPreviewManager.getInstance().startPreview(context, mPreviewView1, mWidth, mHeight,
//                (data, camera, width, height) -> {
//                    if (!useRGBCamera) {
//                        dealCameraData(data, width, height);
//                    }
//                });
//    }
//
//    public void dealCameraData(int[] data, int width, int height) {
////        if (selectType == TYPE_PREVIEWIMAGE_OPEN) {
////            showDetectImage(width, height, data); // 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
////        }
//        // 摄像头预览数据进行人脸检测
//        faceDetect(data, width, height);
//
//    }
//
//    private void faceDetect(int[] argb, int width, int height) {
////        if (liveType == LivenessSettingActivity.TYPE_NO_LIVENSS) {
////            FaceTrackManager.getInstance().setAliving(false); // 无活体检测
////        } else if (liveType == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
////            FaceTrackManager.getInstance().setAliving(true); // 活体检测
////        }
//        FaceTrackManager.getInstance().faceTrack(argb, width, height, new FaceDetectCallBack() {
//            @Override
//            public void onFaceDetectCallback(LivenessModel livenessModel) {
//                checkResult(livenessModel);
//                handler.post(() -> showFrame(livenessModel));
//            }
//
//            @Override
//            public void onTip(int code, final String msg) {
//
//            }
//        });
//    }
//
//    boolean livenessSuccess = false;
//
//    private void checkResult(LivenessModel model) {
//        if (model == null) {
//            return;
//        }
//        livenessSuccess = (model.getRgbLivenessScore() > livnessScore) ? true : false;
//        if (livenessSuccess) {
//            switch (action) {
//                case Reg:
//                    action = FacePresenter.FaceAction.Normal;
//                    headphotoIR = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
//                    handler.post(() -> {
//                        listener.onBitmap(action, FacePresenter.FaceResultType.headphotoIR, headphotoIR);
//                    });
//                    register(model.getFaceInfo(), model.getImageFrame(), userId, userInfo);
//
//                    break;
//                case Verify:
//                    if (VerifyTimeOut != null) {
//                        VerifyTimeOut.dispose();
//                    }
//                    headphotoRGB = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
//
//                    IMG_to_IMG(headphotoRGB, VerifyBitmap, true, true);
//                    action = FacePresenter.FaceAction.Normal;
//                    useRGBCamera(false);
//                    break;
//                case Identify:
//                    identity(model.getImageFrame(), model.getFaceInfo());
//                    break;
//                case Identify_Model:
//                    identity_model(model.getImageFrame(), model.getFaceInfo());
//                    break;
//                case VerifyAndReg_Verify:
//                    if (VerifyTimeOut != null) {
//                        VerifyTimeOut.dispose();
//                    }
//                    headphotoRGB = FaceCropper.getFace(model.getImageFrame().getArgb(), model.getFaceInfo(), model.getImageFrame().getWidth());
//                    if (IMG_to_IMG(headphotoRGB, VerifyBitmap, true, false)) {
//                        useRGBCamera(false);
//                        action = FacePresenter.FaceAction.Reg;
//                        handler.post(() -> {
//                            listener.onBitmap(action, FacePresenter.FaceResultType.headphotoRGB, headphotoRGB);
//                            listener.onText(action, FacePresenter.FaceResultType.verify_success, "人证比对通过,等待抽取特征值");
//                        });
//                    } else {
//                        handler.post(() -> listener.onText(action, FacePresenter.FaceResultType.verify_failed, "人证比对分数过低"));
//                    }
//                    break;
//                case Normal:
//                    break;
//
//            }
//        }
//    }
//
//    private void register(final FaceInfo faceInfo, final ImageFrame imageFrame, String userId, String userInfo) {
//        /*
//         * 用户id（由数字、字母、下划线组成），长度限制128B
//         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
//         *
//         */
//        // String uid = 修改为自己用户系统中用户的id;
//        final User user = new User();
////        final String uid = UUID.randomUUID().toString();
//        user.setUserId(userId);
//        user.setUserInfo(userInfo);
//        user.setGroupId(mGroupId);
//        es.submit(() -> {
//            final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
//            ARGBImg argbImg = FeatureUtils.getImageInfo(headphotoIR);
//            byte[] bytes = new byte[512];
//            float ret = FaceApi.getInstance().extractVisFeature(argbImg, bytes, 50);
////                if (ret == -1) {
////                    toast("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
//////                    action = FacePresenter.FaceAction.Reg_ACTION;
////                } else
//            if (ret != -1) {
//                Feature feature = new Feature();
//                feature.setGroupId(mGroupId);
//                feature.setUserId(userId);
//                feature.setFeature(bytes);
//                user.getFeatureList().add(feature);
//                if (FaceApi.getInstance().userAdd(user)) {
//                    handler.post(() -> {
//                        listener.onBitmap(action, FacePresenter.FaceResultType.Reg_success, bitmap);
//                        listener.onUser(action, FacePresenter.FaceResultType.Reg_success, user);
//                        listener.onText(action, FacePresenter.FaceResultType.Reg_success, "人脸数据录入成功");
//                    });
//                    //saveFace(faceInfo,imageFrame,cardInfo);
//                } else {
//                    handler.post(() -> {
//                        listener.onText(action, FacePresenter.FaceResultType.Reg_failed, "人员数据录入失败");
//                    });
//                }
//            } else {
//                handler.post(() -> {
//                    listener.onText(action, FacePresenter.FaceResultType.Reg_failed, "抽取特征失败");
//                });
//            }
//        });
//    }
//
//    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {
//        if (identityStatus != IDENTITY_IDLE) {
//            return;
//        }
//        headphotoIR = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
//
//        float raw = Math.abs(faceInfo.headPose[0]);
//        float patch = Math.abs(faceInfo.headPose[1]);
//        float roll = Math.abs(faceInfo.headPose[2]);
//        // 人脸的三个角度大于20不进行识别
//        if (raw > 20 || patch > 20 || roll > 20) {
//            return;
//        }
//        identityStatus = IDENTITYING;
//        int[] argb = imageFrame.getArgb();
//        int rows = imageFrame.getHeight();
//        int cols = imageFrame.getWidth();
//        int[] landmarks = faceInfo.landmarks;
//        final IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, mGroupId);
//        if (identifyRet.getScore() < 80) {
//            if (IdentifyTimeOut != null) {
//                IdentifyTimeOut.dispose();
//            }
//            identityStatus = IDENTITY_IDLE;
//            handler.post(() -> {
//                FaceSetNoAction();
//                listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "系统没有找到相关人脸信息");
//            });
//            return;
//        } else {
//            if (IdentifyTimeOut != null) {
//                IdentifyTimeOut.dispose();
//            }
//            identityStatus = IDENTITY_IDLE;
//            final User user = FaceApi.getInstance().getUserInfo(mGroupId, identifyRet.getUserId());
//            if (user == null) {
//                handler.post(() -> {
//                    FaceSetNoAction();
//                    listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "系统没有找到相关人脸信息");
//                });
//            } else {
//                handler.post(() -> {
//                    FaceSetNoAction();
//                    listener.onBitmap(action, FacePresenter.FaceResultType.Identify_success, global_bitmap);
//                    listener.onBitmap(action, FacePresenter.FaceResultType.headphotoIR, headphotoIR);
//                    listener.onText(action, FacePresenter.FaceResultType.Identify_success, String.valueOf((int) identifyRet.getScore()));
//                    listener.onUser(action, FacePresenter.FaceResultType.Identify_success, user);
//                });
//            }
//        }
//    }
//
//
//    private void identity_model(ImageFrame imageFrame, FaceInfo faceInfo) {
//        if (identityStatus != IDENTITY_IDLE) {
//            return;
//        }
//        headphotoIR = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
//
//        float raw = Math.abs(faceInfo.headPose[0]);
//        float patch = Math.abs(faceInfo.headPose[1]);
//        float roll = Math.abs(faceInfo.headPose[2]);
//        // 人脸的三个角度大于20不进行识别
//        if (raw > 20 || patch > 20 || roll > 20) {
//            return;
//        }
//        identityStatus = IDENTITYING;
//        int[] argb = imageFrame.getArgb();
//        int rows = imageFrame.getHeight();
//        int cols = imageFrame.getWidth();
//        int[] landmarks = faceInfo.landmarks;
//        final IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, mGroupId);
//        if (identifyRet.getScore() < 80) {
//            Observable.timer(3, TimeUnit.SECONDS).observeOn(Schedulers.from(es))
//                    .subscribe((l) -> {
//                        identityStatus = IDENTITY_IDLE;
//                    });
//            handler.post(() -> {
//                listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "系统没有找到相关人脸信息");
//            });
//            return;
//        } else {
//            Observable.timer(5, TimeUnit.SECONDS).observeOn(Schedulers.from(es))
//                    .subscribe((l) -> {
//                        identityStatus = IDENTITY_IDLE;
//                    });
//            final User user = FaceApi.getInstance().getUserInfo(mGroupId, identifyRet.getUserId());
//            if (user == null) {
//                handler.post(() -> {
//                    listener.onText(action, FacePresenter.FaceResultType.Identify_failed, "系统没有找到相关人脸信息");
//                });
//            } else {
//                handler.post(() -> {
//                    listener.onBitmap(action, FacePresenter.FaceResultType.Identify_success, global_bitmap);
//                    listener.onBitmap(action, FacePresenter.FaceResultType.headphotoIR, headphotoIR);
//                    listener.onText(action, FacePresenter.FaceResultType.Identify_success, String.valueOf((int) identifyRet.getScore()));
//                    listener.onUser(action, FacePresenter.FaceResultType.Identify_success, user);
//                });
//            }
//        }
//    }
//
//
//    private Paint paint = new Paint();
//
//    {
//        paint.setColor(Color.YELLOW);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setTextSize(30);
//        paint.setStrokeWidth(5);
//    }
//
//    RectF rectF = new RectF();
//
//    private void showFrame(LivenessModel model) {
//        if (textureView == null){
//            return;
//        }
//        Canvas canvas = textureView.lockCanvas();
//        if (canvas == null) {
//            textureView.unlockCanvasAndPost(canvas);
//            return;
//        }
//        if (model == null) {
//            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
////            Log.e("canvasClear", "canvas by model is null");
//            OutputControlPresenter.getInstance().WhiteLight(false);
//            return;
//        }
//        FaceInfo[] faceInfos = model.getTrackFaceInfo();
//        ImageFrame imageFrame = model.getImageFrame();
//        if (faceInfos == null || faceInfos.length == 0) {
//            // 清空canvas
//            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            textureView.unlockCanvasAndPost(canvas);
//            Log.e("canvasClear", "canvas by faceInfo is null");
//            return;
//        }
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//        FaceInfo faceInfo = faceInfos[0];
//        rectF.set(getFaceRectTwo(faceInfo, imageFrame));
//        // 检测图片的坐标和显示的坐标不一样，需要转换。
//        mapFromOriginalRect(rectF, faceInfo, imageFrame);
//        float yaw2 = Math.abs(faceInfo.headPose[0]);
//        float patch2 = Math.abs(faceInfo.headPose[1]);
//        float roll2 = Math.abs(faceInfo.headPose[2]);
//        if (yaw2 > 20 || patch2 > 20 || roll2 > 20) {
//            // 不符合要求，绘制黄框
//            paint.setColor(Color.YELLOW);
//            String text = "请正视屏幕";
//            float width = paint.measureText(text) + 50;
//            float x = rectF.centerX() - width / 2;
//            paint.setColor(Color.RED);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, x + 25, rectF.top - 20, paint);
//            paint.setColor(Color.YELLOW);
//        } else {
//            // 符合检测要求，绘制绿框
//            paint.setColor(Color.GREEN);
//        }
//        paint.setStyle(Paint.Style.STROKE);
//        // 绘制框
//        canvas.drawRect(rectF, paint);
//        OutputControlPresenter.getInstance().WhiteLight(true);
//        textureView.unlockCanvasAndPost(canvas);
//
//    }
//
//
//    public Rect getFaceRectTwo(FaceInfo faceInfo, ImageFrame frame) {
//        Rect rect = new Rect();
//        int[] points = new int[8];
//        faceInfo.getRectPoints(points);
//        int left = points[2];
//        int top = points[3];
//        int right = points[6];
//        int bottom = points[7];
//
//        int width = (right - left);
//        int height = (bottom - top);
//
//        left = (int) ((faceInfo.mCenter_x - width / 2));
//        top = (int) ((faceInfo.mCenter_y - height / 2));
//
//        rect.top = top < 0 ? 0 : top;
//        rect.left = left < 0 ? 0 : left;
//        rect.right = (int) ((faceInfo.mCenter_x + width / 2));
//        rect.bottom = (int) ((faceInfo.mCenter_y + height / 2));
//        return rect;
//    }
//
//    public void mapFromOriginalRect(RectF rectF, FaceInfo faceInfo, ImageFrame imageFrame) {
//        int selfWidth = mPreviewView.getPreviewWidth();
//        int selfHeight = mPreviewView.getPreviewHeight();
//        Matrix matrix = new Matrix();
//        if (selfWidth * imageFrame.getHeight() > selfHeight * imageFrame.getWidth()) {
//            int targetHeight = imageFrame.getHeight() * selfWidth / imageFrame.getWidth();
//            int delta = (targetHeight - selfHeight) / 2;
//            float ratio = 1.0f * selfWidth / imageFrame.getWidth();
//            matrix.postScale(ratio, ratio);
//            matrix.postTranslate(0, -delta);
//        } else {
//            int targetWith = imageFrame.getWidth() * selfHeight / imageFrame.getHeight();
//            int delta = (targetWith - selfWidth) / 2;
//            float ratio = 1.0f * selfHeight / imageFrame.getHeight();
//            matrix.postScale(ratio, ratio);
//            matrix.postTranslate(-delta, 0);
//        }
//        matrix.mapRect(rectF);
//        if (mirror) { // 根据镜像调整
//            float left = selfWidth - rectF.right;
//            float right = left + rectF.width();
//            rectF.left = left;
//            rectF.right = right;
//        }
//    }
//
//
//    private void livnessTypeTip() {
//        PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_LIVENSS);
//    }
//}
