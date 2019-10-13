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
        mAllVideos = mVideoDao.selectAllVideos();
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

    public void insertDummies() {
        Video bb = new Video(
                "This problem seems hard, then it doesn't, but it really is",
                "https://i.ytimg.com/vi/M64HUIJFTZM/sddefault.jpg",
                "The famous (infamous?) \"windmill\" problem on the 2011 IMO\n" +
                        "Home page: https://www.3blue1brown.com\n" +
                        "Brought to you by you: http://3b1b.co/windmillthanks\n\n" +
                        "The author of this problem was Geoff Smith.  You can find the full list of " +
                        "problems considered for the IMO that year, together with their solutions," +
                        " here:\nhttps://www.imo-official.org/problems/IMO2011SL.pdf\n\n" +
                        "You can find data for past IMO results here:\nhttps://www.imo-official.org/" +
                        "\n\nViewer-created interactive about this problem:\n" +
                        "https://www.reddit.com/r/3Blue1Brown/comments/d0b0qw/interactive_windmill_visual_program_download_link/\n\n" +
                        "I made a quick reference to \"proper time\" as an example of an invariant. " +
                        " Take a look at this minutephysics video if you want to learn more.\n" +
                        "https://youtu.be/WFAEHKAR5hU\n\n------------------\n\n" +
                        "These animations are largely made using manim, a scrappy open-source " +
                        "python library:  https://github.com/3b1b/manim\n\n" +
                        "If you want to check it out, I feel compelled to warn you that it's not " +
                        "the most well-documented tool, and it has many other quirks you might " +
                        "expect in a library someone wrote with only their own use in mind.\n\n" +
                        "Music by Vincent Rubinetti.\nDownload the music on Bandcamp:\n" +
                        "https://vincerubinetti.bandcamp.com/album/the-music-of-3blue1brown\n\n" +
                        "Stream the music on Spotify:\n" +
                        "https://open.spotify.com/album/1dVyjwS8FBqXhRunaG5W5u\n\n" +
                        "If you want to contribute translated subtitles or to help review those" +
                        " that have already been made by others and need approval, you can click " +
                        "the gear icon in the video and go to subtitles/cc, then \"add subtitles/cc\"." +
                        "  I really appreciate those who do this, as it helps make the lessons " +
                        "accessible to more people.\n\n------------------\n\n" +
                        "3blue1brown is a channel about animating math, in all senses of the word " +
                        "animate.  And you know the drill with YouTube, if you want to stay posted " +
                        "on new videos, subscribe: http://3b1b.co/subscribe\n\n" +
                        "Various social media stuffs:\nWebsite: https://www.3blue1brown.com\n" +
                        "Twitter: https://twitter.com/3blue1brown\n" +
                        "Reddit: https://www.reddit.com/r/3blue1brown\n" +
                        "Instagram: https://www.instagram.com/3blue1brown_animations/\n" +
                        "Patreon: https://patreon.com/3blue1brown\n" +
                        "Facebook: https://www.facebook.com/3blue1brown",
                1,
                "M64HUIJFTZM",
                false,
                "2019-08-04T17:03:51.000Z");

        Video ado = new Video(
                "PROFOTO C1 & C1 PLUS | Hands On",
                "https://i.ytimg.com/vi/M64HUIJFTZM/sddefault.jpg",
                "Introducing the brand new ProFoto C1 and ProFoto C1Plus Pocket Studio Light " +
                        "For Smartphones. Created for creators by creators!\n\n" +
                        "In stock with free expedited delivery on Adorama.com \n☞" +
                        "https://www.adorama.com/g/profoto-c1-plus-pocket-flash-led-light\n\n" +
                        "Key Features of the ProFoto C1:\n•4 warm and 3 cool LEDs\n•Max 1000 lumens " +
                        "continuous light\n•30 minutes of continuous light at full power in one " +
                        "charge\nC1 Plus:\n•10 warm and 10 cool LEDs\n•Max 4300 lumens\n•40 minutes " +
                        "of continuous light at full power in one charge\n•Compatible with light " +
                        "shaping tools like Clic Gels, Grids and Domes\n•Compatible with all " +
                        "Profoto AirTTL Remotes\n\n\nPlus Clic Light-Shaping Domes, Gel & Grids " +
                        "for A1, A1X & C1Plus\n\nSubscribe to the Adorama YouTube Channel:\n☞ " +
                        "https://www.youtube.com/user/adoramaTV \nFollow us on Social Media \n☞ " +
                        "https://www.facebook.com/Adorama/ \n☞https://www.instagram.com/adorama/ \n☞" +
                        " https://twitter.com/adorama\n\n#profoto #c1 #fashion",
                2,
                "XUagjSy2vBE",
                false,
                "2019-09-18T09:00:10.000Z"
        );

        insert(bb);
        insert(ado);
    }
}
