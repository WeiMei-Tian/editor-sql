package com.gmobile.sqliteeditor.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmobile.library.base.view.customview.bottomview.BottomViewMgr;
import com.gmobile.library.base.view.customview.dialog.FeDialog;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialog;
import com.gmobile.library.base.view.operation.slidingupview.SlidingUpDialogInterface;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.ColumnConditionAdapter;
import com.gmobile.sqliteeditor.adapter.CustomSpinnerAdapter;
import com.gmobile.sqliteeditor.adapter.SelectTabOrColumnDialog;
import com.gmobile.sqliteeditor.adapter.SqlCreateViewAdapter;
import com.gmobile.sqliteeditor.adapter.listener.SqlActivityResultHandler;
import com.gmobile.sqliteeditor.assistant.FeViewUtils;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.constant.SqlConstant;
import com.gmobile.sqliteeditor.model.bean.sqlite.NewColumnField;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;
import com.gmobile.sqliteeditor.widget.RightBorderTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by sg on 2016/9/21.
 */
public class SqlCreateTableActivity extends SqlBaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecycleView;
    private TableLayout mTabHeader;
    public static final String TABLE_NAME_KEY = "tableName";
    public static final String TABLE_NAMES_KEY = "tableNames";
    public static final String IS_INFORMATION = "is_information";
    private String mTableName = "";
    private String mColumnDataType = "INTEGER";
    private String mSelectedTab;
    private TextView mConditionTypeTxt;
    private LinearLayout mInputDefaultLayout;
    private List<String> mConditionList;
    private SqlCreateViewAdapter mAdapter;
    private SwipeRefreshLayout mEmptyRefreshLayout;
    private List<Integer> conditionSelected;
    private SqlActivityResultHandler resultHandler;

    private static final int SCROLL_DISTANCE = 400;
    public static final String DB_NAME = "db_name";


    private List<NewColumnField> mNewColumnFieldList = new LinkedList<>();
    private int mClickPosition = -1;
    private boolean isEdit;
    private boolean isInformation = false;

    private NewColumnField mTmpEditColumnField;
    private NewColumnField mTmpEditOldColumnField;


    public static Intent newIntent(Context context, boolean isInformation, String mTableName) {
        Intent intent = new Intent(context, SqlCreateTableActivity.class);
        intent.putExtra(IS_INFORMATION, isInformation);
        intent.putExtra(TABLE_NAME_KEY, mTableName);
        return intent;
    }

    @Override
    protected int setViews() {
        return R.layout.sql_create_table_layout;
    }

    @Override
    protected void initIntent(Intent intent) {
        if (intent != null) {
            mTableName = getIntent().getStringExtra(TABLE_NAME_KEY);
            isInformation = getIntent().getBooleanExtra(IS_INFORMATION, false);
        }
    }

    @Override
    void findViews() {
        EventBus.getDefault().register(this);
        mRecycleView = (RecyclerView) findViewById(R.id.sql_tabs_recycleview);
        mTabHeader = (TableLayout) findViewById(R.id.sql_tabview_header_tab);
        mEmptyRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.empty_view_container);
        setRefreshLayoutImpl(mEmptyRefreshLayout);
    }

    @Override
    String getToolbarTitle() {
        return mTableName;
    }

    @Override
    int getToolBarMenu() {
        return 0;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        mToolbar.setSubtitle(getIntent().getStringExtra(DB_NAME));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewColumnFieldList == null || mNewColumnFieldList.size() == 0) {
                    showExitTipDialog();
                } else {
                    finish();
                }
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_column_edit:
                        addColumnToTab(mNewColumnFieldList.get(mClickPosition), false);
                        break;

                    case R.id.action_column_delete:
