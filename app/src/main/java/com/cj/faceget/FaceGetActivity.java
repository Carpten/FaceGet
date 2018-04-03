package com.cj.faceget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class FaceGetActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView mCameraView;
    private ImageView mImageView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_get);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCameraView = findViewById(R.id.camera_view);
        mImageView = findViewById(R.id.image_view);
        mTextView = findViewById(R.id.text_view);
        mImageView.setOnClickListener(this);
        clearFile();
    }

    private void clearFile() {
        File file = getExternalCacheDir();
        if (file != null) {
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void startFaceGetActivity(Context context, String id, String name, String department) {
        Intent intent = new Intent(context, FaceGetActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("department", department);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(mTextView.getText())) {
            UploadActivity.startUploadActivity(FaceGetActivity.this, 100, getIntent().getStringExtra("id"),
                    getIntent().getStringExtra("name"), getIntent().getStringExtra("department"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getExternalCacheDir() != null && getExternalCacheDir().listFiles() != null
                && getExternalCacheDir().listFiles().length != 0) {
            mTextView.setText(String.valueOf(getExternalCacheDir().listFiles().length));
        } else {
            mTextView.setText("");
        }

        if (resultCode == Activity.RESULT_OK) {
            finish();
        }
    }
}
