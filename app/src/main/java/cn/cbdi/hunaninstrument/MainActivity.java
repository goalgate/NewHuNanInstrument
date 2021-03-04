package cn.cbdi.hunaninstrument;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.PermissionUtils;

import cn.cbsd.cjyfunctionlib.Func_Activity.CJYExtensionAndUpdateActivity;
import cn.cbsd.cjyfunctionlib.Func_Activity.CardActivity;
import cn.cbsd.cjyfunctionlib.Func_Activity.FaceInitActivity;
import cn.cbsd.cjyfunctionlib.Func_Activity.FingerPrintActivity;
import cn.cbsd.cjyfunctionlib.Func_Activity.HttpAndCollectionBoxActivity;
import cn.cbsd.cjyfunctionlib.Func_Activity.OutputControlActivity;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        PermissionUtils.requestPermissions(this, 200,
                new String[]{Manifest.permission.CAMERA},
                new PermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                    }
                });

        findViewById(R.id.func_identify).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CardActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.func_fingerPrint).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FingerPrintActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.func_face).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FaceInitActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.func_cjyextens).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CJYExtensionAndUpdateActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.func_collectionbox).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, HttpAndCollectionBoxActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.func_switch).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OutputControlActivity.class);
            startActivity(intent);
        });


    }

}
