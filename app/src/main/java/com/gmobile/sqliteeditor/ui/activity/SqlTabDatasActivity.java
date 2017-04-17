package com.gmobile.sqliteeditor.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.gmobile.sqliteeditor.adapter.AddAndUpdateAdapter;
import com.gmobile.sqliteeditor.adapter.CustomSpinnerAdapter;
import com.gmobile.sqliteeditor.adapter.SortTypeAdapter;
import com.gmobile.sqliteeditor.adapter.TabDatasAdapter;
import com.gmobile.sqliteeditor.adapter.TableDataMoreOperAdapter;
import com.gmobile.sqliteeditor.adapter.listener.SqlActivityResultHandler;
import com.gmobile.sqliteeditor.assistant.AdMobView;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.assistant.PermissionUtils;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.assistant.sqlite.ExportTool;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLHelper;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.model.bean.sqlite.ColumnField;
import com.gmobile.sqliteeditor.model.bean.sqlite.TableDataField;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.gmobile.sqliteeditor.widget.MyHorizontalScrollView;
import com.gmobile.sqliteeditor.widget.RightBorderTextView;
import com.google.android.gms.ads.AdView;
import com.jaredrummler.fastscrollrecycle.FastScrollRecyclerView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by admin on 2016/9/20.
 */
public class SqlTabDatasActivity extends SwipeBackActivity implements View.OnClickListener {

    private TableLayout mTabHeader;
    private FastScrollRecyclerView mRecycleView;
    private MyHorizontalScrollView mContent;
    private TextView mDisplayPathTv;
    private String mSelectedPath;
    private List<Integer> mWidthList;
    private Toolbar mToolBar;
    private List<ColumnField> columnFields;
    private List<Map<String, TableDataField>> tablesData;
    private List<String> mTitles;
    private BottomTip mLoadingTip, mImportingTip, mExportingTip;
    private AdView mAdView;
    private Button mAdMobBgBtn;
    private FloatingActionButton btn;
    private int mClickPosition = -1;
    private boolean isEdit;
    private LinearLayoutManager mLinearLayoutManager;
    private RelativeLayout mAdmobLayout, mRootLayout;
    private ImageView mAdClose;
    private BottomSheetDialog mSortDialog;
    private boolean isFromSql = false;
    private static String mTableName;
    private TabDatasAdapter mTabDatasAdapter;
    private String selectFormat;
    private String mSqlStr;
    private int dataSource = SqlConstant.TABLE_DATAS_NORMAL;
    private SqlActivityResultHandler resultHandler;
    private SlidingUpDialog addOrUpdateDialog;

    private static final int SQL_CREATE_TAB_PATH_CODE = 1;
    private static final int SQL_IMPORT_TAB_PATH_CODE = 10;

    public static final String SORT_TYPE = "sort_type";
    public static final String TABLE_KEY = "table";
    public static final String DB_NAME = "db_name";
    public static final String DATA_SOURCE = "data_source";
    public static final String SQL_STR = "sql_str";
    public static final String FILTER_COLUM = "filter_colum";
    public static final String FILTER_KEY = "filter_key";

    private int mHeight;

    private ArrayList<ColumnField> tmpEditNewData = new ArrayList<>();
    private int tmpEditOldClickPosition;
    private boolean hasEditDate = false;
    private AdMobView adMobView;


