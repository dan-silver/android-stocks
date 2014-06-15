package dan.stocks;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dan on 6/14/14.
 */
public class ImageAdapter extends ArrayAdapter<Stock> {

    Context context;
    List<Stock> stocks;
    int layoutResourceId;

    public ImageAdapter(Context context, int layoutResourceId, List<Stock> stocks) {
        super(context, layoutResourceId, stocks);
        this.context = context;
        this.stocks = stocks;
        this.layoutResourceId = layoutResourceId;
    }

    public void add(Stock stock) {
        stocks.add(stock);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        StockHolder holder;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new StockHolder();
            holder.ticker = (TextView) row.findViewById(R.id.symbol);
            holder.companyName = (TextView) row.findViewById(R.id.companyName);
            holder.lastPrice= (TextView) row.findViewById(R.id.lastPrice);

            row.setTag(holder);
        } else {
            holder = (StockHolder)row.getTag();
        }

        Stock s = stocks.get(position);
        holder.ticker.setText(s.ticker);
        holder.companyName.setText(s.companyName);
        holder.lastPrice.setText(Double.toString(RoundTo2Decimals(s.lastPrice)));

        return row;
    }

    double RoundTo2Decimals(double val) {
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(val));
    }

    static class StockHolder
    {
        TextView ticker;
        TextView companyName;
        TextView lastPrice;
    }
}