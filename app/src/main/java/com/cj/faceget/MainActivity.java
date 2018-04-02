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
        String name = ((EditText) findViewById(R.id.et_name)).getText().toString();
        String department = ((EditText) findViewById(R.id.et_department)).getText().toString();
        String occupation = ((EditText) findViewById(R.id.et_occupation)).getText().toString();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(department) && !TextUtils.isEmpty(occupation)) {
            FaceGetActivity.startFaceGetActivity(MainActivity.this, name, department, occupation);
        }
    }
}
