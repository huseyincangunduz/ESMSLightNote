package com.esenlermotionstar.lightnote;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES10;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import javax.microedition.khronos.opengles.GL10;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String IMAGE_PATH_EXTRA = "imagepath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ProgressDialog di = ESMSUtils.showProgressDialog(this,"",getString(R.string.pls_wait_loading));
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra(IMAGE_PATH_EXTRA);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
        int[] maxSize = new int[1];
        ZoomableImageView zoomableImageView = ((ZoomableImageView) (findViewById(R.id.zoomable_img)));
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);

        if (bmp.getWidth() >= 2020 || bmp.getHeight() >= 2020) {
            float scale = Math.max((float)bmp.getWidth(), (float)bmp.getHeight()) / 2020;
            bmp = Bitmap.createScaledBitmap(bmp,(int) (bmp.getWidth() / scale), (int)(bmp.getHeight()/scale),false);
        }

        zoomableImageView.setImageBitmap(bmp);
        di.dismiss();

    }

    public void gobackbtnclicked(View view) {
        finish();
    }
}
