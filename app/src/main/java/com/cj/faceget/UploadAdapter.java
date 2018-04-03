package com.cj.faceget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * @author: yangshuiqiang
 * Time: 2018/4/3.
 */

public class UploadAdapter extends RecyclerView.Adapter {
    private File[] mFiles;
    private Context mContext;

    public UploadAdapter(Context context) {
        readFiles(context);
        mContext = context;
    }

    private void readFiles(Context context) {
        File file = context.getExternalCacheDir();
        mFiles = file.listFiles();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_upload, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Glide.with(mContext).load(mFiles[position]).into(((VH) holder).imageView);
        ((VH) holder).imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFiles[position].delete();
                readFiles(mContext);
                notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.length;
    }

    private static class VH extends RecyclerView.ViewHolder {
        ImageView imageView;

        public VH(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
