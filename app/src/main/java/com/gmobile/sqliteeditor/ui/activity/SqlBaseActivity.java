package com.gmobile.sqliteeditor.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by admin on 2016/9/21.
 */
public abstract class SqlBaseActivity extends SwipeBackActivity
        implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    protected Activity mContext;
    protected FloatingActionButton floatingActionButton;
    protected Toolbar mToolbar;
    protected TextView mToolBarButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    protected void initView() {
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackgroundDrawable(null);
        setContentView(R.layout.sql_base_activity_layout);
        mContext = this;
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment);
        if( frameLayout != null){
            frameLayout.addView(LayoutInflater.from(mContext).inflate(setViews(),null));
        }
        initIntent(getIntent());
        findViews();

        initToolBar();
        initFloatBtn();
    }

    private void initFloatBtn() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabBtnClick();
            }
        });
    }

    protected abstract int setViews();

    protected abstract void initData();

    protected abstract void fabBtnClick();

    protected abstract void initIntent(Intent intent);

    abstract void findViews();

    protected void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarButton = (TextView) findViewById(R.id.toolbar_button);
        if (mToolbar != null) {
            mToolbar.setTitleTextAppearance(this, R.style.ToolbarTitleAppearance);
            mToolbar.setTitle(getToolbarTitle());
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_left_navbar);
            mToolbar.setNavigationOnClickListener(this);
            mToolbar.setSubtitleTextAppearance(this, R.style.ToolbarSubTitleAppearance);
            mToolbar.setSubtitleTextColor(getResources().getColor(R.color.label_grey));

            if(getToolBarMenu() != 0){
                mToolbar.inflateMenu(getToolBarMenu());
            }
            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onClick(View v) {
        mContext.finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    abstract String getToolbarTitle();

    abstract int getToolBarMenu();

    protected void setFloatingActionButtonVisible() {
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    protected void setToolBarButtonVisible(){
        mToolBarButton.setVisibility(View.VISIBLE);
    }

    protected void setToolBarButtonGone(){
        mToolBarButton.setVisibility(View.GONE);
    }

}
