package com.gmobile.sqliteeditor.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.listener.SqlActivityResultHandler;
import com.gmobile.sqliteeditor.assistant.sqlite.SQLManager;
import com.gmobile.sqliteeditor.constant.SqlConstant;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * Created by admin on 2016/10/17.
 */
public class SqlCommondActivity extends SwipeBackActivity {

    private FrameLayout btn_content;
    private EditText clearEditText;
    private int keyHeight;
    private TextView delete_btn;
    private Button mToolBarBtn;
    private LinearLayout response_content_layout;
    private TextView response_content;
    private Toolbar toolBar;
    private SqlActivityResultHandler resultHandler;

    public static final String TABLE_NAME = "table_name";
    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sql_commond_layout);
        initView();
        initIntent();
        setListener();
        initData();
    }

    private void initIntent() {
        tableName = getIntent().getStringExtra(TABLE_NAME);
    }

    private void initView() {
        delete_btn = (TextView) findViewById(R.id.delete_btn);
        clearEditText = (EditText) findViewById(R.id.edit_sql);
        response_content_layout = (LinearLayout) findViewById(R.id.response_content_ll);
        response_content = (TextView) findViewById(R.id.response_content);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBarBtn = (Button) findViewById(R.id.toolbar_button);
        mToolBarBtn.setVisibility(View.VISIBLE);
        mToolBarBtn.setText(getResources().getString(R.string.sql_commond_run));
        initToolBar();
    }

    protected void initToolBar() {
        if (toolBar != null) {
            toolBar.setTitleTextAppearance(this, R.style.ToolbarTitleAppearance);
            toolBar.setTitle(getResources().getString(R.string.sql_commond_sql));
            toolBar.setNavigationIcon(R.drawable.ic_arrow_left_navbar);
            toolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolBar.setTitleTextColor(getResources().getColor(R.color.white));
            toolBar.setSubtitleTextColor(getResources().getColor(R.color.label_grey));
        }
    }


    private void setListener() {
        resultHandler = new SqlActivityResultHandler(this);

        clearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (clearEditText.isFocused() && s.length() > 0) {
                    delete_btn.setVisibility(View.VISIBLE);
                } else {
                    delete_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEditText.setText(null);
            }
        });

        mToolBarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSql(clearEditText.getText().toString().trim());
//                StatisticsData.recordOperationId(StatisticsData.ActionId.SQLITE_RUN_SQL);
            }
        });
    }

    private void initData() {
        int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        keyHeight = screenHeight / 3;
    }

    //此处执行sql命令
    private void runSql(String sql) {

        hideSoftwareInput(this, clearEditText);
        response_content_layout.setVisibility(View.VISIBLE);

        //查询语句跳转界面,其他直接显示结果
        int sqlType = DatabaseUtils.getSqlStatementType(sql);

        if (sqlType == DatabaseUtils.STATEMENT_SELECT) {

            startTableDataActivity(sql);

        } else {

            try {
                int result = SQLManager.getSQLHelper(SqlCommondActivity.this).execSQLStr(sql);
                response_content.setText(result + "");
            } catch (Exception e) {
                e.printStackTrace();
                response_content.setText(e.getMessage());
            }
        }
    }

    private void startTableDataActivity(String sql) {
        Intent intent = SqlTabDatasActivity.newIntent(this, SqlConstant.TABLE_DATAS_SQL, sql);
        startActivity(intent);
    }

    private void hideSoftwareInput(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultHandler != null) {
            resultHandler.handleResult(requestCode, resultCode, data);
        }
    }
}
