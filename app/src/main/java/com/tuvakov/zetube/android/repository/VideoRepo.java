package com.tuvakov.zetube.android.repository;

import androidx.lifecycle.LiveData;

import com.tuvakov.zetube.android.data.Video;
import com.tuvakov.zetube.android.data.VideoDao;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VideoRepo {

    private final VideoDao mVideoDao;
    private final ExecutorService mDiskIO;
    private LiveData<List<Video>> mAllVideos;

    @Inject
    public VideoRepo(VideoDao videoDao, ExecutorService diskIO) {
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

    public void deleteAllForService() {
        mVideoDao.deleteAll();
    }

    public LiveData<List<Video>> getAllVideos() {
        return mAllVideos;
    }

    public Video getVideoById(String videoId) {
        Future<Video> video = mDiskIO.submit(() -> mVideoDao.getVideoById(videoId));
        try {
            return video.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
