package com.tuvakov.zeyoube.android.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.SubscriptionDao;
import com.tuvakov.zeyoube.android.data.ZeYouBeDatabase;
import com.tuvakov.zeyoube.android.utils.AppExecutors;

import java.util.List;


public class SubscriptionRepo {

    private SubscriptionDao mSubscriptionDao;
    private AppExecutors mAppExecutors;
    private LiveData<List<Subscription>> mAllSubscriptions;

    public SubscriptionRepo(Application application) {
        mSubscriptionDao = ZeYouBeDatabase.getInstance(application).getSubscriptionDao();
        mAppExecutors = AppExecutors.getInstance();
        mAllSubscriptions = mSubscriptionDao.selectAll();
    }

    public void insert(Subscription subscription) {
        mAppExecutors.getDiskIO().execute(() -> mSubscriptionDao.insert(subscription));
    }

    public void update(Subscription subscription) {
        mAppExecutors.getDiskIO().execute(() -> mSubscriptionDao.update(subscription));
    }

    public void delete(Subscription subscription) {
        mAppExecutors.getDiskIO().execute(() -> mSubscriptionDao.delete(subscription));
    }

    public void deleteAll() {
        mAppExecutors.getDiskIO().execute(() -> mSubscriptionDao.deleteAll());
    }

    public LiveData<List<Subscription>> getAllSubscriptions() {
        return mAllSubscriptions;
    }
}
