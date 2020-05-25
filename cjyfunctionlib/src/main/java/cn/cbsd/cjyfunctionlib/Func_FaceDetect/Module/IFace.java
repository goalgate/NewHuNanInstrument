package cn.cbsd.cjyfunctionlib.Func_FaceDetect.Module;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;


import com.baidu.idl.main.facesdk.camera.AutoTexturePreviewView;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.manager.UserInfoManager;
import com.baidu.idl.main.facesdk.model.LivenessModel;
import com.baidu.idl.main.facesdk.model.User;

import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;


public interface IFace {

    void FaceInit(Context context, SdkInitListener listener);

    void CameraPreview(Context context, AutoTexturePreviewView previewView, AutoTexturePreviewView previewView1, TextureView textureView, IFaceListener listener);

    void FaceIdentify();

    void FaceIdentify_model();

    void FaceVerify(String userName, String userInfo, Bitmap bitmap);

    void FaceReg(String userName, String userInfo);

    boolean FaceRegByBase64(String userName, String userInfo, String base64);

    boolean IMG_to_IMG(Bitmap bmp1, Bitmap bmp2, boolean IDCard_HeadPhoto,boolean useThread);

    void FaceSetNoAction();

    void setIdentifyStatus(int i);

    void FaceIdentifyReady();

    void PreviewCease(CeaseListener listener);

    void useRGBCamera(boolean status);

    Bitmap getGlobalBitmap();

    void SetGroupID(String groupId);

    void FaceDelete(String userName);

    void FaceVerifyAndReg(String userName, String userInfo, Bitmap bitmap);

    void FaceUpdate(Bitmap bitmap, String userName, UserInfoManager.UserInfoListener listener);

    void FaceRegOrUpdateByFeature(String userName, String userInfo,byte[] feature,boolean Reg);

    void FaceGroupDelete(String groupId);

    User GetUser(String userName);

    interface IFaceListener {
        void onText(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, String text);

        void onBitmap(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, Bitmap bitmap);

        void onUser(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, User user);

        void onLivenessModel(FacePresenter.FaceAction action, FacePresenter.FaceResultType resultType, LivenessModel model);

    }

    interface CeaseListener {
        void CeaseCallBack();
    }


}
