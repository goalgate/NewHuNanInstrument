package cn.cbsd.cjyfunctionlib.Func_Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import cn.cbsd.cjyfunctionlib.Func_CJYExtension.Machine.CJYHelper;
import cn.cbsd.cjyfunctionlib.Func_Card.CardHelper.ICardInfo;
import cn.cbsd.cjyfunctionlib.Func_Card.presenter.IDCardPresenter;
import cn.cbsd.cjyfunctionlib.Func_Card.view.IIDCardView;
import cn.cbsd.cjyfunctionlib.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class CardActivity extends Activity implements IIDCardView {

    IDCardPresenter idp = IDCardPresenter.getInstance();

    Button btn_ICCard;

    Button btn_IDCard;

    ImageView headphoto;

    TextView tv_info;

    boolean canICRead = false;

    boolean canIDRead = false;

    private Disposable disposableTips;

    static int Success_IDCount = 0;

    static int Failed_IDCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_activity);
        tv_info = (TextView) findViewById(R.id.tv_info);
        btn_ICCard = (Button) findViewById(R.id.btn_ICCard);
        btn_IDCard = (Button) findViewById(R.id.btn_IDCard);
        headphoto = (ImageView) findViewById(R.id.iv_headphoto);
        findViewById(R.id.btn_getSam).setOnClickListener((v) -> {
            idp.readSam();
        });
        btn_ICCard.setOnClickListener((v) -> {
            canICRead = !canICRead;
            if (canICRead) {
                idp.ReadIC();
                btn_ICCard.setText(R.string.stopIC);
            } else {
                idp.StopReadIC();
                btn_ICCard.setText(R.string.startIC);
            }
        });

        btn_IDCard.setOnClickListener((view -> {
            canIDRead = !canIDRead;
            if (canIDRead) {
                idp.ReadID();
                btn_IDCard.setText(R.string.stopID);
            } else {
                idp.StopReadID();
                btn_IDCard.setText(R.string.startID);
            }
        }));


        idp.idCardOpen(this);
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(3, TimeUnit.SECONDS)
                .switchMap(charSequence -> Observable.just("等待刷卡信息"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((s) -> tv_info.setText(s));
    }


    @Override
    protected void onResume() {
        super.onResume();
        idp.IDCardPresenterSetView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        idp.IDCardPresenterSetView(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        idp.idCardClose();
        if (disposableTips != null) {
            disposableTips.dispose();
        }
    }

    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
        tv_info.setText("身份证号：" + cardInfo.cardId() + "   姓名：" + cardInfo.name());

    }

    @Override
    public void onsetCardImg(Bitmap bmp) {
        if (bmp == null) {
            tv_info.setText("警告，没有身份证照片，可能获取身份证延时不足或者没有wltlib文件夹");
            Failed_IDCount++;
        } else {
            Success_IDCount++;
            headphoto.setImageBitmap(bmp);
            headphoto.setVisibility(View.VISIBLE);
            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((l) -> headphoto.setVisibility(View.GONE));
        }
        tv_info.setText("成功刷卡: " + Success_IDCount + " 次；失败刷卡: " + Failed_IDCount + " 次");
    }

    @Override
    public void onSetText(String Msg) {
        Toast.makeText(this, Msg, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onsetICCardInfo(ICardInfo cardInfo) {
        tv_info.setText(cardInfo.getUid());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
