package cn.edu.seu.videoplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class VideoPlayer extends AppCompatActivity {
    static int PERMISSION_REQUEST_CODE = 0x4894;
    static int START_ACTIVITY_FOR_RESULT_CODE = 0x3295;
    final String TAG = "Video Player";
    String file_Path = "";
    VideoViewWithPlayPauseListener videoView;
    MediaController mediaController;
    MediaMetadataRetriever mediaMetadataRetriever;
    ExecutorService executorService;
    ColumnChartView hsv_histogram;
    LineChartView red_channel_histogram;
    LineChartView green_channel_histogram;
    LineChartView blue_channel_histogram;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("PLAYING_STATUS", videoView.isPlaying());
        videoView.pause();
        outState.putInt("LAST_PLAYED_TIME", videoView.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        videoView.seekTo(savedInstanceState.getInt("LAST_PLAYED_TIME"));
        if (savedInstanceState.getBoolean("PLAYING_STATUS"))
            videoView.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.videoplayer_landspace);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.videoplayer_portrait);
        }

        Intent intent = getIntent();
        file_Path = intent.getStringExtra("filePath");

        videoView = (VideoViewWithPlayPauseListener) findViewById(R.id.videoView);
        videoView.setVideoPath(file_Path);
        videoView.requestFocus();

        mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.videoView));
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(file_Path);
            ((TextView) findViewById(R.id.file_path)).setText(file_Path);
            ((TextView) findViewById(R.id.video_width)).setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            ((TextView) findViewById(R.id.video_height)).setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            Integer video_duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            String video_duration_str = String.format("%02d:%02d:%02d.%03d", video_duration / 3600000, (video_duration % 3600000) / 60000, (video_duration % 60000) / 1000, video_duration % 1000);
            ((TextView) findViewById(R.id.video_duration)).setText(video_duration_str);
            ((TextView) findViewById(R.id.video_bitrate)).setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) + "bps");
            ((TextView) findViewById(R.id.video_mimetype)).setText(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));

            hsv_histogram = (ColumnChartView) findViewById(R.id.hsv_info);
            red_channel_histogram = (LineChartView) findViewById(R.id.red_info);
            green_channel_histogram = (LineChartView) findViewById(R.id.green_info);
            blue_channel_histogram = (LineChartView) findViewById(R.id.blue_info);

            videoView.setOnPlayPauseListener(new VideoViewWithPlayPauseListener.PlayPauseListener() {
                @Override
                public void onPlay() {
                    executorService = Executors.newFixedThreadPool(4);
                    executorService.execute(new HsvHistDetector(videoView, mediaMetadataRetriever, hsv_histogram));
                    executorService.execute(new RgbHistDetector(videoView, mediaMetadataRetriever, RgbHistDetector.Channel.RED, red_channel_histogram));
                    executorService.execute(new RgbHistDetector(videoView, mediaMetadataRetriever, RgbHistDetector.Channel.GREEN, green_channel_histogram));
                    executorService.execute(new RgbHistDetector(videoView, mediaMetadataRetriever, RgbHistDetector.Channel.BLUE, blue_channel_histogram));
                }

                @Override
                public void onPause() {
                    executorService.shutdown();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        setResult(START_ACTIVITY_FOR_RESULT_CODE);
        super.onBackPressed();
    }
}
