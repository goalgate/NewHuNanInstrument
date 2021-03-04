package com.cbsd.mvphelper.mvplibrary.mvpforView;

import android.os.Bundle;
import android.view.View;

public interface IView<P>{

    void bindUI(View rootView);

    void bindEvent();

    int getLayoutId();

    void initData(Bundle savedInstanceState);

    int getOptionsMenuId();

    boolean useEventBus();

    P newP();

    void showLoading();

    void hideLoading();

}
