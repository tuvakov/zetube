package com.tuvakov.zetube.android.repository;

import androidx.lifecycle.LiveData;

import com.tuvakov.zetube.android.data.Subscription;
import com.tuvakov.zetube.android.data.SubscriptionDao;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SubscriptionRepo {

    private final SubscriptionDao mSubscriptionDao;
    private final ExecutorService mDiskIO;
    private LiveData<List<Subscription>> mAllSubscriptions;

    @Inject
    public SubscriptionRepo(SubscriptionDao subscriptionDao, ExecutorService diskIO) {
        mSubscriptionDao = subscriptionDao;
        mDiskIO = diskIO;
        mAllSubscriptions = mSubscriptionDao.selectAll();
    }

    public void insert(Subscription subscription) {
        mDiskIO.execute(() -> mSubscriptionDao.insert(subscription));
    }

    public void bulkInsertForService(List<Subscription> subscriptions) {
        mSubscriptionDao.bulkInsert(subscriptions);
    }

    public void update(Subscription subscription) {
        mDiskIO.execute(() -> mSubscriptionDao.update(subscription));
    }

    public void delete(Subscription subscription) {
        mDiskIO.execute(() -> mSubscriptionDao.delete(subscription));
    }

    public void deleteAll() {
        mDiskIO.execute(mSubscriptionDao::deleteAll);
    }

    public void deleteAllForService() {
        mSubscriptionDao.deleteAll();
    }

    public LiveData<List<Subscription>> getAllSubscriptions() {
        return mAllSubscriptions;
    }
}
