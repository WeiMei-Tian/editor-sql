package com.gmobile.sqliteeditor.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobile.choicefile.ChoiceFileActivity;
import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.library.base.view.customview.bottomview.BottomTip;
import com.gmobile.library.base.view.customview.bottomview.BottomViewMgr;
import com.gmobile.library.base.view.customview.dialog.FeDialog;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialog;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialogInterface;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.DbTablesAdapter;
import com.gmobile.sqliteeditor.adapter.listener.SqlActivityResultHandler;
import com.gmobile.sqliteeditor.assistant.AdMobView;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.assistant.PermissionUtils;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.assistant.sqlite.ExportTool;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.library.fab.FloatingActionButton;
import com.gmobile.sqliteeditor.library.fab.FloatingActionMenu;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.gmobile.sqliteeditor.widget.FeSwipeRefreshLayout;
import com.google.android.gms.ads.AdView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by admin on 2016/9/20.
 */
public class DbTablesActivity extends SwipeBackActivity {

    public static final String DB_PATH = "db_path";
    public static final String DB_NAME = "db_name";
    public static final String TAB_PATH = "tab_path";
    private static final int SQL_CREATE_TAB__PATH_CODE = 1;
    public static final int CREATE_TABLE_REFRESH = 3;
    private static final int SQL_IMPORT_TAB_PATH_CODE = 9;
    private static final int SQL_IMPORT_DATABASE_PATH_CODE = 10;
    private RecyclerView mRecycleView;
    private TextView mDisplayPathTv;
    private String mSelectedPath;
    private FeSwipeRefreshLayout mSwipeRefresh;
    private DbTablesAdapter mTabsAdapter;
    private FloatingActionButton mCreateNewTab, mImportTab;
    private FloatingActionMenu mMenu;
    private FloatingActionButton mFab;
    private String mDbPath;
    private ArrayList<String> mTableNames = null;
    private boolean isEdit;
    private Toolbar toolbar;
    private int mClickPosition = -1;
    private static final int SCROLL_DISTANCE = 400;
    private String mDbName;
    private BottomTip mExportingTip, mImportingTip;
    private SqlActivityResultHandler resultHandler;

    private RelativeLayout mAdmobLayout, mRootLayout;
    private int mHeight;
    private Button mAdBg;
    private ImageView mAdClose;
    private AdView mAdView;

    private AdMobView mAdMobView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.sql_tabs_activity_layout);

        if (!initIntent()) {
            return;
        }

        initView();
        setListener();
        initData();
        initAdMob();

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                initFab();
            }
        });

    }

    private void initAdMob() {
        mAdMobView = new AdMobView();
        mAdMobView.init(mAdmobLayout, mRootLayout, mAdBg, mAdView, mAdClose, this);
        mAdMobView.setupGoogleAdMob();
    }

    private void initFab() {
        mHeight = mFab.getMeasuredHeight() + FeViewUtils.dpToPx(16);
        mFab.setVisibility(View.GONE);

        mCreateNewTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMfab(mMenu);

//                if (Utils.isFunNeedBuy(DbTablesActivity.this)) {
//                    return;
//                }

                showRenameOrCreateDialog(true, R.string.sql_create_new_tab);
//                StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_NEW_TABLE);
            }
        });

        mImportTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMfab(mMenu);

//                if (Utils.isFunNeedBuy(DbTablesActivity.this)) {
//                    return;
//                }

                importTabForResult(DbTablesActivity.this, SQL_IMPORT_TAB_PATH_CODE, R.string.sql_import_tab_path);
