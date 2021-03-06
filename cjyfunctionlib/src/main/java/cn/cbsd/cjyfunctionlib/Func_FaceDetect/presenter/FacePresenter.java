package cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.TextureView;


import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module.IFace;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.view.IFaceView;


public class FacePresenter {
    private IFaceView view;

    private IFace iFace ;
    private static FacePresenter instance = null;

    public enum FaceAction {
        Normal, Reg, Identify, Verify, Identify_Model, VerifyAndReg_Verify,
        VerifyWithImg,IMG_TO_IMG
    }

    public enum FaceResultType {
        Reg_success, Reg_failed,
        verify_success, verify_failed,
        Identify_success, Identify_failed,
        IMG_MATCH_IMG_False, IMG_MATCH_IMG_True, IMG_MATCH_IMG_Score,
        headphotoRGB, headphotoIR
    }

    private FacePresenter() {
    }

    public static FacePresenter getInstance() {
        if (instance == null) {
            instance = new FacePresenter();
        }
        return instance;
    }

    public void FacePresenterSetView(IFaceView view) {
        this.view = view;
    }


    public void FaceInit(IFace faceImpl,Context context, SdkInitListener listener) {
        try {
            iFace = faceImpl;
            iFace.FaceInit(context, listener);
        } catch (Exception e) {
            Log.e("FaceInit", e.toString());
        }
    }

    public void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView) {
        try {
            iFace.CameraPreview(context, previewView, previewView1, textureView, new IFace.IFaceListener() {
                @Override
                public void onText(FacePresenter.FaceAction action, FaceResultType resultType, String text) {
                    try {
                        view.onText(action, resultType, text);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmap(FacePresenter.FaceAction action, FaceResultType resultType, Bitmap bitmap) {
                    try {
                        view.onBitmap(action, resultType, bitmap);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUser(FacePresenter.FaceAction action, FaceResultType resultType, User user) {
                    try {
                        view.onUser(action, resultType, user);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLivenessModel(FaceAction action, FaceResultType resultType, LivenessModel model) {
                    try {
                        view.onLivenessModel(action, resultType, model);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("CameraPreview", e.toString());
        }
    }

    public void FaceIdentify() {
        try {
            iFace.FaceIdentify();
        } catch (Exception e) {
            Log.e("FaceIdentify", e.toString());

        }

    }

    public void FaceIdentify_model() {
        try {
            iFace.FaceIdentify_model();
        } catch (Exception e) {
            Log.e("FaceIdentify_model", e.toString());

        }
    }

    public boolean isReady(){
        try {
            return iFace.isReady();
        } catch (Exception e) {
            Log.e("isReady", e.toString());

        }
        return false;
    }

    public void FaceVerify(String userName, String userInfo, Bitmap bitmap) {
        try {
            iFace.FaceVerify(userName, userInfo, bitmap);
        } catch (Exception e) {
            Log.e("FaceVerify", e.toString());

        }
    }

    public void FaceReg(String userName, String userInfo) {
        try {
            iFace.FaceReg(userName, userInfo);
        } catch (Exception e) {
            Log.e("FaceReg", e.toString());

        }
    }

    public boolean FaceRegByBase64(String userName, String userInfo, String base64) {
        try {
            return iFace.FaceRegByBase64(userName, userInfo, base64);
        } catch (Exception e) {
            Log.e("FaceRegByBase64", e.toString());
        }
        return false;
    }

    public boolean IMG_to_IMG(Bitmap bmp1, Bitmap bmp2, boolean IDCard_HeadPhoto, boolean useThread) {
        try {
            return iFace.IMG_to_IMG(bmp1, bmp2, IDCard_HeadPhoto, useThread);
        } catch (Exception e) {
            Log.e("IMG_to_IMG", e.toString());

        }
        return false;
    }

    public void Feature_to_Feature(byte[] feature1,byte[] feature2) {
        try {
            iFace.Feature_to_Feature(feature1, feature2);
        } catch (Exception e) {
            Log.e("Feature_to_Feature", e.toString());

        }
    }

    public void FaceSetNoAction() {
        try {
            iFace.FaceSetNoAction();
        } catch (Exception e) {
            Log.e("FaceSetNoAction", e.toString());

        }
    }

    public void setIdentifyStatus(int i) {
        try {
            iFace.setIdentifyStatus(i);
        } catch (Exception e) {
            Log.e("setIdentifyStatus", e.toString());

        }
    }

    public void FaceIdentifyReady() {
        try {
            iFace.FaceIdentifyReady();
        } catch (Exception e) {
            Log.e("FaceIdentifyReady", e.toString());
        }
    }

    public void PreviewCease(IFace.CeaseListener listener) {
        try {
            iFace.PreviewCease(listener);
        } catch (Exception e) {
            Log.e("PreviewCease", e.toString());

        }
    }

    public void FaceVerifyAndReg(String userName, String userInfo, Bitmap bitmap) {
        try {
            iFace.FaceVerifyAndReg(userName, userInfo, bitmap);
        } catch (Exception e) {
            Log.e("FaceVerifyAndReg", e.toString());

        }
    }

    public void FaceVerify(Bitmap bitmap) {
        try {
            iFace.FaceVerify(bitmap);
        } catch (Exception e) {
            Log.e("FaceVerify", e.toString());

        }
    }

    public void useRGBCamera(boolean status) {
        try {
            iFace.useRGBCamera(status);
        } catch (Exception e) {
            Log.e("useRGBCamera", e.toString());

        }
    }

    public Bitmap getGlobalBitmap() {
        try {
            return iFace.getGlobalBitmap();
        } catch (Exception e) {
            Log.e("getGlobalBitmap", e.toString());
        }
        return null;
    }

    public void SetGroupID(String groupId) {
        try {
            iFace.SetGroupID(groupId);
        } catch (Exception e) {
            Log.e("SetGroupID", e.toString());
        }
    }

    public void FaceDeleteByUserName(String userName) {
        try {
            iFace.FaceDeleteByUserName(userName);
        } catch (Exception e) {
            Log.e("FaceDeleteByUserName", e.toString());

        }
    }

    public void FaceDeleteByUserId(String UserId) {
        try {
            iFace.FaceDeleteByUserId(UserId);
        } catch (Exception e) {
            Log.e("FaceDeleteByUserId", e.toString());

        }
    }

    public void FaceGroupDelete(String groupId) {
        try {
            iFace.FaceGroupDelete(groupId);
        } catch (Exception e) {
            Log.e("FaceGroupDelete", e.toString());
        }
    }



    public void FaceUpdate(Bitmap bitmap, String userName, UserInfoManager.UserInfoListener listener) {
        try {
            iFace.FaceUpdate(bitmap, userName,listener);
        } catch (Exception e) {
            Log.e("FaceUpdate", e.toString());
        }
    }

    public void FaceRegOrUpdateByFeature( String userName, String UserInfo,byte[] feature,boolean RegOrUpdate) {
        try {
            iFace.FaceRegOrUpdateByFeature( userName,UserInfo,feature,RegOrUpdate);
        } catch (Exception e) {
            Log.e("FaceRUByFeature", e.toString());
        }
    }

    public User GetUserByUserName(String userName) {
        return iFace.GetUserByUserName(userName);
    }

    public User GetUserByIdInTable(int IdInTable) {
        return iFace.GetUserByIdInTable(IdInTable);
    }

}
