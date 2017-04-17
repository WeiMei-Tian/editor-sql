package com.gmobile.sqliteeditor.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by admin on 2016/11/22.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Context context;

    @BindView(R.id.toolbar_button)
    TextView toolbarButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    protected void initView() {
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);
        context = this;
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment);
        if (frameLayout != null) {
            frameLayout.addView(LayoutInflater.from(context).inflate(getLayoutId(), null));
        }

        findViews();
        initIntent(getIntent());
        setListeners();
        initToolBar();
        initFloatBtn();
    }


    protected abstract int getLayoutId();

    protected abstract void initData();

    protected abstract void fabBtnClick();

    protected abstract void initIntent(Intent intent);

    abstract void findViews();

    protected abstract void setListeners();

    protected abstract String getToolbarTitle();

    protected abstract int getToolBarMenu();

    protected void initToolBar() {
        if (toolbar != null) {
            toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleAppearance);
            toolbar.setTitle(getToolbarTitle());
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity.this.finish();
                }
            });

            if (getToolBarMenu() != 0) {
                toolbar.inflateMenu(getToolBarMenu());
            }
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }
    }

    private void initFloatBtn() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabBtnClick();
            }
        });
    }

    protected void setFloatingActionButtonVisible() {
        fab.setVisibility(View.VISIBLE);
    }

    protected void setToolBarButtonVisible() {
        toolbarButton.setVisibility(View.VISIBLE);
    }

    protected void setToolBarButtonGone() {
        toolbarButton.setVisibility(View.GONE);
    }
}
