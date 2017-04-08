package cn.edu.seu.videoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VideoViewWithPlayPauseListener extends VideoView {
    private PlayPauseListener mListener;

    public VideoViewWithPlayPauseListener(Context context) {
        super(context);
    }

    public VideoViewWithPlayPauseListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoViewWithPlayPauseListener(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnPlayPauseListener(PlayPauseListener listener) {
        mListener = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    public interface PlayPauseListener {
        void onPlay();

        void onPause();
    }
}