//                        if (Utils.isFunNeedBuy(SqlCreateTableActivity.this)) {
//                            return true;
//                        }

                        showSureDeleteDialog();
                        break;

                }
                return true;
            }
        });
        initToolBarButton();
    }

    private void initToolBarButton() {
        resultHandler = new SqlActivityResultHandler(this);

        if (isInformation) {
            setToolBarButtonGone();
        } else {
            mToolbar.getMenu().clear();
            mToolBarButton.setText(getResources().getString(R.string.okey));
            mToolBarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(SqlCreateTableActivity.this, SqlTabDatasActivity.class);
//                    intent.putExtra(SqlTabDatasActivity.TABLE_KEY, mTableName);
//                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void initData() {

        if (!isInformation) {
            setFloatingActionButtonVisible();
        }

        mNewColumnFieldList = SQLManager.getSQLHelper(this).getNewColumnFieldList(mTableName);
        if (mNewColumnFieldList != null && mNewColumnFieldList.size() > 0) {
            openResult();
        } else {
            mEmptyRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showSureDeleteDialog() {
        FeDialog.Builder builder = new FeDialog.Builder(this);

        builder.setMessage(R.string.sure_delete_column_data)
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

                        Bundle bundle = SQLManager.getSQLHelper(SqlCreateTableActivity.this).processColumn(mTableName,
                                mNewColumnFieldList.get(mClickPosition).getFieldName(), null);

                        int r = bundle.getInt(SqlConstant.RESULT);

                        switch (r) {
                            case SqlConstant.SUCCESS:
                                mNewColumnFieldList.remove(mClickPosition);
                                mAdapter.notifyItemRemoved(mClickPosition);
                                break;

                            case SqlConstant.FAIL_DELETE_COLUMN_EMPTY:
                                ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.delete_column_error1);
                                break;

                            case SqlConstant.FAIL:
                                if (bundle.containsKey(SqlConstant.ERROR_MSG)){
                                    ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, Integer.parseInt(bundle.getString(SqlConstant.ERROR_MSG)));
                                } else {
                                    ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.delete_column_error2);
                                }
                                break;

                            case SqlConstant.FAIL_EDIT_COLUMN_FK:
                                ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.delete_column_error3);
                                break;
                        }
                        if (isEdit) {
                            clearEditMode();
                        }
                    }
                });

        BottomViewMgr.showBottomView(builder.create());
    }

    private void clearEditMode() {
        isEdit = false;
        mToolbar.getMenu().clear();
        mClickPosition = -1;
        mAdapter.clearEditType();
        if (mConditionList != null) {
            mConditionList.clear();
        }
    }

    private void openResult() {

        mEmptyRefreshLayout.setVisibility(View.GONE);
        mTabHeader.removeAllViews();
        String[] array = getResources().getStringArray(R.array.add_column_result2);
        TableRow myTabRow = new TableRow(mContext);
        String columnName;
        for (int i = 0; i < array.length; i++) {

            columnName = array[i];
            RightBorderTextView borderTextView = new RightBorderTextView(this, false);
            borderTextView.setSingleLine(true);
            borderTextView.setPadding(25, 10, 25, 10);
            if ((i > 0 && i <= 5) || i > 9) {
                borderTextView.setWidth(FeViewUtils.dpToPx(85));
            } else {
                borderTextView.setWidth(FeViewUtils.dpToPx(120));
            }
            borderTextView.setText(columnName);
            myTabRow.addView(borderTextView);
            borderTextView.setTextColor(getResources().getColor(R.color.table_head_text_color));
        }
        mTabHeader.setBackgroundColor(getResources().getColor(R.color.table_head_bg));
        mTabHeader.addView(myTabRow);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SqlCreateViewAdapter(mNewColumnFieldList, array, this, isInformation);
        mRecycleView.setAdapter(mAdapter);

    }

    private void setRefreshLayoutImpl(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setDistanceToTriggerSync(SCROLL_DISTANCE);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    @Override
    protected void fabBtnClick() {

        addColumnToTab(null, true);
//        StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_ADD_COLUMN);
    }

    private void addColumnToTab(final NewColumnField oldField, final boolean addNew) {
        try {
            View view = View.inflate(this, R.layout.sql_create_tab_add_cloum, null);

            final MaterialEditText cloum_name_input = (MaterialEditText) view.findViewById(R.id.cloum_name);
            final MaterialEditText string_length = (MaterialEditText) view.findViewById(R.id.input_string_length);
            final CheckBox primary_checkbox = (CheckBox) view.findViewById(R.id.primary_key_checkbox);
            final MaterialEditText default_value_input = (MaterialEditText) view.findViewById(R.id.et_default_value);
            final CheckBox primary_autoincrement_checkbox = (CheckBox) view.findViewById(R.id.primaty_autoincrement_checkbox);
            final Spinner typeSpinner = (Spinner) view.findViewById(R.id.spinner_type);

            List<String> mDatas = Arrays.asList(getResources().getStringArray(R.array.cloum_type));
            CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(mDatas, this);
            typeSpinner.setAdapter(spinnerAdapter);
            typeSpinner.setSelection(7);
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mColumnDataType = (String) typeSpinner.getSelectedItem();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            final TextView fk_field_title = (TextView) view.findViewById(R.id.fk_field_title);
            final TextView fk_field = (TextView) view.findViewById(R.id.fk_field);
            final TextView fk_table = (TextView) view.findViewById(R.id.fk_table);
            RelativeLayout condition_type_content = (RelativeLayout) view.findViewById(R.id.condition_type_content);
            final RelativeLayout primary_key_auto_content = (RelativeLayout) view.findViewById(R.id.primaty_autoincrement_content);
            final RelativeLayout cloumn_names = (RelativeLayout) view.findViewById(R.id.rl_select_cloumn_layout);
            final RelativeLayout table_names = (RelativeLayout) view.findViewById(R.id.rl_select_tab_layout);

            mConditionTypeTxt = (TextView) view.findViewById(R.id.condition_type);
            mInputDefaultLayout = (LinearLayout) view.findViewById(R.id.input_default_value_content);
            conditionSelected = new ArrayList<>();

            cloumn_names.setEnabled(false);
            cloumn_names.setClickable(false);
            fk_field_title.setTextColor(getResources().getColor(R.color.dialog_text_color2));

            primary_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && SqlConstant.INTEGER_TYPE.equals(mColumnDataType)) {
                        primary_key_auto_content.setVisibility(View.VISIBLE);
                    } else {
                        primary_key_auto_content.setVisibility(View.GONE);
                    }
                }
            });

            condition_type_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConditionTypeDialog(mConditionTypeTxt);
                }
            });

            table_names.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> cloumns = (List<String>) getIntent().getSerializableExtra(TABLE_NAMES_KEY);
                    showSelectColumsDialog(cloumns, fk_table, cloumn_names, fk_field_title, fk_field);
                }
            });

            cloumn_names.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("abc", "=====name=====" + mSelectedTab);
                    List<String> cloumns = SQLManager.getSQLHelper(SqlCreateTableActivity.this).getColumnFieldNameList(mSelectedTab);
                    Log.e("abc", "==========" + cloumns.size());
                    showSelectColumsDialog(cloumns, fk_field, cloumn_names, fk_field_title, null);
                }
            });

            boolean needShowDefaultInput = false;
            if (oldField != null) {
                cloum_name_input.setText(oldField.getFieldName());
                if (oldField.getFieldName() != null) {
                    cloum_name_input.setSelection(cloum_name_input.getText().toString().length());
                }
                if (!TextUtils.isEmpty(oldField.getFkTable())) {
                    fk_table.setText(oldField.getFkTable());
                }
                if (!TextUtils.isEmpty(oldField.getFkField())) {
                    fk_field.setText(oldField.getFkField());
                }
                if (oldField.getSize() > 0) {
                    string_length.setText(oldField.getSize() + "");
                }
                if (!TextUtils.isEmpty(oldField.getDefaultValue())) {
                    default_value_input.setText(oldField.getDefaultValue());
                }

                if (!TextUtils.isEmpty(oldField.getFieldType())) {
                    mColumnDataType = oldField.getFieldType();
                    int index = mDatas.indexOf(oldField.getFieldType());
                    typeSpinner.setSelection(index);
                }
                mColumnDataType = oldField.getFieldType();
                primary_checkbox.setChecked(oldField.isPrimaryKey());

                if (oldField.isPrimaryKey() && SqlConstant.INTEGER_TYPE.equals(mColumnDataType)) {
                    primary_key_auto_content.setVisibility(View.VISIBLE);
                    primary_autoincrement_checkbox.setChecked(oldField.isAutoIncrement());
                }

                mConditionList = new ArrayList<>();
                if (oldField.isForeignKey()) {
                    mConditionList.add(getString(R.string.condition_foregin_key));
                    conditionSelected.add(3);
                }

                if (oldField.isDefaultKey()) {
                    mConditionList.add(getString(R.string.condition_default));
                    conditionSelected.add(2);
                    mInputDefaultLayout.setVisibility(View.VISIBLE);
                }

                if (oldField.isUnique()) {
                    mConditionList.add(getString(R.string.condition_unique));
                    conditionSelected.add(1);
                }

                if (oldField.isNotNull()) {
                    mConditionList.add(getString(R.string.condition_notnull));
                    conditionSelected.add(0);
                }

                if (mConditionList.contains(getString(R.string.condition_default))) {
                    needShowDefaultInput = true;
                }

            }

            showDefaultInputLayout(needShowDefaultInput);
            int conditionSize = 0;
            if (mConditionList != null) {
                conditionSize = mConditionList.size() > 0 ? mConditionList.size() : 0;
            }

            mConditionTypeTxt.setText(getResources().getString(R.string.sql_create_column_condition_size).replace("&", conditionSize + ""));

            SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);
            int titleResId = (isEdit && !addNew) ? R.string.sql_create_tab_edit_cloum : R.string.sql_create_tab_add_cloum;
            builder.setTitle(titleResId)
                    .setView(view)
                    .setNeutralButton(R.string.cancel,
                            new SlidingUpDialogInterface.OnClickListener() {
                                @Override
                                public void onClick(SlidingUpDialogInterface dialog, int which) {
                                    BottomViewMgr.hideBottomView();
                                    if (isEdit) {
                                        clearEditMode();
                                    }
                                    if (mConditionList != null) {
                                        mConditionList.clear();
                                    }
                                }
                            })
                    .setPositiveButton(R.string.end,
                            new SlidingUpDialogInterface.OnClickListener() {
                                @Override
                                public void onClick(SlidingUpDialogInterface dialog, int which) {

                                    NewColumnField columnField = new NewColumnField();

                                    String fieldName = cloum_name_input.getText().toString();
                                    String fieldType = mColumnDataType;
                                    String defaultValue = default_value_input.getText().toString();
                                    String fkTable = fk_table.getText().toString();//spinner获取显示的内容
                                    String fkField = fk_field.getText().toString();
                                    boolean isPrimaryKey = primary_checkbox.isChecked();
                                    boolean isAutoIncrement = primary_autoincrement_checkbox.isChecked();

                                    long size = 0;
                                    if (string_length.getText() != null) {
                                        String sizeStr = string_length.getText().toString().trim();
                                        if (!TextUtils.isEmpty(sizeStr)) {
                                            size = Long.valueOf(sizeStr);
                                        }
                                    }
                                    columnField.setSize(size);

                                    if (!TextUtils.isEmpty(fieldName)) {
                                        columnField.setFieldName(fieldName.trim());
                                    }
                                    if (!TextUtils.isEmpty(fkTable)) {
                                        columnField.setFkTable(fkTable.trim());
                                    }
                                    if (!TextUtils.isEmpty(fkField)) {
                                        columnField.setFkField(fkField.trim());
                                    }
                                    if (!TextUtils.isEmpty(defaultValue)) {
                                        columnField.setDefaultValue(defaultValue);
                                    }
                                    columnField.setFieldType(fieldType);
                                    columnField.setPrimaryKey(isPrimaryKey);
                                    columnField.setAutoIncrement(isAutoIncrement);

                                    if (mConditionList != null) {
                                        for (String type : mConditionList) {
                                            if (type.equals(getString(R.string.condition_notnull))) {
                                                columnField.setNotNull(true);
                                            }

                                            if (type.equals(getString(R.string.condition_unique))) {
                                                columnField.setUnique(true);
                                            }

                                            if (type.equals(getString(R.string.condition_default))) {
                                                columnField.setDefaultKey(true);
                                            }

                                            if (type.equals(getString(R.string.condition_foregin_key))) {
                                                columnField.setForeignKey(true);
                                            }
                                        }
                                    }

                                    for (NewColumnField field : mNewColumnFieldList) {

                                        if (addNew) {
                                            if (columnField.isPrimaryKey() && field.isPrimaryKey()) {
                                                ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error1);
                                                return;
                                            }

                                            if (fieldName.equals(field.getFieldName())) {
                                                ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error2);
                                                return;
                                            }
                                        } else {

                                            String oldName = "";
                                            if (mTmpEditOldColumnField != null) {
                                                oldName = mTmpEditOldColumnField.getFieldName();
                                            } else if (oldField != null) {
                                                oldName = oldField.getFieldName();
                                            }

                                            if (!TextUtils.equals(oldName, fieldName)) {

                                                if (columnField.isPrimaryKey() && field.isPrimaryKey() && !field.getFieldName().equals(oldName)) {
                                                    ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error1);
                                                    return;
                                                }

                                                if (fieldName.equals(field.getFieldName())) {
                                                    ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error2);
                                                    return;
                                                }
                                            }
                                        }
                                    }

                                    if (columnField.isForeignKey()) {
                                        if (TextUtils.isEmpty(columnField.getFkTable())) {
                                            ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error4);
                                            return;
                                        }

                                        if (TextUtils.isEmpty(columnField.getFkField())) {
                                            ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error5);
                                            return;
                                        }
                                    }

                                    if (TextUtils.isEmpty(fieldName)) {
                                        ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, R.string.create_column_error6);
                                        return;
                                    }

