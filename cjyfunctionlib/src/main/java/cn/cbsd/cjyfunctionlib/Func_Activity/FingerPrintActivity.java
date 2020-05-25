package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import cn.cbsd.cjyfunctionlib.Func_FingerPrint.presenter.FingerPrintPresenter;
import cn.cbsd.cjyfunctionlib.Func_FingerPrint.view.IFingerPrintView;
import cn.cbsd.cjyfunctionlib.R;

public class FingerPrintActivity extends Activity implements IFingerPrintView {

    Button btnOpenDevice;

    Button btnEnroll;

    Button btnIdentify;

    Button btnGetEmptyID;

    Button btnRemoveTemplate;

    Button btnGetTemplate;

    Button btnCloseDevice;

    Button btnCancel;

    Button btnVerify;

    Button btnGetEnrollCount;

    Button btnCaptureImage;

    Button btnRemoveAll;

    Button btnSetTemplate;

    EditText m_editUserID;

    TextView tv_status;

    ImageView iv_ImageViewer;

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    List<Button> buttonList = new ArrayList<Button>();

    SharedPreferences sharedPreferences ;  //私有数据


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_activity);
        btnOpenDevice = (Button) findViewById(R.id.btnOpenDevice);
        btnOpenDevice.setOnClickListener(mOnClickListener);
        buttonList.add(btnOpenDevice);

        btnEnroll = (Button) findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(mOnClickListener);
        buttonList.add(btnEnroll);

        btnIdentify = (Button) findViewById(R.id.btnIdentify);
        btnIdentify.setOnClickListener(mOnClickListener);
        buttonList.add(btnIdentify);

        btnGetEmptyID = (Button) findViewById(R.id.btnGetEmptyID);
        btnGetEmptyID.setOnClickListener(mOnClickListener);
        buttonList.add(btnGetEmptyID);

        btnRemoveTemplate = (Button) findViewById(R.id.btnRemoveTemplate);
        btnRemoveTemplate.setOnClickListener(mOnClickListener);
        buttonList.add(btnRemoveTemplate);

        btnGetTemplate = (Button) findViewById(R.id.btnGetTemplate);
        btnGetTemplate.setOnClickListener(mOnClickListener);
        buttonList.add(btnGetTemplate);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(mOnClickListener);
        buttonList.add(btnCancel);

        btnCloseDevice = (Button) findViewById(R.id.btnCloseDevice);
        btnCloseDevice.setOnClickListener(mOnClickListener);
        buttonList.add(btnCloseDevice);

        btnVerify = (Button) findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(mOnClickListener);
        buttonList.add(btnVerify);

        btnGetEnrollCount = (Button) findViewById(R.id.btnGetEnrollCount);
        btnGetEnrollCount.setOnClickListener(mOnClickListener);
        buttonList.add(btnGetEnrollCount);

        btnCaptureImage = (Button) findViewById(R.id.btnCaptureImage);
        btnCaptureImage.setOnClickListener(mOnClickListener);
        buttonList.add(btnCaptureImage);

        btnRemoveAll = (Button) findViewById(R.id.btnRemoveAll);
        btnRemoveAll.setOnClickListener(mOnClickListener);
        buttonList.add(btnRemoveAll);

        btnSetTemplate = (Button) findViewById(R.id.btnSetTemplate);
        btnSetTemplate.setOnClickListener(mOnClickListener);
        buttonList.add(btnSetTemplate);

        tv_status = (TextView) findViewById(R.id.txtStatus);
        iv_ImageViewer = (ImageView) findViewById(R.id.ivImageViewer);
        m_editUserID = (EditText) findViewById(R.id.editUserID);

        BtnDisable(btnOpenDevice);

        fpp.fpInit(this);

        sharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fpp.FingerPrintPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fpp.FingerPrintPresenterSetView(null);
    }

    View.OnClickListener mOnClickListener = view -> {
        int vid = view.getId();
        if(vid == R.id.btnOpenDevice){
            fpp.fpOpen();
        }else if(vid == R.id.btnEnroll){
            fpp.fpEnroll(m_editUserID.getText().toString());
        }else if(vid == R.id.btnIdentify){
            fpp.fpIdentify();
        }else if(vid == R.id.btnGetEmptyID){
            m_editUserID.setText(String.valueOf(fpp.fpGetEmptyID()));
        }else if(vid == R.id.btnRemoveTemplate){
            fpp.fpRemoveTmpl(m_editUserID.getText().toString());
        }else if(vid == R.id.btnGetTemplate){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fp_temp",   fpp.fpUpTemlate(m_editUserID.getText().toString()));
            editor.commit();
        }else if(vid == R.id.btnCancel){
            fpp.fpCancel(true);
        }else if(vid == R.id.btnCloseDevice){
            fpp.fpClose();
        }else if(vid == R.id.btnVerify){
            fpp.fpVerify(m_editUserID.getText().toString());
        }else if(vid == R.id.btnGetEnrollCount){
            fpp.fpGetEnrollCount();
        }else if(vid == R.id.btnCaptureImage){
            fpp.fpCaptureImg();
        }else if(vid == R.id.btnRemoveAll){
            fpp.fpRemoveAll();
        }else if(vid == R.id.btnSetTemplate){
            fpp.fpDownTemplate(m_editUserID.getText().toString(), sharedPreferences.getString("fp_temp",""));
        }
    };






    private void BtnDisable(Button clickable) {

        for (Button button:buttonList){
            button.setEnabled(false);
        }
        if(clickable!=null){
            clickable.setEnabled(true);
        }
    }

    private void BtnEnable() {
        for (Button button:buttonList){
            button.setEnabled(true);
        }
        btnOpenDevice.setEnabled(false);
    }

    @Override
    public void onSetImg(Bitmap bmp) {
        iv_ImageViewer.setImageBitmap(bmp);

    }

    @Override
    public void onText(String msg) {
        tv_status.setText(msg);

    }

    @Override
    public void onFpSucc(String msg) {

        if(msg.equals("fpOpen")||msg.equals("fpCancel")){
            BtnEnable();
        }else if(msg.equals("fpClose")) {
            BtnDisable(btnOpenDevice);
        }else if(msg.equals("fpEnroll")||msg.equals("fpVerify")||msg.equals("fpIdentify")){
            BtnDisable(btnCancel);
        }else if(msg.startsWith("TAG")){
//            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        }

    }
}