    public static Intent newIntent(Context context, int dataSource, String sqlStr) {
        Intent intent = new Intent(context, SqlTabDatasActivity.class);
        intent.putExtra(SqlTabDatasActivity.DATA_SOURCE, dataSource);
        intent.putExtra(SqlTabDatasActivity.SQL_STR, sqlStr);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        setContentView(R.layout.sql_tab_viewactivity_layout);
        initIntent();
        initView();
        setListener();
        initAdMob();

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mHeight = btn.getMeasuredHeight() + FeViewUtils.dpToPx(26);
            }
        });
    }

    private void initAdMob() {
        adMobView = new AdMobView();
        adMobView.init(mAdmobLayout, mRootLayout, mAdMobBgBtn, mAdView, mAdClose, this);
        adMobView.setupGoogleAdMob();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initToolBar();
        mToolBar.getMenu().clear();
        switch (dataSource) {
            case SqlConstant.TABLE_DATAS_NORMAL:
                getData(null, true, false, null, null);
                break;
            case SqlConstant.TABLE_DATAS_SQL:
                isFromSql = true;
                getDataBySql();
                break;
            case SqlConstant.TABLE_DATAS_FILTER:
                String columnName = getIntent().getStringExtra(FILTER_COLUM);
                String filter_key = getIntent().getStringExtra(FILTER_KEY);
                getData(null, true, true, columnName, filter_key);
                break;
        }
        setToolBarName();
    }

    private void setToolBarName(){
        mToolBar.setBackgroundColor(getResources().getColor(R.color.app_bar_bg_dark));
        switch (dataSource) {
            case SqlConstant.TABLE_DATAS_NORMAL:
                mToolBar.setTitle(mTableName);
                mToolBar.setSubtitle(getIntent().getStringExtra(DB_NAME));
                mToolBar.inflateMenu(R.menu.sql_table_data);
                break;
            case SqlConstant.TABLE_DATAS_SQL:
                mToolBar.setTitle(R.string.sql_commond_response);
                mToolBar.setSubtitle(getIntent().getStringExtra(DB_NAME));
                mToolBar.inflateMenu(R.menu.sql_commond_data);
                break;
            case SqlConstant.TABLE_DATAS_FILTER:
                mToolBar.setTitle(getResources().getString(R.string.sql_filter_result));
                mToolBar.setSubtitle(mTableName);
                mToolBar.inflateMenu(R.menu.sql_filter_data);
                break;
        }
    }

    private void getDataBySql() {

        final Subscription subscription = showLoadingDlgDelay();
        Observable.just(0)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        tablesData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).execQuerySQLStr(mSqlStr);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                        hideLoadingDlgDelay();

                        List<ColumnField> colFields = new ArrayList<>();
                        if (tablesData != null && tablesData.size() > 0) {
                            Map<String, TableDataField> map = tablesData.get(0);
                            Set<String> set = map.keySet();
                            ColumnField columnField;
                            for (String column : set) {
                                columnField = new ColumnField();
                                columnField.setFieldName(column);
                                colFields.add(columnField);
                            }
                        }

                        columnFields = colFields;
                        initTitleWidthAndHeader(true);
                        initData();

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        subscription.unsubscribe();
                        throwable.printStackTrace();
                        ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.sql_table_sql_error);
                        hideLoadingDlgDelay();
                    }
                });
    }

    private void getData(final String sortColumnName, final boolean isDesc, final boolean isFilter, final String selectColum, final String filter_key) {

        showLoadingDlgDelay();

        Observable.just(0)
                .observeOn(Schedulers.newThread())
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        columnFields = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getColumnFieldList(mTableName);
                        if (isFilter) {
                            if (getResources().getString(R.string.all_field).equals(selectColum)) {
                                tablesData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getFilterData(mTableName, columnFields, selectColum, filter_key, true);
                            } else {
                                tablesData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getFilterData(mTableName, columnFields, selectColum, filter_key, false);
                            }
                        } else {
                            tablesData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getTableDataList(mTableName, 0, 0, sortColumnName, isDesc);
                        }

                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                        hideLoadingDlgDelay();
                        if (tablesData != null && tablesData.size() > 0) {
                            initTitleWidthAndHeader(true);
                            initData();
                        } else {
                            initTitleWidthAndHeader(false);
                            if (isFilter) {
                                ToastManger.showNormalToast(SqlTabDatasActivity.this, R.string.sql_filter_no_data);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        Log.e("onConfigurationChanged", "横竖屏切换");
        super.onConfigurationChanged(newConfig);
        mTabDatasAdapter = null;
        boolean isVertical;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isVertical = false;
            mRecycleView.setMinimumWidth(FeViewUtils.getScreenHeight(this));
        } else {
            isVertical = true;
            mRecycleView.setMinimumWidth(FeViewUtils.getScreenWidth(this));
        }
        initTitleWidthAndHeader(true);
        mTabDatasAdapter = null;
        switch (dataSource) {
            case SqlConstant.TABLE_DATAS_NORMAL:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_NORMAL, isVertical);
                break;
            case SqlConstant.TABLE_DATAS_SQL:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_SQL, isVertical);
                break;
            case SqlConstant.TABLE_DATAS_FILTER:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_FILTER, isVertical);
                break;
        }
        mRecycleView.setAdapter(mTabDatasAdapter);
        mTabDatasAdapter.setDatas(tablesData);
    }


    private void initView() {
        mTabHeader = (TableLayout) findViewById(R.id.sql_tabview_header_tab);
        mRecycleView = (FastScrollRecyclerView) findViewById(R.id.sql_tabview_recycleview);
        mLinearLayoutManager = new LinearLayoutManager(SqlTabDatasActivity.this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLinearLayoutManager);
        mContent = (MyHorizontalScrollView) findViewById(R.id.content);
        mRecycleView.setMinimumWidth(FeViewUtils.getScreenWidth(this));
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mAdmobLayout = (RelativeLayout) findViewById(R.id.admob_layout);
        mRootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        mAdView = (AdView) findViewById(R.id.adView);
        mAdMobBgBtn = (Button) findViewById(R.id.admob_bg_btn);
        mAdClose = (ImageView) findViewById(R.id.admob_close);
        btn = (FloatingActionButton) findViewById(R.id.fab);
        mTabHeader.setBackground(getResources().getDrawable(R.color.table_head_bg));

        if (dataSource != SqlConstant.TABLE_DATAS_NORMAL) {
            btn.setVisibility(View.GONE);
        } else {
            btn.setVisibility(View.VISIBLE);
        }

        resultHandler = new SqlActivityResultHandler(this);
    }

    private void setListener() {

        btn.setOnClickListener(this);
        mContent.setOnScrollListener(new MyHorizontalScrollView.OnScrollListener() {
            @Override
            public void onScroll(int scrollX) {
                mRecycleView.hideBar();
                mRecycleView.updateStartPosition(scrollX);
            }

            @Override
            public void onScrollStop() {
                mRecycleView.updateStartPosition(mContent.getScrollX());
            }
        });

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
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();

        int margin = FeViewUtils.dpToPx(26);
        if (params.bottomMargin <= margin && params.bottomMargin >= -mHeight) {
            params.bottomMargin -= dy;
        }
        if (params.bottomMargin > margin) {
            params.bottomMargin = margin;
        }
        if (params.bottomMargin < -mHeight) {
            params.bottomMargin = -mHeight;
        }
        btn.setLayoutParams(params);
    }


    public void showFamStateIdle() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btn.getLayoutParams();
        if (params.bottomMargin > -mHeight / 2) {
            params.bottomMargin = FeViewUtils.dpToPx(26);
        } else if (params.bottomMargin <= -mHeight / 2) {
            params.bottomMargin = -mHeight;
        }
        btn.setLayoutParams(params);
    }

    protected void initToolBar() {
        if (mToolBar != null) {
            mToolBar.setTitleTextAppearance(this, R.style.CopyToToolbarTitleAppearance);
            mToolBar.setNavigationIcon(R.drawable.ic_arrow_left_navbar);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            mToolBar.setTitleTextColor(getResources().getColor(R.color.white));
            mToolBar.setSubtitleTextAppearance(this, R.style.ToolbarSubTitleAppearance);
            mToolBar.setSubtitleTextColor(getResources().getColor(R.color.label_grey));
            mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    int actionId = -1;
                    switch (item.getItemId()) {
                        case R.id.action_more:
                            showMoreMenuDialog();
                            break;

                        case R.id.action_edit:
                            if (mClickPosition != -1) {
                                addDataOrUpdateToTab(false, false);
//                                actionId = StatisticsData.ActionId.SQLITE_EDIT_DATA;
                            }
                            break;

                        case R.id.action_sql:
                            Intent intent = new Intent(SqlTabDatasActivity.this, SqlCommondActivity.class);
                            startActivity(intent);
                            break;

                        case R.id.action_filter:
                            showFilterDialog();
//                            actionId = StatisticsData.ActionId.SQLITE_FILTER;
                            break;

                        case R.id.action_delete:
//                            if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {
//                                return true;
//                            }
//
                            showSureDeleteDialog(false);
//                            actionId = StatisticsData.ActionId.SQLITE_DELETE_DATA;
                            break;

                        case R.id.action_filter_export:
                            showExportDialog();
                            break;

                        case R.id.action_filter_sql:
                            Intent intent1 = new Intent(SqlTabDatasActivity.this, SqlCommondActivity.class);
                            startActivity(intent1);
                            break;

//                        case R.id.action_unable_delete:
//                        case R.id.action_unable_edit:
//                            return true;
                    }
                    if (actionId > 0) {
//                        StatisticsData.recordOperationId(actionId);
                    }

                    return false;
                }
            });
        }
    }

    private void showMoreMenuDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = View.inflate(this, R.layout.bottom_menu_content, null);
        bottomSheetDialog.setContentView(view);
        RecyclerView itemList = (RecyclerView) view.findViewById(R.id.bottom_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        TableDataMoreOperAdapter adapter = new TableDataMoreOperAdapter(this);
        itemList.setAdapter(adapter);
        adapter.setOnMenuItemClickListener(new TableDataMoreOperAdapter.onMenuClickListener() {
            @Override
            public void onMenuCkick(int strRes) {
                bottomSheetDialog.dismiss();
                int actionId = -1;
                switch (strRes) {
                    case R.string.sql_data_more_information:
                        startInformationActivity();
//                        actionId = StatisticsData.ActionId.SQLITE_CUR_TABLE_INFO;
                        break;

                    case R.string.sql_data_more_clear_data:
//                        if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {
//                            return;
//                        }
//
                        showSureDeleteDialog(true);
//                        actionId = StatisticsData.ActionId.SQLITE_CLEAR_TABLE;
                        break;

                    case R.string.sql_data_more_sort:
                        showSortDialog();
//                        actionId = StatisticsData.ActionId.SQLITE_SORT;
                        break;

                    case R.string.sql_data_more_export:
                        showExportDialog();
                        break;

                    case R.string.sql_data_more_import:
//                        if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {
//                            return;
//                        }
//
                        showImportDialog();
//                        actionId = StatisticsData.ActionId.SQLITE_IMPORT_CSV;
                        break;
                }

                if (actionId > 0) {
//                    StatisticsData.recordOperationId(actionId);
                }
            }
        });

        bottomSheetDialog.show();
    }

    private void startInformationActivity() {
        Intent intent = SqlCreateTableActivity.newIntent(this, true, mTableName);
        intent.putExtra(SqlCreateTableActivity.DB_NAME, getIntent().getStringExtra(DB_NAME));
        startActivity(intent);
    }

    private void showExportDialog() {

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.table_datas_export, null);

        final Spinner formatSpinner = (Spinner) view.findViewById(R.id.spinner_format);
        final TextView exportHead = (TextView) view.findViewById(R.id.export_head);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.export_head_checkBox);
        LinearLayout layoutPath = (LinearLayout) view.findViewById(R.id.name_path_content);
        mDisplayPathTv = (TextView) view.findViewById(R.id.display_path_tv);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mSelectedPath = path;
        mDisplayPathTv.setText(path);

        final RelativeLayout layoutExportColumn = (RelativeLayout) view.findViewById(R.id.export_column_content);
        final TextView columnTitle = (TextView) view.findViewById(R.id.export_column_title);
        final TextView columnTypes = (TextView) view.findViewById(R.id.column_type);

        layoutPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChoicePath(getString(R.string.sql_create_db_path));
            }
        });

        final String[] export_format;
        if (isFromSql) {
            export_format = getResources().getStringArray(R.array.sql_result_export_format);
        } else {
            export_format = getResources().getStringArray(R.array.sql_export_format);
        }
        final List<String> mDatas = Arrays.asList(export_format);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(mDatas, this);
        formatSpinner.setAdapter(spinnerAdapter);
        formatSpinner.setSelection(0);
        selectFormat = mDatas.get(0);

        final MaterialEditText fileName = (MaterialEditText) view.findViewById(R.id.sql_export_table_name);
        fileName.setText(mTableName);
        fileName.setSelection(0, fileName.getText().toString().length());

        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectFormat = formatSpinner.getSelectedItem().toString();
                if (selectFormat.equals(getResources().getString(R.string.sql_table_export_format_sql))) {
                    exportHead.setTextColor(getResources().getColor(R.color.dialog_text_color2));
                    checkBox.setEnabled(false);

                    columnTitle.setTextColor(getResources().getColor(R.color.dialog_text_color2));
                    columnTypes.setTextColor(getResources().getColor(R.color.dialog_text_color2));
                    layoutExportColumn.setEnabled(false);
                } else {
                    exportHead.setTextColor(getResources().getColor(R.color.maintxt));
                    checkBox.setEnabled(true);

                    columnTitle.setTextColor(getResources().getColor(R.color.maintxt));
                    columnTypes.setTextColor(getResources().getColor(R.color.maintxt));
                    layoutExportColumn.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        resetExportColumn();
        layoutExportColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColumnTypeDialog(columnTypes);
            }
        });

        builder.setTitle(R.string.sql_data_more_export)
                .setView(view)
                .setNeutralButton(R.string.cancel, new SlidingUpDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which) {

                        BottomViewMgr.hideBottomView();
                    }
                })
                .setPositiveButton(R.string.okey, new SlidingUpDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which) {

                        String suffix = "";
                        final int position = mDatas.indexOf(selectFormat);
                        switch (position) {
                            case 0://HTML
                                suffix = ".html";
                                break;
                            case 1://CSV
                                suffix = ".csv";
                                break;
                            case 2://SQL
                                suffix = ".sql";
                                break;
                        }

                        String name = Utils.isFileExist(SqlTabDatasActivity.this, fileName, suffix, mSelectedPath);
                        if (name == null) return;

                        BottomViewMgr.hideBottomView();
                        showExportingDlg();

                        Observable.just(name)
                                .observeOn(Schedulers.newThread())
                                .map(new Func1<String, Integer>() {
                                    @Override
                                    public Integer call(String name) {
                                        int r = SqlConstant.FAIL;
                                        int actionId = -1;

                                        List<String> exportColumns = null;
                                        if (position == 0 || position == 1) {
                                            if (mExportColumns != null && mTitles != null) {
                                                exportColumns = new ArrayList<>();

                                                for (int i = 0; i < mExportColumns.length; i++) {
                                                    if (mExportColumns[i]) {
                                                        exportColumns.add(mTitles.get(i));
                                                    }
                                                }

                                                if (exportColumns.isEmpty() || exportColumns.size() == mTitles.size()) {
                                                    exportColumns = null;
                                                }
                                            }
                                        }

                                        switch (position) {
                                            case 0://HTML
                                                if (isFromSql) {
                                                    r = ExportTool.exportHtmlByResult(SqlTabDatasActivity.this, mSelectedPath, name, checkBox.isChecked(), tablesData, exportColumns);
                                                } else {
                                                    try {
                                                        r = ExportTool.exportHtml(SqlTabDatasActivity.this, mTableName, mSelectedPath, name, columnFields, checkBox.isChecked(), exportColumns);
                                                    } catch (NoSuchFieldException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
//                                                actionId = StatisticsData.ActionId.SQLITE_EXPORT_HTML;
                                                break;

                                            case 1://CSV
                                                if (isFromSql) {
                                                    r = ExportTool.exportCsvByResult(mSelectedPath, name, checkBox.isChecked(), tablesData, exportColumns);
                                                } else {
                                                    try {
                                                        r = ExportTool.exportCsv(mTableName, mSelectedPath, name, columnFields, checkBox.isChecked(), exportColumns);
                                                    } catch (NoSuchFieldException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
//                                                actionId = StatisticsData.ActionId.SQLITE_EXPORT_CSV;
                                                break;

                                            case 2://SQL
                                                try {
                                                    r = ExportTool.exportTable(mTableName, mSelectedPath, name);
                                                } catch (NoSuchFieldException e) {
                                                    e.printStackTrace();
                                                }
//                                                actionId = StatisticsData.ActionId.SQLITE_EXPORT_TABLE;
                                                break;
                                        }

//                                        if (actionId > 0) {
//                                            StatisticsData.recordOperationId(actionId);
//                                        }

                                        return r;
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Integer>() {
                                    @Override
                                    public void call(Integer result) {
                                        hideExportingDlg();

                                        if (result == SqlConstant.SUCCESS) {
                                            ToastManger.showDoneToast(SqlTabDatasActivity.this, R.string.operation_success);
                                        } else {
                                            ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.operation_failed);
                                        }
                                    }
                                });
                    }
                });

        BottomViewMgr.showBottomView(this, builder.create());
        BottomViewMgr.showSoftInput(fileName);

    }

    private boolean[] mExportColumns = null;

    private void resetExportColumn() {
        if (mExportColumns != null) {
            for (int i = 0; i < mExportColumns.length; i++) {
                mExportColumns[i] = true;
            }
        }
    }

    private void showColumnTypeDialog(final TextView columnTypes) {

        String[] array = new String[mTitles.size()];
        mTitles.toArray(array);

        if (mExportColumns == null) {
            mExportColumns = new boolean[mTitles.size()];
            for (int i = 0; i < mExportColumns.length; i++) {
                mExportColumns[i] = true;
            }
        }

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        builder.setTitle(R.string.sql_export_column)
                .setMultiChoiceItems(array, mExportColumns, new SlidingUpDialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which, boolean isChecked) {
                        mExportColumns[which] = isChecked;
                    }
                })
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomView();
                            }
                        })
                .setPositiveButton(R.string.okey,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {

                                int count = 0;
                                for (boolean mExportColumn : mExportColumns) {
                                    if (mExportColumn) {
                                        count++;
                                    }
                                }

                                if (count == 0) {
                                    ToastManger.showSystemErrorToast(SqlTabDatasActivity.this, R.string.sql_select_column_error);
                                    return;
                                }

                                BottomViewMgr.hideBottomView();

                                if (count == mExportColumns.length) {
                                    columnTypes.setText(R.string.all_field);
                                } else {
                                    columnTypes.setText(getResources().getString(R.string.sql_create_column_condition_size)
                                            .replace("&", String.valueOf(count)));
                                }
                            }
                        });

        BottomViewMgr.showBottomView(this, builder.create());
    }

    public void importTabForResult(Activity activity, int code, int title) {
        ChoiceFileActivity.startActivity(activity,getResources().getString(title),false,code);
    }

    private void showImportDialog() {

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.table_datas_import_csv, null);

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.export_head_checkBox);
        LinearLayout layoutPath = (LinearLayout) view.findViewById(R.id.name_path_content);
        mDisplayPathTv = (TextView) view.findViewById(R.id.display_path_tv);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mSelectedPath = path;
        mDisplayPathTv.setText(path);

        final RelativeLayout layoutImportColumn = (RelativeLayout) view.findViewById(R.id.import_column_content);
        final TextView columnTypes = (TextView) view.findViewById(R.id.column_type);

        layoutPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importTabForResult(SqlTabDatasActivity.this, SQL_IMPORT_TAB_PATH_CODE, R.string.sql_import_csv_path);
            }
        });

        resetExportColumn();
        layoutImportColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColumnTypeDialog(columnTypes);
            }
        });

        builder.setTitle(R.string.sql_data_more_import)
                .setView(view)
                .setNeutralButton(R.string.cancel, new SlidingUpDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which) {

                        BottomViewMgr.hideBottomView();
                    }
                })
                .setPositiveButton(R.string.okey, new SlidingUpDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which) {

                        BottomViewMgr.hideBottomView();

                        List<String> importColumns = null;
                        if (mExportColumns != null && mTitles != null) {
                            importColumns = new ArrayList<>();

                            for (int i = 0; i < mExportColumns.length; i++) {
                                if (mExportColumns[i]) {
                                    importColumns.add(mTitles.get(i));
                                }
                            }

                            if (importColumns.isEmpty()) {
                                importColumns = null;
                            }
                        }

                        int r;
                        try {
                            r = ExportTool.isColumnCountSame(mTableName, mSelectedPath);
                            switch (r) {
                                case SqlConstant.SUCCESS:
                                    showImportingDlg();
                                    Observable.just(importColumns)
                                            .observeOn(Schedulers.newThread())
                                            .map(new Func1<List<String>, Integer>() {
                                                @Override
                                                public Integer call(List<String> importColumns) {
                                                    try {
                                                        return ExportTool.importCsv(mTableName, mSelectedPath, !checkBox.isChecked(), importColumns);
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
                                                    hideImportingDlg();

                                                    if (result == SqlConstant.SUCCESS) {
                                                        ToastManger.showDoneToast(SqlTabDatasActivity.this, R.string.operation_success);
                                                        getData(null, true, false, null, null);

                                                    } else {
                                                        ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.operation_failed);
                                                    }
                                                }
                                            });
                                    break;

                                case SqlConstant.FAIL:
                                    ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.operation_failed);
                                    break;

                                case SqlConstant.IMPORT_COUNT_DIFFERENT:
                                    showImportWarnDialog(!checkBox.isChecked(), importColumns);
                                    break;
                            }
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                    }
                });

        BottomViewMgr.showBottomView(this, builder.create());
    }

    private void showImportWarnDialog(final boolean skipFirstLine, final List<String> importColumns) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                showImportingDlg();
                Observable.just(0)
                        .observeOn(Schedulers.newThread())
                        .map(new Func1<Integer, Integer>() {
                            @Override
                            public Integer call(Integer name) {
                                try {
                                    return ExportTool.importCsv(mTableName, mSelectedPath, skipFirstLine, importColumns);
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
                                hideImportingDlg();

                                if (result == SqlConstant.SUCCESS) {
                                    ToastManger.showDoneToast(SqlTabDatasActivity.this, R.string.operation_success);
                                    getData(null, true, false, null, null);

                                } else {
                                    ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.operation_failed);
                                }
                            }
                        });
            }
        };
        FeViewUtils.createMsgDialog(SqlTabDatasActivity.this, R.string.column_count_not_same, R.string.cancel, R.string.okey, runnable);
    }

    private void startChoicePath(String title) {
        ChoiceFileActivity.startActivity(SqlTabDatasActivity.this,title,true,SQL_CREATE_TAB_PATH_CODE);
    }

    private void clearTabData() {

        if (tablesData != null && tablesData.size() > 0) {
            int r = 0;
            try {
                r = SQLManager.getSQLHelper().deleteAllDataFromTable(mTableName);
                if (r == SqlConstant.SUCCESS) {
                    tablesData = SQLManager.getSQLHelper().getTableDataList(mTableName, 0, 0, null, true);
                    initData();
                    initTitleWidthAndHeader(false);
                    ToastManger.showDoneToast(this, R.string.operation_success);
                } else {
                    ToastManger.showErrorToast(this, R.string.operation_failed);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

        } else {
            ToastManger.showNormalToast(this, R.string.empty_data_table);
        }
    }

    private void showSureDeleteDialog(final boolean isAll) {
        FeDialog.Builder builder = new FeDialog.Builder(this);

        if (isAll) {
            builder.setMessage(R.string.sure_clear_table_data);
        } else {
            builder.setMessage(R.string.sure_delete_row_data);
        }

        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BottomViewMgr.hideBottomView();
            }
        })
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BottomViewMgr.hideBottomView();

                        if (isAll) {
                            clearTabData();
                        } else {
                            Map<String, TableDataField> map = tablesData.get(mClickPosition);
                            SQLManager.getSQLHelper(SqlTabDatasActivity.this).deleteTableData(mTableName, map);
                            tablesData.remove(map);
                            mTabDatasAdapter.clearEditType();
                            mToolBar.getMenu().clear();
                            mToolBar.inflateMenu(R.menu.sql_table_data);
                            isEdit = false;
                            EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.refreshTable, null));
                        }

                    }
                });

        BottomViewMgr.showBottomView(builder.create());
    }

    private void showFilterDialog() {
        View view = View.inflate(this, R.layout.sql_tabdatas_filter_dialog, null);

        final MaterialEditText filter_value = (MaterialEditText) view.findViewById(R.id.filter_value);
        final Spinner filterSpinner = (Spinner) view.findViewById(R.id.spinner_filter);

        List<String> mDatas = new ArrayList<>();
        mDatas.add(getResources().getString(R.string.all_field));
        for (int i = 0; i < columnFields.size(); i++) {
            mDatas.add(columnFields.get(i).getFieldName());
        }

        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(mDatas, this);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setSelection(0);

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        builder.setTitle(R.string.filter_title)
                .setView(view)
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomView();
                            }
                        })
                .setPositiveButton(R.string.okey, new SlidingUpDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(SlidingUpDialogInterface dialog, int which) {

                        BottomViewMgr.hideSoftwareInput(SqlTabDatasActivity.this, filter_value);
                        BottomViewMgr.hideBottomView();

                        String selectColum = (String) filterSpinner.getSelectedItem();

                        startFilterActivity(selectColum, filter_value);
                    }
                });

        BottomViewMgr.showBottomView(this, builder.create());

    }

    private void startFilterActivity(String selectColum, MaterialEditText filter_value) {
        Intent intent = new Intent(SqlTabDatasActivity.this, SqlTabDatasActivity.class);
        intent.putExtra(SqlTabDatasActivity.TABLE_KEY, mTableName);
        intent.putExtra(SqlTabDatasActivity.DB_NAME, getIntent().getStringExtra(DB_NAME));
        intent.putExtra(SqlTabDatasActivity.DATA_SOURCE, SqlConstant.TABLE_DATAS_FILTER);
        intent.putExtra(SqlTabDatasActivity.FILTER_COLUM, selectColum);
        intent.putExtra(SqlTabDatasActivity.FILTER_KEY, filter_value.getText().toString());
        startActivity(intent);
    }

    private void initIntent() {
        if (getIntent() != null) {
            mTableName = getIntent().getStringExtra(TABLE_KEY);
            mSqlStr = getIntent().getStringExtra(SQL_STR);
            dataSource = getIntent().getIntExtra(DATA_SOURCE, SqlConstant.TABLE_DATAS_NORMAL);
        }
    }

    //初始化每列的宽度以及header字段
    private void initTitleWidthAndHeader(boolean hasData) {

        if (mWidthList == null) {
            mWidthList = new ArrayList<>();
        } else {
            mWidthList.clear();
        }
        mTabHeader.removeAllViews();
        mTitles = new ArrayList<>();

        if (hasData) {
            getColumnWith(mWidthList);
        }

        TableRow myTabRow = new TableRow(this);
        String name;
        RightBorderTextView borderTextView;
        for (int col = 0; col < columnFields.size(); col++) {

            if (col == columnFields.size() - 1) {
                borderTextView = new RightBorderTextView(this, true);
            } else {
                borderTextView = new RightBorderTextView(this, false);
            }

            borderTextView.setTextSize(12);
            if (hasData) {
                borderTextView.setWidth(Math.min(mWidthList.get(col) + FeViewUtils.dpToPx(32), 600));

            } else {
                borderTextView.setMaxWidth(FeViewUtils.dpToPx(600));
            }
            borderTextView.setTextColor(getResources().getColor(R.color.table_head_text_color));
            borderTextView.setSingleLine(true);
            borderTextView.setPadding(FeViewUtils.dpToPx(12), 10, FeViewUtils.dpToPx(12), 10);
            name = columnFields.get(col).getFieldName();
            borderTextView.setText(name);

            myTabRow.addView(borderTextView);
            mTitles.add(name);
        }

        mTabHeader.addView(myTabRow);
    }

    private void initData() {
        mTabDatasAdapter = null;
        boolean isVertical = true;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isVertical = false;
            mRecycleView.setMinimumWidth(FeViewUtils.getScreenHeight(this));
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            isVertical = true;
            mRecycleView.setMinimumWidth(FeViewUtils.getScreenWidth(this));
        }
        switch (dataSource) {
            case SqlConstant.TABLE_DATAS_NORMAL:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_NORMAL, isVertical);
                break;
            case SqlConstant.TABLE_DATAS_SQL:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_SQL, true);
                break;
            case SqlConstant.TABLE_DATAS_FILTER:
                mTabDatasAdapter = new TabDatasAdapter(mWidthList, mTitles, this, SqlConstant.TABLE_DATAS_FILTER, true);
                break;
        }
        mRecycleView.setAdapter(mTabDatasAdapter);
        mTabDatasAdapter.setDatas(tablesData);
        if (mClickPosition > 0) {
            mLinearLayoutManager.scrollToPositionWithOffset(mClickPosition, FeViewUtils.dpToPx(55));
            mTabDatasAdapter.setClickPosition(mClickPosition);
            mTabDatasAdapter.notifyDataSetChanged();
        }
    }

    private void getColumnWith(List<Integer> mWidthList) {

        mWidthList.clear();
        String columnValue;
        ColumnField columnField;
        int topWidth = 0, contentWidth;
        int size = (tablesData != null && tablesData.size() > 1) ? 2 : 1;
        for (int j = 0; j < size; j++) {

            if (tablesData != null && tablesData.size() > 0) {

                String fieldType;
                Map<String, TableDataField> rowData = tablesData.get(j);

                for (int i = 0; i < columnFields.size(); i++) {

                    columnField = columnFields.get(i);
                    topWidth = Utils.MeasureString(this, columnField.getFieldName(), 15, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).x;
                    fieldType = columnField.getFieldType();
                    if (i == 0 && (fieldType != null) && (("INT").equals(fieldType) || ("INTEGER").equals(fieldType))) {
                        mWidthList.add(FeViewUtils.dpToPx(42));
                    } else {

                        TableDataField tableDataField = rowData.get(columnField.getFieldName());
                        if (tableDataField != null) {
                            columnValue = tableDataField.getFieldData();
                            if (columnValue == null || columnValue.equals("")) {
                                mWidthList.add(topWidth);
                            } else {
                                contentWidth = Utils.MeasureString(this, columnValue, 15, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).x;
                                if (j == 1) {
                                    int width = Math.max(mWidthList.get(i), contentWidth);
                                    mWidthList.set(i, width);
                                } else {
                                    mWidthList.add(Math.max(topWidth, contentWidth));
                                }
                            }
                        }

                    }
                }
            } else {
                mWidthList.add(topWidth);
            }
        }

    }

    private void getCloumWidth(List<Integer> widthList) {

        widthList.clear();

        for (int j = 0; j < columnFields.size(); j++) {
            widthList.add(Utils.MeasureString(this, columnFields.get(j).getFieldName(), 15, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).x);
        }

        String value;
        int maxWidth, width = 0;
        int tableDatasSize = tablesData.size();
        int columnSize = columnFields.size();
        for (int j = 0; j < columnSize; j++) {

            for (int i = 0; i < tableDatasSize; i++) {
                value = tablesData.get(i).get(columnFields.get(j).getFieldName()).getFieldData();
                width = Utils.MeasureString(this, value, 15, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).x;
            }

            widthList.set(j, Math.max(width, widthList.get(j)));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ViewEvent event) {

        ViewEvent.EvenType type = event.getType();
        Bundle args = event.getArgs();
        switch (type) {
            case sqlTabDataItemClick:

                if (dataSource == SqlConstant.TABLE_DATAS_NORMAL) {
                    mClickPosition = args.getInt(ViewEvent.Keys.SQL_TAB_ITEM_CLICK);
                    //TODO  更新toolbar 开始进入编辑状态

                    isEdit = true;
                    mToolBar.setBackgroundColor(getResources().getColor(R.color.fab_bg));
                    mToolBar.setTitle(R.string.editing);
                    mToolBar.setSubtitle(null);
                    mToolBar.getMenu().clear();
                    if (mTitles.contains(SQLHelper.ROW_ID)) {
                        mToolBar.inflateMenu(R.menu.sql_table_unable_operation);
                    } else {
                        mToolBar.inflateMenu(R.menu.sql_table_operation);
                    }
                    mTabDatasAdapter.notifyDataSetChanged();
                }
                break;
            case sqlTabDataFilterItemClick:

                if (dataSource == SqlConstant.TABLE_DATAS_FILTER) {
                    mClickPosition = args.getInt(ViewEvent.Keys.SQL_TAB_ITEM_CLICK);

                    isEdit = true;
                    mToolBar.getMenu().clear();
                    if (mTitles.contains(SQLHelper.ROW_ID)) {
                        mToolBar.inflateMenu(R.menu.sql_table_unable_operation);
                    } else {
                        mToolBar.inflateMenu(R.menu.sql_table_operation);
                    }
                    mTabDatasAdapter.notifyDataSetChanged();
                }
                break;
            case hideLoading:
                mTabDatasAdapter.setDatas(tablesData);
                hideLoadingDlg();
                break;
            case refreshTableData:
                if (tablesData == null || tablesData.size() == 0) {
                    tablesData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getTableDataList(mTableName, 0, 0, null, true);
                    initTitleWidthAndHeader(true);
                    initData();
                } else {

                    initNullData(columnFields);
                    Map<String, TableDataField> newData = SQLManager.getSQLHelper(SqlTabDatasActivity.this).selectOneDataFromTable(mTableName, columnFields);
                    if (newData != null) {
                        int position = tablesData.size();
                        tablesData.add(position, newData);
                        mTabDatasAdapter.notifyItemInserted(position);
                        if (position > 20) {
                            mLinearLayoutManager.scrollToPositionWithOffset(position, FeViewUtils.dpToPx(55));
                        }

                        //添加完以后记得刷新集合中的数据，一免对下次的更新造成影响，更新也要刷新，以免对添加造成影响
                        for (int i = 0; i < columnFields.size(); i++) {
                            columnFields.get(i).setColumnValue(null);
                        }
                    }
                }
                break;
            case sqlSortData:
                BottomViewMgr.hideBottomView();
                tablesData = null;
                String sortName = args.getString(ViewEvent.Keys.SQL_SORT);
                boolean isDesc = args.getBoolean(ViewEvent.Keys.SQL_SORT_DESC_ASC);
                getData(sortName, isDesc, false, null, null);
                break;
            case controlAdMob:
                controlAdMob(args);
                break;
            case isEditTableItem:
                hasEditDate = true;
                break;
            case refreshTable:
                if (dataSource == SqlConstant.TABLE_DATAS_NORMAL) {
                    tablesData = null;
                    getData(null, true, false, null, null);
                }
                break;
        }
    }

    private void controlAdMob(Bundle args) {
        boolean show = args.getBoolean(ViewEvent.Keys.ACTION);
        boolean isInAppSkin = args.getBoolean(ViewEvent.Keys.REFRESH_TYPE);
        if (show) {
            if (isInAppSkin) {
                addGoogleAdMobInApp();
                return;
            }
            adMobView.setupGoogleAdMob();
        } else {
            adMobView.hideAdMob();
        }
    }

    public void addGoogleAdMobInApp() {
        if (PermissionUtils.checkHasPermission(this, PermissionUtils.READ_PHONE_STATE_PER[0])) {
            adMobView.loadAdMob();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                addDataOrUpdateToTab(true, false);
//                StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_ADD_DATA);
                break;
        }
    }

    private void addDataOrUpdateToTab(final boolean isAdd, boolean isBuyData) {

        List<String> autoColumnList = new ArrayList<>();
        final List<ColumnField> insertColumnList = filterAutoIncColum(mTableName, columnFields, autoColumnList);

        View view = View.inflate(this, R.layout.bottom_menu_content, null);
        View dividerLine = view.findViewById(R.id.dividerLine);
        dividerLine.setVisibility(View.VISIBLE);
        RecyclerView itemList = (RecyclerView) view.findViewById(R.id.bottom_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        final AddAndUpdateAdapter addAndUpdateAdapter;

        if (isBuyData) {

            Map<String, TableDataField> map = null;
            if (mClickPosition >= 0 && mClickPosition < tablesData.size()) {
                map = tablesData.get(mClickPosition);//传入原始的数据
            }
            addAndUpdateAdapter = new AddAndUpdateAdapter(this, columnFields, true, map, autoColumnList);

        } else {

            if (isAdd) {
                addAndUpdateAdapter = new AddAndUpdateAdapter(this, columnFields, isAdd, null, autoColumnList);
            } else {
                Map<String, TableDataField> map = null;
                if (mClickPosition >= 0 && mClickPosition < tablesData.size()) {
                    map = tablesData.get(mClickPosition);//传入原始的数据
                }
                addAndUpdateAdapter = new AddAndUpdateAdapter(this, columnFields, isAdd, map, autoColumnList);
            }
        }

        itemList.setAdapter(addAndUpdateAdapter);
        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
        int titleRes = isAdd ? R.string.sql_add_column_data : R.string.sql_update_column_data;
        builder.setTitle(titleRes)
                .setView(view)
                .setNeutralButton(R.string.cancel,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {
                                BottomViewMgr.hideBottomView();
                            }
                        })
                .setPositiveButton(R.string.okey,
                        new SlidingUpDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(SlidingUpDialogInterface dialog, int which) {

                                tmpEditNewData.clear();
//                                if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {//TODO
//                                    for (ColumnField v : columnFields) {
//                                        tmpEditNewData.add(new ColumnField(v));
//                                    }
//
//                                    tmpEditOldClickPosition = mClickPosition;
//                                    clearCache();
//                                    return;
//                                }

                                if (isAdd) {

                                    initNullData(insertColumnList);//此方法用以初始化非空字段的添加问题
                                    SQLManager.getSQLHelper(SqlTabDatasActivity.this).insertData2(mTableName, insertColumnList);
                                    EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.refreshTableData, null));
                                } else {
                                    final Map<String, TableDataField> map = tablesData.get(mClickPosition);

                                    boolean isAllNull = initNullDataForUpdate(insertColumnList, map);
                                    if (!isAllNull) {
                                        SQLManager.getSQLHelper(SqlTabDatasActivity.this).updateTableData(mTableName, map, insertColumnList);
                                    } else {
                                        ToastManger.showErrorToast(SqlTabDatasActivity.this, R.string.no_data_change);
                                    }

                                    String conditionName;
                                    for (int i = 0; i < mTitles.size(); i++) {
                                        conditionName = columnFields.get(i).getFieldName();
                                        if (columnFields.get(i).getColumnValue() != null) {
                                            map.get(conditionName).setFieldData(columnFields.get(i).getColumnValue());
                                        }
                                        mTabDatasAdapter.notifyItemChanged(mClickPosition);
                                    }

                                    if (isEdit) {
                                        clearEditMode();
                                    }

                                    EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.refreshTable, null));
                                }

                                BottomViewMgr.hideBottomView();
                            }
                        });
        if (isAdd) {
            builder.setNeutralButton(R.string.sql_create_tab_add_goon,
                    new SlidingUpDialogInterface.OnClickListener() {
                        @Override
                        public void onClick(SlidingUpDialogInterface dialog, int which) {

                            tmpEditNewData.clear();
//                            if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {
//                                for (ColumnField v : columnFields) {
//                                    tmpEditNewData.add(new ColumnField(v));
//                                }
//
//                                tmpEditOldClickPosition = mClickPosition;
//                                clearCache();
//                                return;
//                            }
//
//                            StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_ADD_DATA);

                            BottomViewMgr.hideBottomView();
                            //TODO 添加数据
                            initNullData(columnFields);//此方法用以初始化非空字段的添加问题，但是一定要去除自增的列才会正常
                            SQLManager.getSQLHelper(SqlTabDatasActivity.this).insertData2(mTableName, columnFields);
                            EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.refreshTableData, null));
                            addDataOrUpdateToTab(true, false);
                        }
                    });
            builder.setNegativeButton(R.string.cancel, new SlidingUpDialogInterface.OnClickListener() {
                @Override
                public void onClick(SlidingUpDialogInterface dialog, int which) {
                    BottomViewMgr.hideBottomView();
                }
            });
        }

        addOrUpdateDialog = builder.create();
        BottomViewMgr.showBottomView(this, addOrUpdateDialog);
        BottomViewMgr.getMgr().getBottomView().setDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //刷新数据，以免对后续操作造成影响
                clearCache();
            }
        });
        BottomViewMgr.getMgr().getBottomView().setKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (hasEditDate) {
                        showSaveUpdateDialog();
                        return true;
                    } else {
                        return false;
                    }

                }
                return false;
            }
        });
    }

    private void clearCache() {
        for (int i = 0; i < columnFields.size(); i++) {
            columnFields.get(i).setColumnValue(null);
        }
    }


    private List<ColumnField> filterAutoIncColum(String tableName, List<ColumnField> insertColumList, List<String> autoColumName) {

        //获取字段中包括自增的信息
        List<ColumnField> autoColumList = SQLManager.getSQLHelper(SqlTabDatasActivity.this).getAutoincrementColumn(tableName);

        if (autoColumList.size() > 0) {
            for (int i = 0; i < autoColumList.size(); i++) {
                ColumnField columnField = autoColumList.get(i);
                if (columnField.isAutoIncr()) {
                    autoColumName.add(columnField.getFieldName());
                }
            }
        }

        List<ColumnField> nowColumns = new ArrayList<>();
        nowColumns.addAll(insertColumList);

        //过滤掉包含自增的字段进行查找
        for (int i = 0; i < insertColumList.size(); i++) {
            ColumnField columnField = insertColumList.get(i);
            if (autoColumName.contains(columnField.getFieldName())) {
                Log.e("abc", "=====自增的键名=====" + columnField.getFieldName());
                nowColumns.remove(columnField);
            }
        }

        return nowColumns;
    }


    private void initNullData(List<ColumnField> columnFields) {

        for (int i = 0; i < columnFields.size(); i++) {

            if ((columnFields.get(i).getNotNull() == 1) && TextUtils.isEmpty(columnFields.get(i).getColumnValue())) {
                columnFields.get(i).setColumnValue("");
            }
        }
    }

    private boolean initNullDataForUpdate(List<ColumnField> columnFields, Map<String, TableDataField> map) {

        boolean isAllNull = true;

        for (int i = 0; i < columnFields.size(); i++) {

            if ((columnFields.get(i).getColumnValue() == null)) {
                String data = map.get(columnFields.get(i).getFieldName()).getFieldData();
                columnFields.get(i).setColumnValue(data);
                if (data != null) {
                    isAllNull = false;
                }
            } else {
                isAllNull = false;
            }
        }

        return isAllNull;
    }

    private void showSortDialog() {
        mSortDialog = new BottomSheetDialog(this);
        View view = View.inflate(this, R.layout.bottom_menu_content, null);
        RecyclerView itemList = (RecyclerView) view.findViewById(R.id.bottom_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        SortTypeAdapter adapter = new SortTypeAdapter(this, columnFields);
        itemList.setAdapter(adapter);

        mSortDialog.setContentView(view);
        mSortDialog.show();
    }

    public BottomSheetDialog getSortDialog() {
        return mSortDialog;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (isEdit) {
                clearEditMode();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private FeDialog mSureSaveDialog;

    private void showSaveUpdateDialog() {

        if (mSureSaveDialog == null) {
            FeDialog.Builder builder = new FeDialog.Builder(this);
            builder.setMessage(R.string.exit_save_msg)
                    .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BottomViewMgr.hideBottomView();
                            tmpEditNewData.clear();
//                            if (Utils.isFunNeedBuy(SqlTabDatasActivity.this)) {//TODO
//                                for (ColumnField v : columnFields) {
//                                    tmpEditNewData.add(new ColumnField(v));
//                                }
//
//                                tmpEditOldClickPosition = mClickPosition;
//                                clearCache();
//                            } else {
//                                addOrUpdateDialog.getmPositiveButton().performClick();
//                            }
                        }
                    })
                    .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BottomViewMgr.hideBottomView();
                        }
                    });
            mSureSaveDialog = builder.create();
        }

        if (!mSureSaveDialog.isShowing()) {
            BottomViewMgr.showBottomView(mSureSaveDialog);
        }

        BottomViewMgr.getMgr().getBottomView().setDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hasEditDate = false;
            }
        });
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
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        StatisticsData.recordOperationId(StatisticsData.READ_PHONE_STATE_PER_ACCEPT);
                    checkReadPhoneState(grantResults[0]);