//                StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_IMPORT_TABLE);
            }
        });
    }

    public void importTabForResult(Activity activity, int code, int title) {
        ChoiceFileActivity.startActivity(activity, getResources().getString(title), false, code);
    }

    private boolean initIntent() {
        if (getIntent() == null) {
            ToastManger.showErrorToast(this, R.string.open_fail);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
            return false;
        }

        mDbPath = getIntent().getStringExtra(DB_PATH);
        mDbName = getIntent().getStringExtra(DB_NAME);

        if (TextUtils.isEmpty(mDbPath)) {
            mDbPath = getIntent().getDataString();
            if (mDbPath == null) {
                ToastManger.showErrorToast(this, R.string.open_fail);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
                return false;
            }

            if (mDbPath.contains(SqlConstant.CONTENT_PREFIX)) {
                mDbPath = Utils.saveAttachment(this, getIntent());
            } else {
                mDbPath = Uri.decode(mDbPath);
                mDbPath = mDbPath.replace(SqlConstant.SYSTEM_FILE_PREFIX, "");
            }

            if (!TextUtils.isEmpty(mDbPath)) {
                File file = new File(mDbPath);
                mDbName = file.getName();

                if (!file.exists()) {
                    ToastManger.showErrorToast(this, R.string.operation_failed);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                    return false;
                }
            }

            Log.e("sg", "mDbPath----------" + mDbPath);
            if (!Utils.isCanOpen(this, mDbPath)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
                return false;
            }
        }

        return true;
    }

    protected void initView() {
        mRecycleView = (RecyclerView) findViewById(R.id.sql_tabs_recycleview);
        mSwipeRefresh = (FeSwipeRefreshLayout) findViewById(R.id.recycler_view_container);
        mSwipeRefresh.setProgressViewOffset(true, -20, 150);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.mFab);
        mMenu = (FloatingActionMenu) findViewById(R.id.mFam);
        mCreateNewTab = (FloatingActionButton) findViewById(R.id.sql_btn_create_tab);
        mImportTab = (FloatingActionButton) findViewById(R.id.sql_btn_import_tab);
        mAdView = (AdView) findViewById(R.id.adView);
        mAdBg = (Button) findViewById(R.id.admob_bg_btn);
        mAdClose = (ImageView) findViewById(R.id.admob_close);
        mAdmobLayout = (RelativeLayout) findViewById(R.id.admob_layout);
        mRootLayout = (RelativeLayout) findViewById(R.id.roottabslayout);
        mMenu.setClosedOnTouchOutside(true);
    }

    private void setListener() {
        resultHandler = new SqlActivityResultHandler(this);

        setRefresh();
        if (toolbar != null) {
            toolbar.setTitleTextAppearance(this, R.style.ToolbarTitleAppearance);
            toolbar.setTitle(mDbName);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_left_navbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));

            toolbar.inflateMenu(R.menu.sql_db_tables_menu);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_edit:

//                            StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_EDIT_TABLE);
//
//                            Intent intent = new Intent(DbTablesActivity.this, SqlCreateTableActivity.class);
//                            intent.putExtra(SqlCreateTableActivity.TABLE_NAMES_KEY, mTableNames);
//                            intent.putExtra(SqlCreateTableActivity.DB_NAME, mDbName);
//                            intent.putExtra(SqlCreateTableActivity.TABLE_NAME_KEY, mTableNames.get(mClickPosition));
//                            startActivity(intent);
                            break;

                        case R.id.action_delete:
//                            if (Utils.isFunNeedBuy(DbTablesActivity.this)) {
//                                clearLongClick();
//                                return true;
//                            }
//
//                            StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_DELETE_TABLE);

                            if (mClickPosition != -1) {
                                showSureDeleteDialog();
                            }
                            break;

                        case R.id.action_more_im_sql:
                            Intent inten = new Intent(DbTablesActivity.this, SqlCommondActivity.class);
                            startActivity(inten);
                            break;

                        case R.id.action_export:
                            showExportDialog();
