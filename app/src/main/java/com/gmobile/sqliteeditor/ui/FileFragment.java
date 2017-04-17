package com.gmobile.sqliteeditor.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.FileAdapter;
import com.gmobile.sqliteeditor.adapter.listener.FileItemListener;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.ui.event.FileDataType;
import com.gmobile.sqliteeditor.ui.presenter.BaseViewImp;
import com.gmobile.sqliteeditor.ui.presenter.FileViewImp;
import com.gmobile.sqliteeditor.ui.view.FileView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by admin on 2016/11/22.
 */
public class FileFragment extends BaseFragment implements FileView {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycleview_app)
    RecyclerView recyclerView;

    private FileAdapter adapter;

    public FileFragment() {
        super();
    }

    public static FileFragment getInstance(DataType dataType){
        Bundle bundle = new Bundle();
        bundle.putSerializable("datatype", dataType);
        FileFragment fileFragment = new FileFragment();
        fileFragment.setArguments(bundle);
        return fileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

        } else {

        }
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
    protected void initData() {

        adapter = new FileAdapter(context,PreferenceUtils.getPrefBoolean(context,Constant.HAS_TOP,true),new FileItemListener(this));
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.FILE_DATA_TYPE, FileDataType.init);
        getLoaderManager().destroyLoader(DataType.fileData.ordinal());
        getLoaderManager().initLoader(DataType.fileData.ordinal(), bundle, adapter);

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager2);
        recyclerView.setAdapter(adapter);
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
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(Constant.FILE_DATA_TYPE, FileDataType.refresh);
                                bundle.putString(Constant.BACK_PATH, adapter.getParentPath());
                                getLoaderManager().destroyLoader(DataType.fileData.ordinal());
                                getLoaderManager().initLoader(DataType.fileData.ordinal(), bundle, adapter);
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
        return new FileViewImp(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewImp.unRegisterEvent();
    }

    @Override
    public void controlReFreshLayout() {
        Observable.timer(500,TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void hideTop() {
        adapter.setTopSize(0);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void gotoClickPath(int position) {
        adapter.gotoClickPath(position - adapter.getTopSize() - 1);

        Bundle args = new Bundle();
        args.putSerializable(Constant.FILE_DATA_TYPE, FileDataType.gotoPath);
        args.putInt(Constant.CLICK_POSITION, position - adapter.getTopSize() - 1);
        getLoaderManager().destroyLoader(DataType.fileData.ordinal());
        getLoaderManager().initLoader(DataType.fileData.ordinal(), args, adapter);
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void refreshTopPath(int position) {
        adapter.refreshTopPath(position);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onBackPressed() {
        if(Environment.getExternalStorageDirectory().getAbsolutePath().equals(adapter.getFileData().getPath())){
            ((Activity)context).finish();
        }else {

            adapter.backPath();
            Bundle args = new Bundle();
            args.putSerializable(Constant.FILE_DATA_TYPE,FileDataType.backPath);
            args.putString(Constant.BACK_PATH, adapter.getFileData().getPath());
            getLoaderManager().destroyLoader(DataType.fileData.ordinal());
            getLoaderManager().initLoader(DataType.fileData.ordinal(), args, adapter);
            swipeRefreshLayout.setRefreshing(true);
        }
    }

}
