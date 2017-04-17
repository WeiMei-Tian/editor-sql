package com.gmobile.sqliteeditor.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.listener.BaseItemListener;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.model.DataModel;
import com.gmobile.sqliteeditor.model.bean.BaseData;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 2016/11/22.
 */
public abstract class BaseAdapter<T extends BaseData> extends RecyclerView.Adapter<BaseHolder> implements LoaderManager.LoaderCallbacks<DataModel> {

    protected Context context;
    protected List<T> datas = new ArrayList<>();
    protected BaseItemListener itemListener;

    protected static final int TYPE_PAY = 0;
    protected static final int TYPE_NORMAL = 1;
    protected int topSize;

    public BaseAdapter(Context context, boolean hasTop, BaseItemListener baseItemListener) {
        this.context = context;
        this.itemListener = baseItemListener;
        if (hasTop) {
            topSize = 1;
        }
    }

    public int getTopSize() {
        return topSize;
    }

    public void setTopSize(int topSize) {
        this.topSize = topSize;
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAY) {
            return new TopHolder(LayoutInflater.from(context).inflate(R.layout.sql_tabs_pay_layout, null));
        } else {
            return new InnerHolder(LayoutInflater.from(context).inflate(R.layout.list_item, null));
        }
    }

    @Override
    public void onBindViewHolder(final BaseHolder holder, int position) {
        if (topSize > 0) {
            if (position > 0) {
                bindData((InnerHolder) holder, position - topSize);
            }
        } else {
            bindData((InnerHolder) holder, position);
        }
        holder.itemView.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? datas.size() + topSize : topSize;
    }

    @Override
    public int getItemViewType(int position) {
        if (topSize > 0) {
            if (position == 0) {
                return TYPE_PAY;
            } else {
                return TYPE_NORMAL;
            }
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public Loader<DataModel> onCreateLoader(int id, Bundle args) {

        setDataTypeForArgs(args);
        return new DataLoader(context, args);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<DataModel> loader, DataModel data) {
        this.datas = data.getDatas();
        loadFinished();
    }


    @Override
    public void onLoaderReset(Loader<DataModel> loader) {

    }

    protected abstract void loadFinished();

    protected abstract void setDataTypeForArgs(Bundle args);

    protected abstract void bindData(InnerHolder holder, int position);

    public abstract void gotoClickPath(int position);


    public class InnerHolder extends BaseHolder {

        @BindView(R.id.itemView)
        RelativeLayout itemView;
        @BindView(R.id.fileName)
        TextView fileName;
        @BindView(R.id.item_thumb)
        ImageView appIcon;
        @BindView(R.id.fileInfo)
        TextView fileInfo;
        @BindView(R.id.item_more)
        ImageView itemMore;

        public InnerHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(itemListener);

            itemMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopMenu(v);
                }
            });
        }
    }

    public class TopHolder extends BaseHolder {

        @BindView(R.id.tv_update)
        TextView tvUpdate;

        View.OnClickListener onPayClick = new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                PreferenceUtils.setPrefBoolean(context, Constant.HAS_TOP, false);
                EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.hideTop, new Bundle()));
            }
        };

        public TopHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            tvUpdate.setOnClickListener(onPayClick);
            itemView.setOnClickListener(onPayClick);
        }
    }

    private void showPopMenu(View ancho) {
        FeViewUtils.showPopMenu(context, ancho, R.menu.db_menu, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
    }

}
