package com.cbsd.mvphelper.mvplibrary.mvpforView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cbsd.mvphelper.mvplibrary.Tools.ActivityCollector;
import com.cbsd.mvphelper.mvplibrary.Tools.KnifeUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;


public abstract class MVPBaseActivity<P extends IPresent> extends RxAppCompatActivity implements IView<P> {

    private RxPermissions rxPermissions;

    private P p;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.getInstance().addActivity(this);
        if (getLayoutId() > 0) {
            setContentView(getLayoutId());

            bindUI(null);
            if (useEventBus()) {
                EventBus.getDefault().register(this);
            }
            bindEvent();
        }
        initData(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getP().onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getP().onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getP().onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getP().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getP().onStop();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (useEventBus()) {
            EventBus.getDefault().unregister(this);
        }
        if (getP() != null) {
            getP().detachV();
        }
        getP().onDestroy();
        ActivityCollector.getInstance().removeActivity(this);
    }

    @Override
    public void bindUI(View rootView) {
        KnifeUtils.bind(this);
    }

    @Override
    public void bindEvent() {
        getP().onCreate();
    }


    @Override
    public boolean useEventBus() {
        return true;
    }

    protected RxPermissions getRxPermissions() {
        rxPermissions = new RxPermissions(this);
        return rxPermissions;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    protected P getP() {
        if (p == null) {
            p = newP();
            if (p != null) {
                p.attachV(this);
            }
        }
        return p;
    }

}
