package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.listener.BaseItemListener;
import com.gmobile.sqliteeditor.assistant.FeThumbUtils;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.bean.FileData;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by admin on 2016/11/22.
 */
public class FileAdapter extends BaseAdapter<FileData> {

    private static final int FILE_HEADER = 2;
    private FileData fileData;
    private String parentPath;
    private String currentPath;
    private boolean hasData;

    public FileData getFileData() {
        return fileData;
    }

    public FileAdapter(Context context, boolean hasTop, BaseItemListener itemListener) {
        super(context, hasTop, itemListener);
        fileData = new FileData();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        fileData.setName(file.getName());
        fileData.setPath(file.getAbsolutePath());
        currentPath = fileData.getPath();
        parentPath = fileData.getPath();
        hasData = datas.size() > 0;
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FILE_HEADER) {
            return new InnerHolder(LayoutInflater.from(context).inflate(R.layout.list_item, null));
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    //点击位置在fragment中做移位处理，绑定的BaseData在此处直接绑定好
    @Override
    public void onBindViewHolder(BaseHolder holder, int position) {
        if (topSize > 0) {
            if (position == 1) {
                bindFileHeader((InnerHolder) holder, position - 1);
            } else if (position > 1) {
                bindData((InnerHolder) holder, position - 2);
            }

            if(position > 1){
                holder.setClickPosition(position);
                if (hasData) holder.setBaseData(datas.get(position - 2));
            }else if(position == 1){
                holder.setClickPosition(0);
            } else if(position == 0){
                holder.setClickPosition(-1);
            }
        } else {
            if (position == 0) {
                bindFileHeader((InnerHolder) holder, position);
                holder.setClickPosition(0);
            } else {
                bindData((InnerHolder) holder, position - 1);
                holder.setClickPosition(position);
                if (hasData) holder.setBaseData(datas.get(position - 1));
            }
        }

        holder.itemView.setTag(holder);
    }

    private void bindFileHeader(InnerHolder holder, int position) {
        holder.appIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sqleditor_store));
        holder.fileName.setText(fileData.getName());
        holder.fileInfo.setText(fileData.getPath());
    }

    @Override
    public int getItemViewType(int position) {
        if (topSize > 0) {
            if (position == 0) {
                return TYPE_PAY;
            } else if (position == 1) {
                return FILE_HEADER;
            } else {
                return TYPE_NORMAL;
            }
        } else {
            if (position == 0) {
                return FILE_HEADER;
            } else {
                return TYPE_NORMAL;
            }
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    protected void setDataTypeForArgs(Bundle args) {
        args.putSerializable(Constant.DATA_TYPE_KEY, DataType.fileData);
    }

    @Override
    protected void bindData(InnerHolder holder, int position) {
        if (datas.get(position).isFolder()) {
            holder.appIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder_main));
        } else {
            Bitmap bitmap = FeThumbUtils.getDefaultThumb(context, FeThumbUtils.getMiMeType(datas.get(position).getName()));
            holder.appIcon.setImageBitmap(bitmap);
        }
        holder.itemMore.setVisibility(View.GONE);
        holder.fileName.setText(datas.get(position).getName());
        holder.fileInfo.setText(datas.get(position).getPath());
        holder.setBaseData(datas.get(position));
    }

    @Override
    protected void loadFinished() {
        hasData = datas.size() > 0;
        EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.controlFileReFreshLayout, new Bundle()));
    }

    @Override
    public void gotoClickPath(int position) {
        parentPath = datas.get(position).getPath();
    }

    public String getParentPath() {
        return parentPath;
    }

    public void backPath() {
        parentPath = FileUtils.getParentPath(fileData.getPath());
    }

    public void refreshTopPath(int position) {
        File file = new File(parentPath);
        fileData.setName(file.getName());
        fileData.setPath(file.getAbsolutePath());
        currentPath = fileData.getPath();

        Log.e("refreshTopPath", "==" + currentPath);
        notifyDataSetChanged();
    }
}
