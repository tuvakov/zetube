package com.tuvakov.zeyoube.android.repository;

import androidx.lifecycle.LiveData;

import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.data.VideoDao;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VideoRepo {

    private final VideoDao mVideoDao;
    private final Executor mDiskIO;
    private LiveData<List<Video>> mAllVideos;

    @Inject
    public VideoRepo(VideoDao videoDao, Executor diskIO) {
        mVideoDao = videoDao;
        mDiskIO = diskIO;
        mAllVideos = mVideoDao.selectAllVideos();
    }

    public void insert(Video video) {
        mDiskIO.execute(() -> mVideoDao.insert(video));
    }

    public void bulkInsertForService(List<Video> videos) {
        mVideoDao.bulkInsert(videos);
    }

    public void update(Video video) {
        mDiskIO.execute(() -> mVideoDao.update(video));
    }

    public void delete(Video video) {
        mDiskIO.execute(() -> mVideoDao.delete(video));
    }

    public void deleteAll() {
        mDiskIO.execute(mVideoDao::deleteAll);
    }

    public LiveData<List<Video>> getAllVideos() {
        return mAllVideos;
    }
}
