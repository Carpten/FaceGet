package com.cj.faceget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class FaceGetActivity extends AppCompatActivity {

    private CameraView mCameraView;
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_get);
        mCameraView = findViewById(R.id.camera_view);
        mImageView = findViewById(R.id.image_view);
        mTextView = findViewById(R.id.text_view);
    }

    public void takePhoto(View view) {
        mCameraView.setPhotoCallback(new CameraView.PhotoCallback() {
            @Override
            public void onPhoto(final Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(bitmap);
                        String text = mTextView.getText().toString();
                        if (TextUtils.isEmpty(text))
                            text = "0";
                        mTextView.setText(String.valueOf(Integer.valueOf(text) + 1));
                    }
                });
            }
        });
    }


    public static void startFaceGetActivity(Context context, String name, String department, String occupation) {
        Intent intent = new Intent(context, FaceGetActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("department", department);
        intent.putExtra("occupation", occupation);
        context.startActivity(intent);
    }
}
