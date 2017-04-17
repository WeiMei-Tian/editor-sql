package com.gmobile.sqliteeditor.adapter.listener;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.gmobile.data.DataFactory;
import com.gmobile.data.constant.DataConstant;
import com.gmobile.data.constant.ResultConstant;
import com.gmobile.library.base.assistant.utils.PreferenceUtils;
import com.gmobile.library.base.view.operation.ToastManger;
import com.gmobile.memory.MemoryFileUtils;
import com.gmobile.sqliteeditor.R;
import com.gmobile.sqliteeditor.adapter.BaseHolder;
import com.gmobile.sqliteeditor.assistant.TmpFolderUtils;
import com.gmobile.sqliteeditor.assistant.Utils;
import com.gmobile.sqliteeditor.base.BaseFragment;
import com.gmobile.sqliteeditor.constant.Constant;
import com.gmobile.sqliteeditor.factory.DataType;
import com.gmobile.sqliteeditor.model.bean.AppDataObject;
import com.gmobile.sqliteeditor.ui.event.ViewEvent;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by sg on 2016/11/25.
 */
public class AppItemListener extends BaseItemListener {

    public AppItemListener(BaseFragment fragment) {
        super(fragment);
    }

    @Override
    protected void onClickImp(BaseHolder holder) {
        final AppDataObject data = (AppDataObject) holder.getBaseData();
        int position = holder.getClickPosition();

        if (data != null) {
            final String curPath = data.getPath();
            final String curName = data.getName();
            if (TextUtils.isEmpty(curPath)) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.CLICK_POSITION, position);
                bundle.putSerializable(Constant.DATA_TYPE_KEY, DataType.appData);
                EventBus.getDefault().post(new ViewEvent(ViewEvent.EvenType.gotoAppClickPosition, bundle));

            } else {

                if (curPath.startsWith("/data/data")) {
                    final String tmpFilePath = TmpFolderUtils.getDBTempDir(data.getName());
                    Observable.just(0)
                            .observeOn(Schedulers.io())
                            .map(new Func1<Integer, Integer>() {
                                @Override
                                public Integer call(Integer index) {
                                    return MemoryFileUtils.moveToWithShell(DataFactory.createObject(DataConstant.MEMORY_DATA_ID, curPath),
                                            DataFactory.createLocalObject(TmpFolderUtils.getDBTempDir(curName)));
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Integer>() {
                                @Override
                                public void call(Integer result) {

                                    if (result == ResultConstant.EXIST || result == ResultConstant.SUCCESS) {
                                        PreferenceUtils.getPrefString(fragment.getActivity(), Constant.CUR_APP_DB_PATH, curPath);
                                        PreferenceUtils.getPrefString(fragment.getActivity(), Constant.CUR_APP_TMP_DB_PATH, tmpFilePath);
                                        Utils.openDB(fragment.getActivity(), tmpFilePath, curName);
                                    } else {
                                        ToastManger.showErrorToast(fragment.getActivity(), R.string.error_happen);
                                    }
                                }
                            });

                } else {
                    Utils.openDB(fragment.getActivity(), curPath, data.getName());
                }
            }
        }
    }

    @Override
    protected void onLongClickImp(View v) {

    }
}
