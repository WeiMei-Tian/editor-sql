package com.gmobile.sqliteeditor.orm.helper;

import com.gmobile.sqliteeditor.model.bean.BaseData;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by liucheng on 2015/10/16
 */
public class BaseHelper<T, K> {

    private AbstractDao<T, K> mDao;

    public BaseHelper(AbstractDao dao) {
        mDao = dao;
    }

    public void save(T item) {
        try {
            mDao.insert(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(T... items) {
        try {
            mDao.insertInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(List<T> items) {
        try {
            mDao.insertInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOrUpdate(T item) {
        try {
            mDao.insertOrReplace(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOrUpdate(T... items) {
        try {
            mDao.insertOrReplaceInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveOrUpdate(List<T> items) {
        try {
            mDao.insertOrReplaceInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteByKey(K key) {
        mDao.deleteByKey(key);
    }

    public void delete(T item) {
        mDao.delete(item);
    }

    public void delete(T... items) {
        mDao.deleteInTx(items);
    }

    public void delete(List<T> items) {
        mDao.deleteInTx(items);
    }

    public void deleteAll() {
        mDao.deleteAll();
    }

    public void update(T item) {
        try {
            mDao.update(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(T... items) {
        try {
            mDao.updateInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(List<T> items) {
        try {
            mDao.updateInTx(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T query(K key) {
        return mDao.load(key);
    }

    public List<T> queryAll() {
        return mDao.loadAll();
    }

    public List<T> query(String where, String... params) {
        return mDao.queryRaw(where, params);
    }

    public QueryBuilder<T> queryBuilder() {
        return mDao.queryBuilder();
    }

    public long count() {
        return mDao.count();
    }

    public void refresh(T item) {
        mDao.refresh(item);
    }

    public void detach(T item) {
        mDao.detach(item);
    }

    public T buildObject(BaseData file) {
        return null;
    }

    public List<T> buildList(List<BaseData> fileList) {
        return null;
    }

    public boolean isNeedReloadDB(){
        return !(mDao.getDatabase() != null && mDao.getDatabase().isOpen());
    }

}
