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

import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

public class HsvHistDetector implements Runnable {
    static int sizeOfColor = 16;
    static int[] colorList = new int[sizeOfColor];
    private VideoView videoView;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private ColumnChartView columnChartView;

    HsvHistDetector(VideoView videoView, MediaMetadataRetriever mediaMetadataRetriever, ColumnChartView columnChartView) {
        initColorList();
        this.videoView = videoView;
        this.mediaMetadataRetriever = mediaMetadataRetriever;
        this.columnChartView = columnChartView;
    }

    static void initColorList() {
        for (int i = 0; i < sizeOfColor; i++) {
            colorList[i] = Color.HSVToColor(new float[]{(i + 0.5f) * 360.f / sizeOfColor, 1f, 1f});
        }
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
            Imgproc.cvtColor(frameMat, frameMat, Imgproc.COLOR_BGR2HSV_FULL);
            Core.split(frameMat, frameMatList);
            Imgproc.calcHist(frameMatList, new MatOfInt(0), new Mat(), histogram, new MatOfInt(sizeOfColor), new MatOfFloat(0f, 256f));

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> values;
            for (int i = 0; i < sizeOfColor; i++) {
                values = new ArrayList<>();
                values.add(new SubcolumnValue((float) histogram.get(i, 0)[0], colorList[i]));
                columns.add(new Column(values));
            }
            ColumnChartData data = new ColumnChartData(columns);
            columnChartView.setColumnChartData(data);
        }
    }
}
