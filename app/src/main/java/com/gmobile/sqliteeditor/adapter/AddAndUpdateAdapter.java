package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.model.bean.sqlite.ColumnField;
import com.gmobile.sqliteeditor.model.bean.sqlite.TableDataField;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/9/26.
 */
public class AddAndUpdateAdapter extends RecyclerView.Adapter<AddAndUpdateAdapter.InnerViewHolder> {


    private Context mContext;
    private List<ColumnField> mDatas;
    private boolean isAddData = true;
    private Map<String, TableDataField> mDatasMap;
    private List<String> autoColumList;
    private String mFieldData, columnTxt;
    private String mColumnValue;

    public AddAndUpdateAdapter(Context mContext, List<ColumnField> mDatas, boolean isAddData, Map<String, TableDataField> map, List<String> autoColumList) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        this.isAddData = isAddData;
        this.mDatasMap = map;
        this.autoColumList = autoColumList;
    }

    @Override
    public InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InnerViewHolder(LayoutInflater.from(mContext).inflate(R.layout.add_data_to_tab_layout, null));
    }

    @Override
    public void onBindViewHolder(InnerViewHolder holder, final int position) {
        columnTxt = mDatas.get(position).getFieldName();
        holder.title.setText(columnTxt);
        if (isAddData) {
            if (autoColumList.contains(columnTxt)) {
                holder.editText.setText(mContext.getResources().getString(R.string.sql_column_auto));
                holder.editText.setFocusable(false);
                holder.editText.setEnabled(false);
            } else if (mDatasMap == null) {

                mColumnValue = mDatas.get(position).getColumnValue();
                if (mColumnValue != null) {
                    holder.editText.setText(mColumnValue);
                    holder.editText.setSelection(mColumnValue.length());
                }
                holder.editText.setFocusable(true);
                holder.editText.setEnabled(true);
            } else {
                mColumnValue = mDatas.get(position).getColumnValue();
                if (mColumnValue != null) {
                    holder.editText.setText(mColumnValue);
                    holder.editText.setSelection(mColumnValue.length());
                } else {
                    mFieldData = mDatasMap.get(columnTxt).getFieldData();
                    if (mFieldData != null) {
                        holder.editText.setText(mFieldData);
                        holder.editText.setSelection(mFieldData.length());
                    }
                }
                holder.editText.setFocusable(true);
                holder.editText.setEnabled(true);
            }

        } else {

            if (autoColumList.contains(columnTxt)) {
                holder.editText.setText(mContext.getResources().getString(R.string.sql_column_auto));
                holder.editText.setFocusable(false);
                holder.editText.setEnabled(false);
            } else {
                mColumnValue = mDatas.get(position).getColumnValue();
                if (mColumnValue != null) {
                    holder.editText.setText(mColumnValue);
                    holder.editText.setSelection(mColumnValue.length());
                } else {
                    if (mDatasMap != null){
                        mFieldData = mDatasMap.get(columnTxt).getFieldData();
                        if (mFieldData != null) {
                            holder.editText.setText(mFieldData);
                            holder.editText.setSelection(mFieldData.length());
                        }
                    }
                }
                holder.editText.setFocusable(true);
                holder.editText.setEnabled(true);
            }
        }

        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDatas.get(position).setColumnValue(s.toString().trim());
                EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.isEditTableItem,null));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public class InnerViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public MaterialEditText editText;

        public InnerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.cloum_name);
            editText = (MaterialEditText) itemView.findViewById(R.id.cloum_name_value);
        }
    }
}
