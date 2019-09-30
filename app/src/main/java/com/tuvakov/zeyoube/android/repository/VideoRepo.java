package com.tuvakov.zeyoube.android.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.data.VideoDao;
import com.tuvakov.zeyoube.android.data.ZeYouBeDatabase;
import com.tuvakov.zeyoube.android.utils.AppExecutors;

import java.util.List;

public class VideoRepo {

    private VideoDao mVideoDao;
    private AppExecutors mAppExecutors;
    private LiveData<List<Video>> mAllVideos;

    public VideoRepo(Application application) {
        mVideoDao = ZeYouBeDatabase.getInstance(application).getVideoDao();
        mAppExecutors = AppExecutors.getInstance();
        mAllVideos = mVideoDao.selectAllByDate();
    }

    public void insert(Video video) {
        mAppExecutors.getDiskIO().execute(() -> mVideoDao.insert(video));
    }

    public void update(Video video) {
        mAppExecutors.getDiskIO().execute(() -> mVideoDao.update(video));
    }

    public void delete(Video video) {
        mAppExecutors.getDiskIO().execute(() -> mVideoDao.delete(video));
    }

    public void deleteAll() {
        mAppExecutors.getDiskIO().execute(() -> mVideoDao.deleteAll());
    }

    public LiveData<List<Video>> getAllVideos() {
        return mAllVideos;
    }
}
