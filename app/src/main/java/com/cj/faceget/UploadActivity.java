
package com.cj.faceget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cj.faceget.model.ResponseBean;
import com.cj.faceget.net.RetrofitClient;

import org.reactivestreams.Publisher;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class UploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(UploadActivity.this, 2));
        recyclerView.setAdapter(new UploadAdapter(UploadActivity.this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void upload(View view) {
        if (getExternalCacheDir() != null && getExternalCacheDir().listFiles() != null
                && getExternalCacheDir().listFiles().length != 0) {
            final MaterialDialog dialog = new MaterialDialog.Builder(UploadActivity.this).cancelable(false)
                    .content("请等待").show();
            RetrofitClient.get().updateImage(getIntent().getStringExtra("id"), getExternalCacheDir().listFiles())
                    .flatMap(new Function<Boolean, Publisher<ResponseBean>>() {
                        @Override
                        public Publisher<ResponseBean> apply(Boolean b) throws Exception {
                            return RetrofitClient.get().newMsg(getIntent().getStringExtra("id"),
                                    getIntent().getStringExtra("name"), getIntent().getStringExtra("department"));
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
                                Toast.makeText(UploadActivity.this, "ok", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(UploadActivity.this, responseBean.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(UploadActivity.this, "e:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void startUploadActivity(Activity activity, String path, String id, String name, String department) {
        Intent intent = new Intent(activity, UploadActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("department", department);
        activity.startActivity(intent);
    }
}
