package com.gmobile.sqliteeditor.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.gmobile.sqliteeditor.base.BaseFragment;

import java.util.List;

/**
 * Created by admin on 2016/11/22.
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> datas;
    private List<String> titles;

    public MyPagerAdapter(FragmentManager fm, List<BaseFragment> datas, List<String> titles) {
        super(fm);
        this.datas = datas;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return datas.get(position);
    }

    @Override
    public int getCount() {
        return datas != null ? datas.size() : 0;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

}
