package com.gmobile.sqliteeditor.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.ui.presenter.BaseViewImp;

import butterknife.ButterKnife;

/**
 * Created by admin on 2016/11/22.
 */
public abstract class BaseFragment extends Fragment{

    protected Context context;
    protected BaseViewImp viewImp;

    protected DataType mDataType;

    public BaseFragment() {
    }

    public BaseFragment(DataType dataType) {
        mDataType = dataType;
    }

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        initDataType();
        viewImp = createPresenter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = initParent(inflater, container);
        ButterKnife.bind(this,rootView);

        findViews(rootView);
        initView();
        setListeners();
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private View initParent(LayoutInflater inflater, ViewGroup container) {
        RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_base, container, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View centerView = View.inflate(context, getLayoutId(), null);
        rootView.addView(centerView, layoutParams);
        return rootView;
    }

    /**
     * 加载子类布局
     */
    protected abstract int getLayoutId();

    /**
     * 加载控件
     */
    protected abstract void findViews(View rootView);

    /**
     * 设置监听
     */
    protected abstract void setListeners();

    /**
     * 初始化数据
     */
    protected abstract void initData();

    protected abstract void initView();

    protected abstract void initDataType();

    protected abstract BaseViewImp createPresenter();

    public abstract void onBackPressed();

    @Override
    public Context getContext() {
        return context;
    }
}
