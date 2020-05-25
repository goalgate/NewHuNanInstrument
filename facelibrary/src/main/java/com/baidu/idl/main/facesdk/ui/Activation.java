/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.main.facesdk.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.R;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.listener.SdkInitListener;
import com.baidu.idl.main.facesdk.manager.FaceSDKManager;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.baidu.idl.main.facesdk.utils.ToastUtils;

public class Activation {

    private Context context;
    public Button activateBtn;
    private Button backBtn;
    private TextView deviceIdTv;
    public EditText keyEt;
    private String device = "";
    private Dialog activationDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ActivationCallback activationCallback;
    private int lastKeyLen = 0;
    private Button btOffLineActive;

    private FaceAuth faceAuth;

    private boolean success = false;


    public Activation(Context context) {
        this.context = context;
        faceAuth = new FaceAuth();
    }

    public void setActivationCallback(ActivationCallback callback) {
        this.activationCallback = callback;
    }

    public void show() {
        PreferencesUtil.initPrefs(context.getApplicationContext());
        activationDialog = new Dialog(context);
        activationDialog.setTitle("设备激活");
        activationDialog.setContentView(initView());
        activationDialog.setCancelable(false);
        activationDialog.show();
        addLisenter();
    }

    private View initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_item_view, null);
        deviceIdTv = view.findViewById(R.id.tv_device);
        keyEt = view.findViewById(R.id.et_key);
        activateBtn = view.findViewById(R.id.bt_on_line_active);
        btOffLineActive = view.findViewById(R.id.bt_off_line_active);
        backBtn = view.findViewById(R.id.bt_back);
        keyEt.setTransformationMethod(new AllCapTransformationMethod(true));
        device = faceAuth.getDeviceId(context);
        deviceIdTv.setText(device);
        keyEt.setText(PreferencesUtil.getString("activate_online_key", ""));
        return view;
    }

    private void addLisenter() {
        keyEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 19) {
                    keyEt.setText(s.toString().substring(0, 19));
                    keyEt.setSelection(keyEt.getText().length());
                    lastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < lastKeyLen) {
                    lastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (keyEt.getSelectionStart() < text.length()) {
                    return;
                }
                if (text.length() == 4 || text.length() == 9 || text.length() == 14) {
                    keyEt.setText(text + "-");
                    keyEt.setSelection(keyEt.getText().length());
                }

                lastKeyLen = s.length();
            }
        });
        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String licenseKey = keyEt.getText().toString().trim().toUpperCase();
                if (TextUtils.isEmpty(licenseKey)) {
                    Toast.makeText(context, "序列号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                faceAuth.initLicenseOnLine(context, licenseKey, new Callback() {
                    @Override
                    public void onResponse(final int code, final String response) {
                        ToastUtils.toast(context, code + response);
                        if (code == 0) {
                            FaceSDKManager.getInstance().initModel(context, new SdkInitListener() {
                                @Override
                                public void initStart() {

                                }

                                @Override
                                public void initLicenseSuccess() {

                                }

                                @Override
                                public void initLicenseFail(int errorCode, String msg) {
                                    activationCallback.callback(errorCode,msg,licenseKey);
                                }

                                @Override
                                public void initModelSuccess() {
                                    activationCallback.callback(0,"模型加载成功",licenseKey);
                                }

                                @Override
                                public void initModelFail(int errorCode, String msg) {
                                    activationCallback.callback(errorCode,msg,licenseKey);

                                }
                            });

                        }
                    }
                });
            }
        });

        btOffLineActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activationCallback.callback(222,"本应用暂时不支持离线激活",null);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activationDialog != null) {
                    activationDialog.dismiss();
                }
            }
        });

    }

    public void dismissActivationDialog() {
        if (activationDialog != null) {
            activationDialog.dismiss();
        }
    }




    private int dip2px(int dip) {
        Resources resources = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics());

        return px;
    }

    public interface ActivationCallback {

        public void callback(int code, String response, String licenseKey);
    }

    public class AllCapTransformationMethod extends ReplacementTransformationMethod {

        private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private boolean allUpper = false;

        public AllCapTransformationMethod(boolean needUpper) {
            this.allUpper = needUpper;
        }

        @Override
        protected char[] getOriginal() {
            if (allUpper) {
                return lower;
            } else {
                return upper;
            }
        }

        @Override
        protected char[] getReplacement() {
            if (allUpper) {
                return upper;
            } else {
                return lower;
            }
        }
    }

}
