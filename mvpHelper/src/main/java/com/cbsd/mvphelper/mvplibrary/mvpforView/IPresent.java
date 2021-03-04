package com.cbsd.mvphelper.mvplibrary.mvpforView;

public interface IPresent<V> {

    void onCreate();

    void onStart();

    void onResume();

    void onRestart();

    void onPause();

    void onStop();

    void onDestroy();

    void attachV(V view);

    void detachV();
}
