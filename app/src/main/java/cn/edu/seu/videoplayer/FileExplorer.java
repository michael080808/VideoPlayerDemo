package cn.edu.seu.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileExplorer extends AppCompatActivity {
    static int PERMISSION_REQUEST_CODE = 0x4894;
    static int START_ACTIVITY_FOR_RESULT_CODE = 0x3295;
    final String TAG = "Video Player";

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean result = true;
            for (int grantResult : grantResults)
                result &= (grantResult == PackageManager.PERMISSION_GRANTED);
            if (!result)
                Log.i(TAG, "PERMISSION DENIED!!!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Loading OpenCV Successfully!");
        } else {
            Log.i(TAG, "Loading OpenCV Error!");
        }

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.fileexplorer_landspace);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.fileexplorer_portrait);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        ListView listView = (ListView) findViewById(R.id.list_view);
        List<Map<String, Object>> list = new ArrayList<>();
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA}, null, null, MediaStore.Video.Media.DISPLAY_NAME);
        while (cursor != null && cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("video_name", cursor.getString(1));
            map.put("path", cursor.getString(2));
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(cursor.getString(2));
            map.put("video_preview", mediaMetadataRetriever.getFrameAtTime(0));
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.listview_item, new String[]{"video_name", "path", "video_preview"}, new int[]{R.id.video_name, R.id.path, R.id.video_preview});
        adapter.setViewBinder(new ListViewBinder());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textview = (TextView) view.findViewById(R.id.path);
                Intent intent = new Intent("android.intent.action.VIDEOPLAYER");
                intent.putExtra("filePath", textview.getText());
                startActivityForResult(intent, START_ACTIVITY_FOR_RESULT_CODE);
            }
        });
    }
}
