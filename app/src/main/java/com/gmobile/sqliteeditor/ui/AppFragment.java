package com.gmobile.sqliteeditor.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.AppAdapter;
import com.gmobile.sqliteeditor.adapter.BaseAdapter;
import com.gmobile.sqliteeditor.adapter.HistoryAdapter;
import com.gmobile.sqliteeditor.adapter.MineAdapter;
import com.gmobile.sqliteeditor.adapter.listener.AppItemListener;
import com.gmobile.sqliteeditor.adapter.listener.HistoryItemListener;
import com.gmobile.sqliteeditor.adapter.listener.MineItemListener;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.ui.event.AppDataType;
import com.gmobile.sqliteeditor.ui.presenter.AppViewImp;
import com.gmobile.sqliteeditor.ui.presenter.BaseViewImp;
import com.gmobile.sqliteeditor.ui.view.AppView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by admin on 2016/11/22.
 */
public class AppFragment extends BaseFragment implements AppView {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycleview_app)
    RecyclerView recyclerView;

    private BaseAdapter mAdapter;

    public AppFragment() {
        super();
    }

    public static AppFragment getInstance(DataType dataType){
        Bundle bundle = new Bundle();
        bundle.putSerializable("datatype", dataType);
        AppFragment appFragment = new AppFragment();
        appFragment.setArguments(bundle);
        return appFragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_app;
    }

    @Override
    protected void findViews(View rootView) {

    }

    @Override
    protected void setListeners() {

    }

    @Override
    public void initData() {

        boolean hasTop = PreferenceUtils.getPrefBoolean(context, Constant.HAS_TOP, true);
        int loaderId = 0;
        Bundle bundle = new Bundle();
        switch (mDataType) {
            case appData:
                swipeRefreshLayout.setRefreshing(true);
                mAdapter = new AppAdapter(context, hasTop, new AppItemListener(this));
                loaderId = DataType.appData.ordinal();
                bundle.putSerializable(Constant.FILE_DATA_TYPE, AppDataType.init);
                break;

            case historyData:

                swipeRefreshLayout.setRefreshing(true);
                mAdapter = new HistoryAdapter(context, hasTop, new HistoryItemListener(this));
                loaderId = DataType.historyData.ordinal();
                break;

            case mineData:

                swipeRefreshLayout.setRefreshing(true);
                mAdapter = new MineAdapter(context, hasTop, new MineItemListener(this));
                loaderId = DataType.mineData.ordinal();
                break;
        }

        initLoaderBy(loaderId, bundle);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void initView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.app_bar_bg_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Observable.timer(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
            }
        });
    }

    @Override
    protected void initDataType() {
        mDataType = (DataType) getArguments().get("datatype");
    }

    @Override
    protected BaseViewImp createPresenter() {
        return new AppViewImp(this, mDataType);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        viewImp.unRegisterEvent();
    }

    @Override
    public void controlReFreshLayout() {

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void hideTop() {
        if (mAdapter != null) {
            mAdapter.setTopSize(0);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void gotoClickPath(int position) {
        mAdapter.gotoClickPath(position);

        Bundle args = new Bundle();
        args.putSerializable(Constant.FILE_DATA_TYPE, AppDataType.gointo);
        args.putInt(Constant.CLICK_POSITION, position);

        int loaderId = DataType.appData.ordinal();
        initLoaderBy(loaderId, args);
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onBackPressed() {

        Bundle args = new Bundle();
        args.putSerializable(Constant.FILE_DATA_TYPE, AppDataType.goback);

        int loaderId = DataType.appData.ordinal();
        initLoaderBy(loaderId, args);
        swipeRefreshLayout.setRefreshing(true);

//        ((Activity) context).finish();
    }

    private void initLoaderBy(int loaderId, Bundle bundle) {
        try {
            getLoaderManager().destroyLoader(loaderId);
            getLoaderManager().initLoader(loaderId, bundle, mAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
