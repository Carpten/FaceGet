package com.cj.faceget;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cj.faceget.model.ResponseBean;
import com.cj.faceget.net.RetrofitClient;

import org.reactivestreams.Publisher;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {

    private String id;
    private String name;
    private String department;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void faceGet(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //表示未授权时
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            startFaceGet();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
            startFaceGet();
        } else { //拒绝权限申请
            Toast.makeText(this, "你拒绝他干嘛？", Toast.LENGTH_SHORT).show();
        }
    }


    private void startFaceGet() {
        final String name = ((EditText) findViewById(R.id.et_name)).getText().toString();
        final String department = ((EditText) findViewById(R.id.et_department)).getText().toString();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(department)) {
            final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                    .content("请等待").show();
            RetrofitClient.get().getId().observeOn(AndroidSchedulers.mainThread())
                    .doFinally(new Action() {
                        @Override
                        public void run() throws Exception {
                            dialog.dismiss();
                        }
                    })
                    .subscribe(new Consumer<ResponseBean>() {
                        @Override
                        public void accept(ResponseBean responseBean) throws Exception {
                            if (responseBean.isResult()) {
                                MainActivity.this.id = responseBean.getMsg();
                                MainActivity.this.name = name;
                                MainActivity.this.department = department;
                                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                                takeVideoIntent.putExtra(EXTRA_OUTPUT, getExternalCacheDir());
                                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(takeVideoIntent, 1);
                                }
//                                FaceGetActivity.startFaceGetActivity(MainActivity.this
//                                        , responseBean.getMsg(), name, department);
                            } else {
                                throw new Exception(responseBean.getMsg());
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(MainActivity.this, "请求失败："
                                    + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
//            UploadActivity.startUploadActivity(MainActivity.this, getRealPathFromURI(intent.getData())
//                    , getIntent().getStringExtra("id"), getIntent().getStringExtra(
//                            "name"), getIntent().getStringExtra("department"));
            final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this).cancelable(false)
                    .content("请等待").show();
            RetrofitClient.get().updateVideo(id, new File(getRealPathFromURI(intent.getData())))
                    .flatMap(new Function<ResponseBean, Publisher<ResponseBean>>() {
                        @Override
                        public Publisher<ResponseBean> apply(ResponseBean b) throws Exception {
                            if (!b.isResult()) throw new Exception(b.getMsg());
                            return RetrofitClient.get().newMsg(id, name, department);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(new Action() {
                        @Override
                        public void run() throws Exception {
                            dialog.dismiss();
                        }
                    })
                    .subscribe(new Consumer<ResponseBean>() {
                        @Override
                        public void accept(ResponseBean responseBean) throws Exception {
                            if (responseBean.isResult()) {
                                Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, responseBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(MainActivity.this, "e:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}
