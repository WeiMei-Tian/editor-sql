package com.gmobile.sqliteeditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.choicefile.ChoiceFileActivity;
import com.gmobile.library.base.assistant.utils.FileUtils;
import com.gmobile.library.base.assistant.utils.TimeUtils;
import com.gmobile.library.base.view.customview.bottomview.BottomViewMgr;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialog;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialogInterface;
import com.gmobile.sqliteeditor.adapter.MyPagerAdapter;
import com.gmobile.sqliteeditor.assistant.AdMobView;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.orm.dao.model.MineData;
import com.gmobile.sqliteeditor.orm.helper.DbUtils;
import com.gmobile.sqliteeditor.orm.helper.implement.MineDataHelper;
import com.gmobile.sqliteeditor.ui.AppFragment;
import com.gmobile.sqliteeditor.ui.FileFragment;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.google.android.gms.ads.AdView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.sql_btn_create_tab)
    FloatingActionButton sqlBtnCreateTab;
    @BindView(R.id.admob_bg_btn)
    Button admobBgBtn;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.admob_close)
    ImageView admobClose;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.admob_layout)
    RelativeLayout admobLayout;
    @BindView(R.id.rootLayout)
    RelativeLayout rootLayout;

    private TextView mDisplayPathTv;
    private String mSelectedPath;

    private List<String> titles;
    private List<BaseFragment> datas;
    private static final int SQL_CREATE_DB_CODE = 2;
    private AdMobView adMobView;
    private MyPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sql_operation);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initToolBar();
        initData();
        initFab();
        initAdMob();

    }

    private void initAdMob() {
        adMobView = new AdMobView();
        adMobView.init(admobLayout,rootLayout,admobBgBtn,adView,admobClose,this);
        adMobView.setupGoogleAdMob();
    }

    private void initData() {
        titles = new ArrayList<>();
        datas = new ArrayList<>();

        titles.add("应用");
        titles.add("文件");
        titles.add("最近");
        titles.add("我的");

        BaseFragment appFragment = AppFragment.getInstance(DataType.appData);

        BaseFragment fileFragment = FileFragment.getInstance(DataType.fileData);

        BaseFragment historyFragment = AppFragment.getInstance(DataType.historyData);

        BaseFragment mineFragment = AppFragment.getInstance(DataType.mineData);

        datas.add(appFragment);
        datas.add(fileFragment);
        datas.add(historyFragment);
        datas.add(mineFragment);

        adapter = new MyPagerAdapter(getSupportFragmentManager(), datas, titles);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initFab() {
        sqlBtnCreateTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (TextUtils.isEmpty(mFragment.getCurrentPath())) {
//                    return;
//                }
//
//                if (Utils.isFunNeedBuy(SqlFileChoiceActivity.this)) {
//                    return;
//                }

                showNewDbDialog();
//                StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_NEW_DB);
            }
        });
    }

    //TODO 后期新建数据库界面定了以后应用此方法
    public void showNewDbDialog() {
        View view = View.inflate(this, R.layout.sql_create_db, null);

        final MaterialEditText fileName = (MaterialEditText) view.findViewById(R.id.sql_create_name);
        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        int titleResId = R.string.sql_create_new_db;

        mDisplayPathTv = (TextView) view.findViewById(R.id.display_path_tv);
        LinearLayout layoutPath = (LinearLayout) view.findViewById(R.id.layout_path);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mSelectedPath = path;
        mDisplayPathTv.setText(path);
        layoutPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChoicePath();
            }
        });

        builder.setTitle(titleResId)
                .setView(view)
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomAndSoftInput(MainActivity.this, fileName);
                            }
                        })
                .setPositiveButton(R.string.okey,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {

                                String name = Utils.isFileExist(MainActivity.this, fileName, ".db", mSelectedPath);
                                if (name == null) return;

                                BottomViewMgr.hideBottomAndSoftInput(MainActivity.this, fileName);
                                if (SQLManager.createDatabase(mSelectedPath, name) == SqlConstant.SUCCESS) {
                                    String dbFilePath = FileUtils.concatPath(mSelectedPath) + name;
                                    Utils.openDB(MainActivity.this, dbFilePath, name);

                                    MineData mineData = new MineData();
                                    mineData.setName(name);
                                    mineData.setLastModified(TimeUtils.getCurrentTime());
                                    mineData.setPath(mSelectedPath);
                                    DbUtils.getMineDataHelper().save(mineData);
                                    if(viewPager.getCurrentItem() == 3){
                                        AppFragment appFragment = (AppFragment) datas.get(3);
                                        appFragment.initData();
                                    }

                                } else {
                                    ToastManger.showErrorToast(MainActivity.this, R.string.operation_failed);
                                }
                            }
                        });
        SlidingUpDialog dialog = builder.create();
        BottomViewMgr.showBottomView(this, dialog);
        BottomViewMgr.showSoftInput(fileName);
    }

    private void startChoicePath() {
        ChoiceFileActivity.startActivity(MainActivity.this, getResources().getString(R.string.choice_path_create_db), true, SQL_CREATE_DB_CODE);
    }

    private void initToolBar() {

        toolbar.setTitle(R.string.sqliteeditor);
        toolbar.inflateMenu(R.menu.menu_sql_operation);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleAppearance);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_setting:

                        break;
                    case R.id.action_feedback:

                        break;
                    case R.id.action_about:

                        break;
                }
                return true;
            }
        });

        tabLayout.setTabTextColors(getResources().getColor(R.color.tab_normal_white), getResources().getColor(R.color.tab_selected_color));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tab_selected_color));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ViewEvent event) {
        ViewEvent.EvenType type = event.getType();
        Bundle args = event.getArgs();

        switch (type) {
            case controlAdMob:
                adMobView.controlAdMob(args);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseFragment fragment = datas.get(viewPager.getCurrentItem());
            fragment.onBackPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SQL_CREATE_DB_CODE) {
                mSelectedPath = data.getStringExtra("confirm_path");
                mDisplayPathTv.setText(mSelectedPath);
            }
        }
    }
}
