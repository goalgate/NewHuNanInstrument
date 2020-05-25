package com.baidu.idl.main.facesdk.manager;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;


import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.api.FaceApi;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.Group;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.model.User;
import com.baidu.idl.main.facesdk.utils.FileUtils;
import com.baidu.idl.main.facesdk.utils.LogUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户管理
 * Created by v_liujialu01 on 2018/12/14.
 */

public class UserInfoManager {
    private static final String TAG = UserInfoManager.class.getSimpleName();
    private ExecutorService mExecutorService = null;

    // 私有构造
    private UserInfoManager() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    private static class HolderClass {
        private static final UserInfoManager instance = new UserInfoManager();
    }

    public static UserInfoManager getInstance() {
        return HolderClass.instance;
    }

    /**
     * 释放
     */
    public void release() {
        LogUtils.i(TAG, "release");
    }

    /**
     * 获取组列表信息
     */
    public void getUserGroupInfo(final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                // 如果关键字为null，则全局查找
                if (groupId == null) {
                    listener.userGroupQuerySuccess(FaceApi.getInstance().getGroupList(0, 100));
                } else {
                    // 如果关键字不为null，则按关键字查找
                    if (TextUtils.isEmpty(groupId)) {
                        listener.userGroupQueryFailure("请输入关键字");
                        return;
                    }
                    listener.userGroupQuerySuccess(FaceApi.getInstance().getGroupListByGroupId(groupId));
                }
            }
        });
    }

    /**
     * 删除用户组列表信息
     */
    public void deleteUserGroupListInfo(final List<Group> list, final UserInfoListener listener, final int selectCount) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (list == null) {
                    listener.userGroupDeleteFailure("参数异常");
                    return;
                }

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isChecked()) {
                        FaceApi.getInstance().groupDelete(list.get(i).getGroupId());
                    }
                }
                listener.userGroupDeleteSuccess();
            }
        });
    }

    /**
     * 删除用户组列表信息
     */
    public void deleteUserGroupListInfo(final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (groupId == null) {
                    listener.userGroupDeleteFailure("参数异常");
                    return;
                }

                FaceApi.getInstance().groupDelete(groupId);
                listener.userGroupDeleteSuccess();
            }
        });
    }

    /**
     * 获取用户列表信息
     */
    public void getUserListInfoByGroupId(final String userName, final String groupId,
                                         final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (groupId == null || "".equals(groupId)) {
                    listener.userListQueryFailure("groupId为空");
                    return;
                }

                // 如果关键字为null，则全局查找
                if (userName == null) {
                    listener.userListQuerySuccess(FaceApi.getInstance().getUserList(groupId));
                } else {
                    // 如果关键字不为null，则按关键字查找
                    if (TextUtils.isEmpty(userName)) {
                        listener.userListQueryFailure("请输入关键字");
                        return;
                    }
                    listener.userListQuerySuccess(FaceApi.getInstance().getUserListByUserName(groupId, userName));
                }
            }
        });
    }

    /**
     * 删除用户信息
     */
    public void deleteUserInfo(final String userId, final String groupId, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                boolean success = FaceApi.getInstance().userDelete(userId, groupId);
                if (success) {
                    listener.userDeleteSuccess();
                } else {
                    listener.userDeleteFailure("删除用户失败");
                }
            }
        });
    }

    /**
     * 删除用户列表信息
     */
    public void deleteUserListInfo(final List<User> list, final UserInfoListener listener, final int selectCount) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }

                if (list == null) {
                    listener.userListDeleteFailure("参数异常");
                    return;
                }

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isChecked()) {
                        FaceApi.getInstance().userDelete(list.get(i).getUserId(), list.get(i).getGroupId());
                    }
                }
                listener.userListDeleteSuccess();
            }
        });
    }

    /**
     * 更换图片
     */
    public void updateImageNoSafe(final Bitmap bitmap, final String groupId, final String userName,
                            final String imageName, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(listener == null) {
                    return;
                }

                if (bitmap == null || imageName == null) {
                    listener.updateImageFailure("参数异常");
                    return;
                }

                byte[] bytes = new byte[512];
                float ret = -1;
                // 检测人脸，提取人脸特征值
//                ret = FaceApi.getInstance().getFeature(bitmap, bytes,
//                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
                ret = syncFeature(bitmap, bytes);
                if (ret == -1) {
                    listener.updateImageFailure("未检测到人脸，可能原因：人脸太小");
                    Log.e("userInfoManager","未检测到人脸，可能原因：人脸太小");
                } else if (ret == 128) {
                    // 添加用户信息到数据库
                    boolean update = FaceApi.getInstance().userUpdate(groupId, userName, imageName, bytes);
                    if (update) {
                        listener.updateImageSuccess(bitmap);
                        Log.e("userInfoManager","更新数据库成功");


//                        // 保存图片到新目录中
//                        File facePicDir = FileUtils.getBatchImportSuccessDirectory();
//                        if (facePicDir != null) {
//                            File savePicPath = new File(facePicDir, imageName);
//                            if (FileUtils.saveBitmap(savePicPath, bitmap)) {
//                            } else {
//                                listener.updateImageFailure("图片保存失败");
//                                Log.e("userInfoManager","图片保存失败");
//
//                            }
//                        }
                    } else {
                        listener.updateImageFailure("更新数据库失败");
                        Log.e("userInfoManager","更新数据库失败");

                    }
                } else {
                    listener.updateImageFailure("未检测到人脸");
                    Log.e("userInfoManager","未检测到人脸");

                }
            }
        });
    }


    public void updateImage(final Bitmap bitmap, final String groupId, final String userName,
                            final String imageName, final UserInfoListener listener) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if(listener == null) {
                    return;
                }

                if (bitmap == null || imageName == null) {
                    listener.updateImageFailure("参数异常");
                    return;
                }

                byte[] bytes = new byte[512];
                float ret = -1;
                // 检测人脸，提取人脸特征值
