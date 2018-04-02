package com.cj.faceget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author: yangshuiqiang
 * Time: 2018/3/30.
 */

public class CameraView extends FrameLayout implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private Camera.Size mPreviewSize;


    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setBackgroundColor(Color.BLACK);
        addView(mSurfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    private Handler mCameraHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                Toast.makeText(getContext(), "相机打开失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 1) {
                requestLayout();
                mSurfaceView.setBackground(null);
            }
            return false;
        }
    });

    public synchronized void start() {
        if (mCamera == null) {
            Camera camera = null;
            try {
                stop();
                camera = Camera.open(0);
            } catch (Exception e) {
                Message message = new Message();
                message.what = 0;
                mCameraHandler.sendMessage(message);
            }
            configCamera(camera);
        }
    }


    public void configCamera(Camera camera) {
        if (mCamera == camera) {
            return;
        }
        stop();
        mCamera = camera;
        if (mCamera != null) {
            List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mPreviewSize = getPreviewSize(supportedPreviewSizes);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
            Message message = new Message();
            message.what = 1;
            mCameraHandler.sendMessage(message);
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stop() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();
            mCamera = null;
        }

    }

    private PhotoCallback mPhotoCallback;

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(final byte[] data, Camera camera) {
            if (mPhotoCallback != null) {
                try {
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, mPreviewSize.width, mPreviewSize.height, null);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, mPreviewSize.width, mPreviewSize.height), 80, stream);
                    stream.close();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                    mPhotoCallback.onPhoto(bitmap);

                    File file = new File(getContext().getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    image.compressToJpeg(new Rect(0, 0, mPreviewSize.width, mPreviewSize.height), 80, fos);
                    fos.close();
                } catch (Exception ignored) {
                }
                mPhotoCallback = null;
            }
        }
    };


    private Camera.Size getPreviewSize(List<Camera.Size> sizeList) {
        float minRatio = Float.MAX_VALUE;
        int targetSize = 640 * 480;
        int position = 0;
        for (Camera.Size size : sizeList) {
            float ratio;
            if (size.width * size.height > targetSize) {
                ratio = (float) size.width * size.height / targetSize;
            } else {
                ratio = (float) targetSize / size.width / size.height;
            }
            if (ratio < minRatio) {
                minRatio = ratio;
                position = sizeList.indexOf(size);
            }
        }
        return sizeList.get(position);
    }

    public void setPhotoCallback(PhotoCallback photoCallback) {
        if (mPhotoCallback == null) {
            mPhotoCallback = photoCallback;
        }
    }

    public interface PhotoCallback {
        void onPhoto(Bitmap bitmap);
    }
}
