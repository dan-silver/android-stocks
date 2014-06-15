package dan.stocks;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by dan on 6/14/14.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] stocks;

    public ImageAdapter(Context c, String[] stocks) {
        mContext = c;
        this.stocks = stocks;
    }

    public int getCount() {
        return stocks.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.grid_element, null);
            TextView textView = (TextView) gridView.findViewById(R.id.symbol);
            textView.setText(stocks[position]);
        } else {
            gridView = convertView;
        }
        return gridView;
    }

}