package com.tuvakov.zeyoube.android.utils;

import android.app.Activity;
import android.view.View;

public class FullScreenHelper {

    private Activity context;
    private View[] views;

    public FullScreenHelper(Activity context, View... views) {
        this.context = context;
        this.views = views;
    }

    public void enterFullScreen() {
        View decorView = context.getWindow().getDecorView();

        hideSystemUi(decorView);

        for (View view : views) {
            view.setVisibility(View.GONE);
            view.invalidate();
        }
    }


    public void exitFullScreen() {
        View decorView = context.getWindow().getDecorView();

        showSystemUi(decorView);

        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.invalidate();
        }
    }

    private void hideSystemUi(View mDecorView) {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void showSystemUi(View mDecorView) {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
