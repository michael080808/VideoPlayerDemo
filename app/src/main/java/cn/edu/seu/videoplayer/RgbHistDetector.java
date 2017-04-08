package cn.edu.seu.videoplayer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.widget.VideoView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Michael on 2017/4/7.
 */

public class RgbHistDetector implements Runnable {
    static int sizeOfColor = 16;
    Channel channel;
    private VideoView videoView;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private LineChartView lineChartView;

    RgbHistDetector(VideoView videoView, MediaMetadataRetriever mediaMetadataRetriever, Channel channel, LineChartView lineChartView) {
        this.videoView = videoView;
        this.mediaMetadataRetriever = mediaMetadataRetriever;
        this.channel = channel;
        this.lineChartView = lineChartView;
    }

    @Override
    public void run() {
        while (videoView.isPlaying()) {
            //All the Convert
            Mat histogram = new Mat();
            Mat frameMat = new Mat();
            List<Mat> frameMatList = new ArrayList<>();
            Bitmap frameBitMap = mediaMetadataRetriever.getFrameAtTime(1000 * videoView.getCurrentPosition());
            Utils.bitmapToMat(frameBitMap, frameMat);
            Core.split(frameMat, frameMatList);
            Imgproc.calcHist(frameMatList, channel.getChannelNumber(), new Mat(), histogram, new MatOfInt(sizeOfColor), new MatOfFloat(0f, 256f));

            List<PointValue> pointValues = new ArrayList<>();
            for (int i = 0; i < sizeOfColor; i++)
                pointValues.add(new PointValue((float) i, (float) histogram.get(i, 0)[0]));
            Line line = new Line(pointValues);
            line.setColor(Color.parseColor(channel.toString()));
            line.setFilled(true);
            List<Line> lines = new ArrayList<>();
            lines.add(line);
            lineChartView.setLineChartData(new LineChartData(lines));
        }
    }

    enum Channel {
        RED, GREEN, BLUE;

        public MatOfInt getChannelNumber() {
            switch (this) {
                case RED:
                    return new MatOfInt(2);
                case GREEN:
                    return new MatOfInt(1);
                case BLUE:
                    return new MatOfInt(0);
                default:
                    return new MatOfInt(-1);
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case RED:
                    return "#FF0000";
                case GREEN:
                    return "#00FF00";
                case BLUE:
                    return "#0000FF";
                default:
                    return "#FFFFFF";
            }
        }
    }
}
