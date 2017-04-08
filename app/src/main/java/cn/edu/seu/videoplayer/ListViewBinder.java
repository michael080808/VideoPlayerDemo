package cn.edu.seu.videoplayer;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Created by Michael on 2017/4/7.
 */

public class ListViewBinder implements SimpleAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if ((view instanceof ImageView) && (data instanceof Bitmap)) {
            ImageView imageView = (ImageView) view;
            Bitmap bmp = (Bitmap) data;
            imageView.setImageBitmap(bmp);
            return true;
        }
        if((view instanceof TextView) && (data instanceof String)) {
            TextView textView = (TextView) view;
            textView.setText((String) data);
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            return true;
        }
        return false;
    }
}
