package com.cj.faceget;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

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
                                FaceGetActivity.startFaceGetActivity(MainActivity.this
                                        , responseBean.getMsg(), name, department);
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


}
