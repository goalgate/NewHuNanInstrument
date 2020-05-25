package cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.cundong.utils.PatchUtils;

import java.io.File;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant.MANUAL_PATH;
import static cn.cbsd.cjyfunctionlib.Func_CJYExtension.Update.UpdateConstant.SIGN_MD5;

public class Incremental_Update {

    private static SharedPreferences config;

    private static Handler handler = new Handler();

    public static void CopySourceFile(Context context) {
        config = context.getSharedPreferences("USER", Context.MODE_PRIVATE);
        if (config.getBoolean("CopySourceFileVer1", true)) {
            Observable.create((emitter) -> {
                emitter.onNext(ApkUtils.copyfile(
                        new File(ApkUtils.getSourceApkPath(context, UpdateConstant.TEST_PACKAGENAME)),
                        new File(UpdateConstant.ORIGINAL_APK_PATH),
                        true));
            })
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> {
                        Boolean status = (boolean) l;
                        if (status) {
                            Toast.makeText(context, "源文件复制成功", Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = config.edit();
                            editor.putBoolean("CopySourceFileVer1", false);
                            editor.commit();
                        } else {
                            Toast.makeText(context, "源文件复制失败", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


    public static void Update(Context context) {
        File manual_patch = new File(MANUAL_PATH);
        if (manual_patch.exists()) {
            if (SignUtils.getSignMd5Str(context).equals(SIGN_MD5)) {
                Toast.makeText(context, "正在合成APK，请稍候", Toast.LENGTH_LONG).show();
                new Thread(() -> {
                    int patchResult = PatchUtils.patch(UpdateConstant.ORIGINAL_APK_PATH, UpdateConstant.NEW_APK_PATH, MANUAL_PATH);
//                    manual_patch.delete();
                    if (patchResult == 0) {
                        handler.post(() -> ApkUtils.installApk(context, UpdateConstant.NEW_APK_PATH));
                    } else {
                        Toast.makeText(context, "apk合成失败", Toast.LENGTH_LONG).show();
                    }

                }).start();
            } else {
                Toast.makeText(context, "旧有的MD5值与设定MD5值不一", Toast.LENGTH_LONG).show();
            }
        }
    }
}


