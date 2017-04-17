package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by admin on 2016/9/22.
 */
public class ColumnConditionAdapter extends RecyclerView.Adapter<ColumnConditionAdapter.MyHolder> {

    private Context mContext;
    private String[] mDatas;
    private List<Integer> mPositions;
    private List<Integer> conditionsPos;

    public ColumnConditionAdapter(Context mContext, String[] data, List<Integer> conditionSelected) {
        this.mContext = mContext;
        this.mDatas = data;
        mPositions = new ArrayList<>();
        this.conditionsPos = conditionSelected;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.cloum_type_condition_item,null));
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.txt.setText(mDatas[position]);
        holder.content.setTag(position);
        if(conditionsPos.contains(position)){
            holder.checkBox.setChecked(true);
            mPositions.add(position);
        }else {
            holder.checkBox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.length;
    }

    public List<Integer> getSelectedPositions(){
        return mPositions;
    }

    public class MyHolder extends RecyclerView.ViewHolder{

        public TextView txt;
        public CheckBox checkBox;
        public RelativeLayout content;
        public MyHolder(View itemView) {
            super(itemView);
            txt = (TextView) itemView.findViewById(R.id.type_txt);
            checkBox = (CheckBox) itemView.findViewById(R.id.type_checkbox);
            content = (RelativeLayout) itemView.findViewById(R.id.content);

            content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) content.getTag();
                    if(checkBox.isChecked()){
                        checkBox.setChecked(false);
                        if(mPositions.contains(Integer.valueOf(position))){
                            mPositions.remove(Integer.valueOf(position));
                        }
                    }else {
                        checkBox.setChecked(true);
                        if(!mPositions.contains(Integer.valueOf(position))){
                            mPositions.add(position);
                        }
                    }
                }
            });
        }
    }
}