//                            StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_EXPORT_DB);
                            break;

                        case R.id.action_rename:

                            break;

                        case R.id.action_create_desk:
                            FeViewUtils.sendFileShortcut(DbTablesActivity.this,new File(mDbPath));
                            break;

                    }
                    return true;
                }
            });

        }

        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                showHideFamByScroll(dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    showFamStateIdle();
                }
            }
        });

    }

    public void showHideFamByScroll(int dy) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMenu.getLayoutParams();
        if (params.bottomMargin <= 0 && params.bottomMargin >= -mHeight) {
            params.bottomMargin -= dy;
        }
        if (params.bottomMargin > 0) {
            params.bottomMargin = 0;
        }
        if (params.bottomMargin < -mHeight) {
            params.bottomMargin = -mHeight;
        }
        mMenu.setLayoutParams(params);
    }


    public void showFamStateIdle() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMenu.getLayoutParams();
        if (params.bottomMargin > -mHeight / 2) {
            params.bottomMargin = 0;
        } else if (params.bottomMargin <= -mHeight / 2) {
            params.bottomMargin = -mHeight;
        }
        Log.d("FAB", "idle " + params.bottomMargin);
        mMenu.setLayoutParams(params);
    }

    private void showSureDeleteDialog() {
        FeDialog.Builder builder = new FeDialog.Builder(this);

        builder.setMessage(R.string.sure_delete_table)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BottomViewMgr.hideBottomView();
                    }
                })
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BottomViewMgr.hideBottomView();
                        SQLManager.getSQLHelper(DbTablesActivity.this).dropTable(mTableNames.get(mClickPosition));
                        mTableNames.remove(mClickPosition);
                        clearLongClick();

                    }
                });

        BottomViewMgr.showBottomView(builder.create());
    }

    private void showExportDialog() {

        View view = View.inflate(this, R.layout.sql_db_export_select_path, null);

        final MaterialEditText fileName = (MaterialEditText) view.findViewById(R.id.sql_export_db_name);
        fileName.setText(mDbName);
        int index = mDbName.lastIndexOf(".");
        if (index > -1) {
            fileName.setSelection(0, index);
        } else {
            fileName.setSelection(0, mDbName.length());
        }

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

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        builder.setTitle(R.string.sql_db_export)
                .setView(view)
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomAndSoftInput(DbTablesActivity.this, fileName);
                            }
                        })
                .setPositiveButton(R.string.okey,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {

                                String name = Utils.isFileExist(DbTablesActivity.this, fileName, ".sql", mSelectedPath);
                                if (name == null) return;

                                BottomViewMgr.hideBottomAndSoftInput(DbTablesActivity.this, fileName);
                                showExportDlg();

                                Observable.just(name)
                                        .observeOn(Schedulers.newThread())
                                        .map(new Func1<String, Integer>() {
                                            @Override
                                            public Integer call(String name) {
                                                try {
                                                    return ExportTool.exportDatabase(name, mSelectedPath);
                                                } catch (NoSuchFieldException e) {
                                                    e.printStackTrace();
                                                    return Constant.FAIL;
                                                }
                                            }
                                        })
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<Integer>() {
                                            @Override
                                            public void call(Integer result) {
                                                hideExportDlg();

                                                if (result == SqlConstant.SUCCESS) {
                                                    ToastManger.showDoneToast(DbTablesActivity.this, R.string.operation_success);
                                                } else {
                                                    ToastManger.showErrorToast(DbTablesActivity.this, R.string.operation_failed);
                                                }
                                            }
                                        });
                            }
                        });
        SlidingUpDialog dialog = builder.create();
        BottomViewMgr.showBottomView(this, dialog);
        BottomViewMgr.showSoftInput(fileName);
    }

    private void startChoicePath() {
        ChoiceFileActivity.startActivity(this, getResources().getString(R.string.sql_create_db_path), true, SQL_CREATE_TAB__PATH_CODE);
    }

    private void setRefresh() {
        setRefreshLayoutImpl(mSwipeRefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                Observable.timer(600, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                        });
            }
        });
    }


    private void setRefreshLayoutImpl(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setDistanceToTriggerSync(SCROLL_DISTANCE);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public void closeMfab(FloatingActionMenu menu) {

        if (menu != null && menu.isOpened()) {
            menu.close(true);
        }
    }

    public void showRenameOrCreateDialog(final boolean isCreate,int title) {
        View view = View.inflate(this, R.layout.sql_create_tab_dialog, null);

        final MaterialEditText fileName = (MaterialEditText) view.findViewById(R.id.sql_create_name);
        if(!isCreate){
            String tableName = mTableNames.get(mClickPosition);
            fileName.setText(tableName);
            if(tableName != null){
                fileName.setSelection(0,tableName.length());
            }
        }

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);

        builder.setTitle(title)
                .setView(view)
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomAndSoftInput(DbTablesActivity.this, fileName);
                            }
                        })
                .setPositiveButton(R.string.okey,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {

                                String name = Utils.isFileNameOk(DbTablesActivity.this, fileName, "", "", false);
                                if(isCreate){
                                    boolean hasSameTable = hasSameTable(fileName);
                                    if (name == null || hasSameTable) return;

                                    BottomViewMgr.hideBottomAndSoftInput(DbTablesActivity.this, fileName);

                                    Intent intent = new Intent(DbTablesActivity.this, SqlCreateTableActivity.class);
                                    intent.putExtra(SqlCreateTableActivity.TABLE_NAME_KEY, name);
                                    intent.putExtra(SqlTabDatasActivity.DB_NAME, mDbName);
                                    intent.putStringArrayListExtra(SqlCreateTableActivity.TABLE_NAMES_KEY, mTableNames);
                                    startActivityForResult(intent, CREATE_TABLE_REFRESH);
                                }else {
                                    BottomViewMgr.hideBottomAndSoftInput(DbTablesActivity.this, fileName);
                                    boolean result = SQLManager.getSQLHelper(DbTablesActivity.this).renameTable(mTableNames.get(mClickPosition),name);
                                    if(result){
                                        mTableNames.set(mClickPosition,name);
                                        mTabsAdapter.notifyItemChanged(mClickPosition);
                                        ToastManger.showDoneToast(DbTablesActivity.this,R.string.operation_finish);
                                    }else {
                                        ToastManger.showErrorToast(DbTablesActivity.this,R.string.operation_failed);
                                    }
                                }
                            }
                        });

        SlidingUpDialog dialog = builder.create();
        dialog.hideTitleDivider();
        BottomViewMgr.showBottomView(this, dialog);
        BottomViewMgr.showSoftInput(fileName);
    }

    private boolean hasSameTable(MaterialEditText fileName) {
        boolean hasSame = false;
        String tableName, fileNameStr;
        for (int i = 0; i < mTableNames.size(); i++) {
            tableName = mTableNames.get(i).toLowerCase();
            fileNameStr = fileName.getText().toString().trim().toLowerCase();
            if (tableName.equals(fileNameStr)) {
                hasSame = true;
                fileName.setError(this.getResources().getString(R.string.same_name));
            }
        }
        return hasSame;
    }

    private void initData() {

        mTableNames = (ArrayList<String>) getTabsFromDb(mDbPath);
        if(mTableNames != null){
            Collections.sort(mTableNames);
        }
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mTabsAdapter = new DbTablesAdapter(this);
        mTabsAdapter.setDatas(mTableNames);
        if (mTableNames != null && mTableNames.size() == 0) {
            mRecycleView.setVisibility(View.GONE);
        }

        mRecycleView.setAdapter(mTabsAdapter);
        mTabsAdapter.setTabClickListener(new DbTablesAdapter.TabClickListener() {
            @Override
            public void click(int position) {
                if (isEdit) {
                    clearLongClick();
                } else {
//                    StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_OPEN_TABLE);
//
                    Intent intent = new Intent(DbTablesActivity.this, SqlTabDatasActivity.class);
                    intent.putExtra(SqlTabDatasActivity.TABLE_KEY, mTableNames.get(position));
                    intent.putExtra(SqlTabDatasActivity.DB_NAME, mDbName);
                    intent.putExtra(SqlTabDatasActivity.DATA_SOURCE, SqlConstant.TABLE_DATAS_NORMAL);
                    startActivity(intent);
                }
            }
        });

        mTabsAdapter.setmMoreClickListener(new DbTablesAdapter.TabMoreClickListener() {
            @Override
            public void moreClick(View view,int position) {
                mClickPosition = position;
                showPopMenu(view);
            }
        });
    }

    private void showPopMenu(View ancho) {
        FeViewUtils.showPopMenu(this, ancho, R.menu.table_more_menu, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pop_edit:
//                        StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_EDIT_TABLE);
//
                        Intent intent = new Intent(DbTablesActivity.this, SqlCreateTableActivity.class);
                        intent.putExtra(SqlCreateTableActivity.TABLE_NAMES_KEY, mTableNames);
                        intent.putExtra(SqlCreateTableActivity.DB_NAME, mDbName);
                        intent.putExtra(SqlCreateTableActivity.TABLE_NAME_KEY, mTableNames.get(mClickPosition));
                        startActivity(intent);
                        break;
                    case R.id.pop_delete:
//                        if (Utils.isFunNeedBuy(DbTablesActivity.this)) {
//                        clearLongClick();
//                        return true;
//                            }
//
//                        StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_DELETE_TABLE);
                        if (mClickPosition != -1) {
                            showSureDeleteDialog();
                        }
                        break;
                    case R.id.pop_export:
                        showExportDialog();
//                        StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_EXPORT_DB);
                        break;
                    case R.id.pop_rename:
                        showRenameOrCreateDialog(false,R.string.rename);
                        break;
                }
                return true;
            }
        });
    }

    private List<String> getTabsFromDb(String path) {
        return SQLManager.getSQLHelper(this).getTableNameList();
    }

    private void refreshData() {
        mTableNames = (ArrayList<String>) getTabsFromDb(mDbPath);
        mTabsAdapter.setDatas(mTableNames);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        SQLManager.destroyManager();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEdit) {
                clearLongClick();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void clearLongClick() {
        isEdit = false;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.sql_db_tables_menu);
        if (mTabsAdapter != null) {
            mTabsAdapter.clearEditType();
        }
        mClickPosition = -1;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ViewEvent event) {
        ViewEvent.EvenType type = event.getType();
        Bundle args = event.getArgs();

        switch (type) {
            case controlAdMob:
                mAdMobView.controlAdMob(args);
                break;

            case controlSqlPayTip:
                clearLongClick();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CREATE_TABLE_REFRESH) {
                refreshData();

            } else if (requestCode == SQL_IMPORT_TAB_PATH_CODE || requestCode == SQL_IMPORT_DATABASE_PATH_CODE) {
                //TODO 导入数据库和数据表

                showImportDlg();
                String path = data.getStringExtra(TAB_PATH);
                Observable.just(path)
                        .observeOn(Schedulers.newThread())
                        .map(new Func1<String, Integer>() {
                            @Override
                            public Integer call(String path) {
                                try {
                                    return ExportTool.importTab(path);
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                                return Constant.FAIL;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer result) {
                                hideImportDlg();

                                if (result == SqlConstant.SUCCESS) {
                                    refreshData();
                                    ToastManger.showDoneToast(DbTablesActivity.this, R.string.operation_success);
                                } else {
                                    ToastManger.showErrorToast(DbTablesActivity.this, R.string.operation_failed);
                                }
                            }
                        });

            } else if (requestCode == SQL_CREATE_TAB__PATH_CODE) {

                mSelectedPath = data.getStringExtra(Constant.CONFIRM_PATH);
                mDisplayPathTv.setText(mSelectedPath);

            } else {

                if (resultHandler != null) {
                    resultHandler.handleResult(requestCode, resultCode, data);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            switch (requestCode) {
                case PermissionUtils.WRITE_EXTERNAL_STORAGE_PER_REQUEST:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        StatisticsData.recordOperationId(StatisticsData.WRITE_EXTERNAL_STORAGE_PER_ACCEPT);

                        if (!PermissionUtils.checkHasPermission(this, PermissionUtils.READ_PHONE_STATE_PER[0])) {
                            ActivityCompat.requestPermissions(this, PermissionUtils.READ_PHONE_STATE_PER,
                                    PermissionUtils.READ_PHONE_STATE_PER_REQUEST);
                        }

                    } else {
//                        StatisticsData.recordOperationId(StatisticsData.WRITE_EXTERNAL_STORAGE_PER_REJECT);

                        boolean descDialog = PreferenceUtils.getPrefBoolean(this, Constant.SHOW_DESC_WRITE_STORAGE_PER_DIALOG, true);
                        if (descDialog) {
                            PermissionUtils.askStoragePermisson(this);
                            PreferenceUtils.setPrefBoolean(this, Constant.SHOW_DESC_WRITE_STORAGE_PER_DIALOG, false);
                        } else {
                            boolean show = PermissionUtils.showRequestPermission(this, PermissionUtils.WRITE_EXTERNAL_STORAGE_PER[0]);
                            if (!show) {
                                PermissionUtils.setStoragePermisson(this);
                            } else {
                                finish();
                            }
                        }
                    }
                    return;
                case PermissionUtils.READ_PHONE_STATE_PER_REQUEST:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        StatisticsData.recordOperationId(StatisticsData.READ_PHONE_STATE_PER_ACCEPT);
                        checkReadPhoneState(grantResults[0]);
                    } else {
//                        StatisticsData.recordOperationId(StatisticsData.READ_PHONE_STATE_PER_REJECT);
                    }
                    return;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkReadPhoneState(int grantResult) {
//        if (grantResult == PackageManager.PERMISSION_GRANTED) {
//            Utils.setUniqueDeviceId(this);
//            ServerMsg.getServerInfo(this, false);
//            PurchaseVerify.purchaseServerInfo(this);
//            checkPermissionForAdMob(grantResult);
//        } else {
//            if (!FeFunBase.hideAdMob()) {
//                setupAdMobLayout(View.VISIBLE, 50);
//            }
//        }
    }

    private void checkPermissionForAdMob(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            mAdMobView.setupGoogleAdMob();
        } else {
//            if (!FeFunBase.hideAdMob()) {
            mAdMobView.setupAdMobLayout(View.VISIBLE, 50);
//            }
        }
    }

    private void showImportDlg() {
        if (mImportingTip == null) {
            mImportingTip = FeViewUtils.createLoadingDialog(DbTablesActivity.this, getString(R.string.importing));
        }
        mImportingTip.show();
    }

    private void showExportDlg() {
        if (mExportingTip == null) {
            mExportingTip = FeViewUtils.createLoadingDialog(DbTablesActivity.this, getString(R.string.exporting));
        }
        mExportingTip.show();
    }

    private void hideExportDlg() {
        if (mExportingTip != null) {
            mExportingTip.dismiss();
        }
    }

    private void hideImportDlg() {
        if (mImportingTip != null) {
            mImportingTip.dismiss();
        }
    }
}
