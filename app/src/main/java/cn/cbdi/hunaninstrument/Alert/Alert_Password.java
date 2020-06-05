package cn.cbdi.hunaninstrument.Alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.baidu.idl.main.facesdk.api.FaceApi;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ToastUtils;

import cn.cbdi.hunaninstrument.AppInit;
import cn.cbdi.hunaninstrument.Bean.Employer;
import cn.cbdi.hunaninstrument.Bean.Keeper;
import cn.cbdi.hunaninstrument.UI.PasswordInputView;
import cn.cbdi.hunaninstrument.R;
import cn.cbsd.cjyfunctionlib.Func_FaceDetect.presenter.FacePresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;


public class Alert_Password {
    private Context context;

    public Alert_Password(Context context) {
        this.context = context;
    }

    private AlertView passwordAlert;
    private PasswordInputView passwordInputView;

    public void PasswordViewInit(final Callback callback) {
        ViewGroup passwordView = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.inputpassword_form, null);
        passwordInputView = (PasswordInputView) passwordView.findViewById(R.id.passwordInputView);
        passwordAlert = new AlertView("通知:", "请输入密码以进入设置界面", "取消", new String[]{"确定"}, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (passwordInputView.getText().toString().equals("665901")) {
                        callback.normal_call();
                    } else if (passwordInputView.getText().toString().equals("578412")) {
                        try {
                            AppInit.getInstance().getDaoSession().deleteAll(Keeper.class);
                            AppInit.getInstance().getDaoSession().deleteAll(Employer.class);
                            FacePresenter.getInstance().FaceSetNoAction();
                            Thread.sleep(1000);
                            FacePresenter.getInstance().FaceGroupDelete("cbsd");
                            Thread.sleep(1000);
                            FacePresenter.getInstance().FaceIdentify_model();
                            if (AppInit.getInstrumentConfig().fingerprint()) {
                                FingerPrintPresenter.getInstance().fpCancel(true);
                                Thread.sleep(1000);
                                FingerPrintPresenter.getInstance().fpRemoveAll();
                                Thread.sleep(1000);
                                FingerPrintPresenter.getInstance().fpIdentify();
                            }
                            ToastUtils.showLong("数据已被全部清除");

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtils.showLong("密码错误，请重试");
                    }
                }

            }
        });
        passwordAlert.addExtView(passwordView);
    }

    public void show() {
        passwordInputView.setText(null);
        passwordAlert.show();
    }

    public interface Callback {
        void normal_call();
    }
}