//                    } else {
//                        StatisticsData.recordOperationId(StatisticsData.READ_PHONE_STATE_PER_REJECT);
//                    }
                    return;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkReadPhoneState(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
//            Utils.setUniqueDeviceId(this);
//            ServerMsg.getServerInfo(this, false);
//            PurchaseVerify.purchaseServerInfo(this);
//            checkPermissionForAdMob(grantResult);
        } else {
//            if (!FeFunBase.hideAdMob()) {
//                setupAdMobLayout(View.VISIBLE, 50);
//            }
        }
    }

    private void checkPermissionForAdMob(int grantResult) {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            adMobView.setupGoogleAdMob();
        } else {
//            if (!FeFunBase.hideAdMob()) {
            adMobView.setupAdMobLayout(View.VISIBLE, 50);
//            }
        }
    }

    private void clearEditMode() {
        isEdit = false;
        mTabDatasAdapter.clearEditType();
        mToolBar.getMenu().clear();

        mClickPosition = -1;
        setToolBarName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == SQL_IMPORT_TAB_PATH_CODE) {

                mSelectedPath = data.getStringExtra(DbTablesActivity.TAB_PATH);
                mDisplayPathTv.setText(mSelectedPath);

            } else if (requestCode == SQL_CREATE_TAB_PATH_CODE) {

                mSelectedPath = data.getStringExtra(Constant.CONFIRM_PATH);
                mDisplayPathTv.setText(mSelectedPath);

            } else {
                if (resultHandler != null) {
                    resultHandler.handleResult(requestCode, resultCode, data);

                    if (tmpEditNewData != null && tmpEditNewData.size() > 0) {
                        for (int i = 0; i < columnFields.size(); i++) {
                            try {
                                columnFields.get(i).setColumnValue(tmpEditNewData.get(i).getColumnValue());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        for (ColumnField v : columnFields) {
                            Log.e("sg", "------tmp--------" + v.getColumnValue());
                        }
                        tmpEditNewData.clear();

                        addDataOrUpdateToTab(tmpEditOldClickPosition < 0, true);
                        tmpEditOldClickPosition = -1;
                    }
                }
            }
        }
    }

    private Subscription showLoadingDlgDelay() {
        if (mLoadingTip == null) {
            mLoadingTip = FeViewUtils.createLoadingDialog(SqlTabDatasActivity.this, getString(R.string.loading));
        }

        Subscription subscription = Observable.timer(80, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (tablesData == null) {
                            mLoadingTip.show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        return subscription;
    }

    private void hideLoadingDlgDelay() {
        if (mLoadingTip != null && mLoadingTip.isShowing()) {
            Observable.timer(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            mLoadingTip.dismiss();
                        }
                    });
        }
    }

    private void hideLoadingDlg() {
        if (mLoadingTip != null && mLoadingTip.isShowing()) {
            mLoadingTip.dismiss();
        }
    }

    private void showImportingDlg() {
        if (mImportingTip == null) {
            mImportingTip = FeViewUtils.createLoadingDialog(SqlTabDatasActivity.this, getString(R.string.importing));
        }
        mImportingTip.show();
    }

    private void showExportingDlg() {
        if (mExportingTip == null) {
            mExportingTip = FeViewUtils.createLoadingDialog(SqlTabDatasActivity.this, getString(R.string.exporting));
        }
        mExportingTip.show();
    }

    private void hideExportingDlg() {
        if (mExportingTip != null) {
            mExportingTip.dismiss();
        }
    }

    private void hideImportingDlg() {
        if (mImportingTip != null) {
            mImportingTip.dismiss();
        }
    }

}
