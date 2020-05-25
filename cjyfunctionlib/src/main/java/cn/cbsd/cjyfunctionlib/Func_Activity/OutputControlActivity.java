package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import cn.cbsd.cjyfunctionlib.Func_OutputControl.ControlHelper.Door;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.module.IOutputControl;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.presenter.OutputControlPresenter;
import cn.cbsd.cjyfunctionlib.Func_OutputControl.view.IOutputControlView;

import cn.cbsd.cjyfunctionlib.R;

public class OutputControlActivity extends Activity implements IOutputControlView, View.OnClickListener {

    OutputControlPresenter ocp = OutputControlPresenter.getInstance();

    Button btn_12V;

    Button btn_beep;

    Button btn_ElectricLock;

    Button btn_redlight;

    Button btn_greenlight;

    Button btn_light;

    TextView tv_DoorState;

    TextView tv_humtem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_activity);
        btn_12V = (Button) findViewById(R.id.btn_12V);
        btn_12V.setOnClickListener(this);
        btn_beep = (Button) findViewById(R.id.btn_beep);
        btn_beep.setOnClickListener(this);
        btn_ElectricLock = (Button) findViewById(R.id.btn_ElectricLock);
        btn_ElectricLock.setOnClickListener(this);
        btn_redlight = (Button) findViewById(R.id.btn_redlight);
        btn_redlight.setOnClickListener(this);
        btn_greenlight = (Button) findViewById(R.id.btn_greenlight);
        btn_greenlight.setOnClickListener(this);
        btn_light = (Button) findViewById(R.id.btn_light);
        btn_light.setOnClickListener(this);
        tv_DoorState = (TextView) findViewById(R.id.tv_DoorState);
        tv_humtem = (TextView) findViewById(R.id.tv_humtem);
        ocp.Open();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ocp.SwitchPresenterSetView(this);
        ocp.readHum(5, true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        ocp.readHum(5, false);
        ocp.SwitchPresenterSetView(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ocp.Close();
    }

    boolean Alarm_12V, ElectricLock, WhiteLight = false;

    @Override
    public void onClick(View view) {
        int vid = view.getId();
        if (vid == R.id.btn_12V){
            Alarm_12V = !Alarm_12V;
            ocp.on12V_Alarm(Alarm_12V);
        }else if (vid == R.id.btn_beep){
            ocp.buzz(IOutputControl.Hex.H0);

        }else if (vid == R.id.btn_ElectricLock){
            ElectricLock = !ElectricLock;
            ocp.onElectricLock(IOutputControl.Hex.H0, ElectricLock);
        }else if (vid == R.id.btn_redlight){
            ocp.redLight();

        }else if (vid == R.id.btn_greenlight){
            ocp.greenLight();

        }else if (vid == R.id.btn_light){
            WhiteLight = !WhiteLight;
            ocp.WhiteLight(WhiteLight);
        }
    }



    @Override
    public void onDoorState(Door.DoorState state) {
        if (state.equals(Door.DoorState.State_Open)) {
            tv_DoorState.setText("门开");
        } else {
            tv_DoorState.setText("门关");
        }

    }

    @Override
    public void onTemHum(int temperature, int humidity,String THSwitchValue) {
        tv_humtem.setText("温度：" + temperature + "℃  湿度：" + humidity + "%");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
