package cn.cbdi.hunaninstrument;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.cbsd.YuvUtils;
import cn.cbsd.cjyfunctionlib.Tools.YuvTools;


/**
 * @author Created by WZW on 2021-02-19 16:03.
 * @description
 */
public class ImageActivity extends AppCompatActivity {

    Bitmap bmp_smile;

    ImageView iv_smile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        bmp_smile = BitmapFactory.decodeResource(getResources(), R.drawable.smile);
        iv_smile = (ImageView) findViewById(R.id.iv_smile);
        iv_smile.setImageBitmap(bmp_smile);
        iv_smile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int desWidth = bmp_smile.getWidth();
                int desHeight = bmp_smile.getHeight();
                byte[] src_i420 = new byte[desWidth * desHeight * 3 / 2];
                byte[] clip_i420 = new byte[desWidth * desHeight * 3 / 2];
                byte[] scale_i420 = new byte[desWidth * 2 * desHeight * 2 * 3 / 2];
                byte[] result = new byte[desWidth * 2 * desHeight * 2 * 3 / 2];
                byte[] src_nv21 = YuvTools.getNV21(desWidth, desHeight, bmp_smile);

                Log.e("src_nv21.length", String.valueOf(src_nv21.length));
                Log.e("width*height", String.valueOf(desWidth * desHeight));

                YuvUtils.yuvNV21ToI420(src_nv21, desWidth, desHeight, src_i420);

//                YuvUtils.yuvCropI420(src_i420, desWidth, desHeight, clip_i420, (desWidth / 2), (desHeight / 2), 0, 0);
                YuvUtils.yuvScaleI420(src_i420, desWidth, desHeight, scale_i420, desWidth * 2, desHeight * 2, 0);
                YuvUtils.yuvI420ToNV21(scale_i420, desWidth * 2, desHeight * 2, result);
                Bitmap bitmap = YuvTools.yuv2Bitmap(result, desWidth * 2, desHeight * 2);
                iv_smile.setImageBitmap(bitmap);
            }
        });
    }
}