//                                    if (Utils.isFunNeedBuy(SqlCreateTableActivity.this)) {//TODO
//                                        mTmpEditColumnField = columnField;
//                                        if (addNew) {
//                                            mTmpEditOldColumnField = null;
//                                        } else {
//                                            if (oldField != null) {
//                                                mTmpEditOldColumnField = oldField;
//                                            } else {
//                                                mTmpEditOldColumnField = null;
//                                            }
//                                        }
//                                        return;
//                                    }

                                    int r;
                                    String successTip;
                                    String failTip;
                                    if (addNew) {
                                        r = SQLManager.getSQLHelper(SqlCreateTableActivity.this).createTable(mTableName, columnField);
                                        successTip = getString(R.string.create_column_success);
                                        failTip = getString(R.string.create_column_error3);
                                    } else {
                                        Bundle bundle = SQLManager.getSQLHelper(SqlCreateTableActivity.this).processColumn(mTableName, oldField.getFieldName(), columnField);
                                        r = bundle.getInt(SqlConstant.RESULT);
                                        if (bundle.containsKey(SqlConstant.ERROR_MSG)){
                                            failTip = bundle.getString(SqlConstant.ERROR_MSG);
                                        } else {
                                            failTip = getString(R.string.operation_failed);
                                        }
                                        successTip = getString(R.string.edit_smb_ok);
                                    }

                                    BottomViewMgr.hideBottomView();

                                    if (r == SqlConstant.SUCCESS) {
                                        if (addNew) {
                                            mNewColumnFieldList.add(columnField);
                                        } else {
                                            int index = -1;
                                            if (oldField != null) {
                                                index = mNewColumnFieldList.indexOf(oldField);
                                                if (index != -1) {
                                                    mNewColumnFieldList.remove(oldField);
                                                }
                                            }

                                            if (index < 0 && mTmpEditOldColumnField != null) {
                                                index = mNewColumnFieldList.indexOf(mTmpEditOldColumnField);
                                                if (index != -1) {
                                                    mNewColumnFieldList.remove(mTmpEditOldColumnField);
                                                }
                                            }

                                            if (index != -1) {
                                                mNewColumnFieldList.add(index, columnField);
                                            } else {
                                                mNewColumnFieldList.add(columnField);
                                            }
                                        }

                                        openResult();
//                                        ToastManger.showSystemDoneToast(SqlCreateTableActivity.this, successTip);
                                        setResult(RESULT_OK, getIntent());

                                    } else {

                                        if (r == SqlConstant.FAIL_EDIT_COLUMN_FK){
                                            failTip = getString(R.string.delete_column_error1);
                                        } else if (r == SqlConstant.FAIL_EDIT_COLUMN_FK){
                                            failTip = getString(R.string.delete_column_error3);
                                        }

                                        ToastManger.showSystemErrorToast(SqlCreateTableActivity.this, Integer.parseInt(failTip));
                                    }

                                    BottomViewMgr.hideSoftwareInput(SqlCreateTableActivity.this, cloum_name_input);
                                    if (isEdit) {
                                        clearEditMode();
                                    }
                                    if (mConditionList != null) {
                                        mConditionList.clear();
                                    }
                                }
                            });

            SlidingUpDialog dialog = builder.create();
            BottomViewMgr.showBottomView(this, dialog);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDefaultInputLayout(boolean needShow) {
        if (needShow) {
            mInputDefaultLayout.setVisibility(View.VISIBLE);
        } else {
            mInputDefaultLayout.setVisibility(View.GONE);
        }
    }

    private void showConditionTypeDialog(final TextView mConditionTypeTxt) {
        View view = View.inflate(this, R.layout.bottom_menu_content, null);
        View dividerLine = view.findViewById(R.id.dividerLine);
        dividerLine.setVisibility(View.VISIBLE);
        RecyclerView itemList = (RecyclerView) view.findViewById(R.id.bottom_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        final String[] array = getResources().getStringArray(R.array.condition_type);
        final ColumnConditionAdapter adapter = new ColumnConditionAdapter(this, array, conditionSelected);
        itemList.setAdapter(adapter);

        SlidingUpDialog.Builder builder = new SlidingUpDialog.Builder(this);

        builder.setTitle(R.string.sql_create_tab_cloum_condition)
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

                                BottomViewMgr.hideBottomView();
                                List<Integer> positions = adapter.getSelectedPositions();
                                conditionSelected.clear();
                                conditionSelected.addAll(positions);
                                mConditionList = new ArrayList<>();
                                String str;
                                for (int i = 0; i < positions.size(); i++) {
                                    str = array[positions.get(i)];
                                    mConditionList.add(str);
                                    if (getString(R.string.condition_default).equals(str)) {
                                        mInputDefaultLayout.setVisibility(View.VISIBLE);
                                    }

                                    mConditionTypeTxt.setText(getResources().getString(R.string.sql_create_column_condition_size)
                                            .replace("&", String.valueOf(positions.size())));
                                }
                            }
                        });

        BottomViewMgr.showBottomView(this, builder.create());
    }

    private void showSelectColumsDialog(final List<String> tablesName, final TextView selectedTxt, final RelativeLayout cloumnLayout, final TextView fkFieldTitle, final TextView fk_field) {
        View view = View.inflate(this, R.layout.bottom_menu_content, null);
        View dividerLine = view.findViewById(R.id.dividerLine);
        dividerLine.setVisibility(View.VISIBLE);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        RecyclerView itemList = (RecyclerView) view.findViewById(R.id.bottom_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        SelectTabOrColumnDialog adapter = new SelectTabOrColumnDialog(this, tablesName);
        itemList.setAdapter(adapter);
        adapter.setMyListener(new SelectTabOrColumnDialog.MyListener() {
            @Override
            public void click(int position) {
                bottomSheetDialog.dismiss();
                String selected = tablesName.get(position);
                if (fk_field != null && !selected.equals(mSelectedTab)) {

                    mSelectedTab = selected;
                    fk_field.setText(null);

                    cloumnLayout.setEnabled(true);
                    cloumnLayout.setClickable(true);
//                    fkFieldTitle.setTextColor(getResources().getColor(SkinHandler.getResourceId(SqlCreateTableActivity.this, R.attr.minorText)));
                }
                selectedTxt.setText(selected);
            }
        });


        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ViewEvent event) {

        ViewEvent.EvenType type = event.getType();
        Bundle args = event.getArgs();

        if (!isInformation) {
            switch (type) {

                case sqlTabItemClick:
                    if (!isInformation) {
                        mClickPosition = (int) args.get(ViewEvent.Keys.SQL_TAB_ITEM_CLICK);
                        if (!isEdit) {
                            isEdit = true;
                            mToolbar.getMenu().clear();
                            mToolbar.inflateMenu(R.menu.sql_add_column_menu);
                            mToolBarButton.setVisibility(View.GONE);
                        }
                    }

                    break;
            }
        }

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
            } else if (mNewColumnFieldList == null || mNewColumnFieldList.size() == 0) {
                showExitTipDialog();
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return super.onMenuItemClick(item);
    }

    @Override
    public void onRefresh() {

        mNewColumnFieldList = SQLManager.getSQLHelper(this).getNewColumnFieldList(mTableName);
        if (mNewColumnFieldList != null && mNewColumnFieldList.size() > 0) {
            openResult();
        } else {
            Observable.timer(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            mEmptyRefreshLayout.setRefreshing(false);
                        }
                    });
        }
    }

    private void showExitTipDialog() {
        FeDialog.Builder builder = new FeDialog.Builder(this);
        builder.setMessage(R.string.sql_create_table_null_exit)
                .setPositiveButton(R.string.okey, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SqlCreateTableActivity.this.finish();
                    }
                })
                .setNeutralButton(R.string.sql_create_table_null_save_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BottomViewMgr.hideBottomView();
                        addColumnToTab(null, true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BottomViewMgr.hideBottomView();
                    }
                });

        BottomViewMgr.showBottomView(builder.create());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultHandler != null) {
            resultHandler.handleResult(requestCode, resultCode, data);

            if (mTmpEditColumnField != null){
                if (mTmpEditOldColumnField == null) {
                    addColumnToTab(mTmpEditColumnField, true);
                } else {

                    addColumnToTab(mTmpEditColumnField, false);
                }
            }
        }
    }
}