//                ret = FaceApi.getInstance().getFeature(bitmap, bytes,
//                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
                ret = syncFeature(bitmap, bytes);
                if (ret == -1) {
                    listener.updateImageFailure("未检测到人脸，可能原因：人脸太小");
                    Log.e("userInfoManager","未检测到人脸，可能原因：人脸太小");
                } else if (ret == 128) {
                    // 添加用户信息到数据库
                    boolean update = FaceApi.getInstance().userUpdate(groupId, userName, imageName, bytes);
                    if (update) {
                        // 保存图片到新目录中
                        File facePicDir = FileUtils.getBatchImportSuccessDirectory();
                        if (facePicDir != null) {
                            File savePicPath = new File(facePicDir, imageName);
                            if (FileUtils.saveBitmap(savePicPath, bitmap)) {
                                listener.updateImageSuccess(bitmap);
                            } else {
                                listener.updateImageFailure("图片保存失败");
                                Log.e("userInfoManager","图片保存失败");

                            }
                        }
                    } else {
                        listener.updateImageFailure("更新数据库失败");
                        Log.e("userInfoManager","更新数据库失败");

                    }
                } else {
                    listener.updateImageFailure("未检测到人脸");
                    Log.e("userInfoManager","未检测到人脸");

                }
            }
        });
    }

    private float syncFeature(final Bitmap bitmap, final byte[] feature) {
        float ret = -1;
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

    public static class UserInfoListener {
        public void userGroupQuerySuccess(List<Group> listGroupInfo) {
            // 用户组列表查询成功
        }

        public void userGroupQueryFailure(String message) {
            // 用户组列表查询失败
        }

        public void userListQuerySuccess(List<User> listUserInfo) {
            // 用户列表查询成功
        }

        public void userListQueryFailure(String message) {
            // 用户列表查询失败
        }

        public void userGroupDeleteSuccess() {
            // 用户组列表删除成功
        }

        public void userGroupDeleteFailure(String message) {
            // 用户组列表删除失败
        }

        public void userListDeleteSuccess() {
            // 用户列表删除成功
        }

        public void userListDeleteFailure(String message) {
            // 用户列表删除失败
        }

        public void updateImageSuccess(Bitmap bitmap) {
            // 更新图片成功
        }

        public void updateImageFailure(String message) {
            // 更新图片失败
        }

        public void userDeleteSuccess() {
            // 用户删除成功
        }

        public void userDeleteFailure(String message) {
            // 用户删除失败
        }
    }
}
