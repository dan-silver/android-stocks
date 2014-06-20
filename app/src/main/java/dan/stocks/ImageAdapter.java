package dan.stocks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dan on 6/14/14.
 */
public class ImageAdapter extends ArrayAdapter<Stock> {

    Context context;
    public List<Stock> stocks;
    int layoutResourceId;

    public ImageAdapter(Context context, int layoutResourceId, List<Stock> stocks) {
        super(context, layoutResourceId, stocks);
        this.context = context;
        this.stocks = stocks;
        this.layoutResourceId = layoutResourceId;
    }

    public Stock remove(int position) {
        Stock s = stocks.remove(position);
        notifyDataSetChanged();
        return s;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        StockHolder holder;

        if(row == null) {
            row = ((Activity)context).getLayoutInflater().inflate(layoutResourceId, parent, false);

            holder = new StockHolder();
            holder.ticker = (TextView) row.findViewById(R.id.symbol);
            holder.companyName = (TextView) row.findViewById(R.id.companyName);
            holder.change = (TextView) row.findViewById(R.id.change);
            holder.changePercent = (TextView) row.findViewById(R.id.changePercent);
            holder.lastPrice = (TextView) row.findViewById(R.id.lastPrice);

            row.setTag(holder);
        } else {
            holder = (StockHolder)row.getTag();
        }

        Stock s = stocks.get(position);
        holder.ticker.setText("(" + s.ticker + ")");
        holder.companyName.setText(s.companyName);
        holder.lastPrice.setText(RoundTo2Decimals(s.lastPrice));
        holder.change.setText(RoundTo2Decimals(s.change));
        holder.changePercent.setText("(" + RoundTo2Decimals(s.changePercent) + "%)");
        if (s.changePercent >= 0) {
            holder.changePercent.setTextColor(Color.parseColor("#3d9400"));
            holder.change.setTextColor(Color.parseColor("#3d9400"));
        } else {
            holder.changePercent.setTextColor(Color.parseColor("#dd4b39"));
            holder.change.setTextColor(Color.parseColor("#dd4b39"));
        }
        return row;
    }

    String RoundTo2Decimals(double val) {
        return Double.toString(Double.valueOf(new DecimalFormat("###.##").format(val)));
    }

    public void update(int position, Stock s) {
    }

    public void resetWithData(int mCurCheckPosition, List<Stock> stocks) {
        clear();
        addAll(stocks);
    }

    static class StockHolder
    {
        TextView ticker;
        TextView companyName;
        TextView lastPrice;
        TextView change;
        TextView changePercent;
    }
}