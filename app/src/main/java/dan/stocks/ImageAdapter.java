package dan.stocks;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dan on 6/14/14.
 */
public class ImageAdapter extends ArrayAdapter<Stock> {

    Context context;
    Stock[] stocks;
    int layoutResourceId;

    public ImageAdapter(Context context, int layoutResourceId, Stock[] stocks) {
        super(context, layoutResourceId, stocks);
        this.context = context;
        this.stocks = stocks;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        StockHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new StockHolder();
            holder.ticker = (TextView)row.findViewById(R.id.symbol);
            holder.companyName = (TextView)row.findViewById(R.id.companyName);
            holder.lastPrice= (TextView)row.findViewById(R.id.lastPrice);

            row.setTag(holder);
        }
        else
        {
            holder = (StockHolder)row.getTag();
        }

        Stock s = stocks[position];
        holder.ticker.setText(s.ticker);
        holder.companyName.setText(s.companyName);
        holder.lastPrice.setText(Double.toString(s.lastPrice));

        return row;
    }
    static class StockHolder
    {
        TextView ticker;
        TextView companyName;
        TextView lastPrice;
    }
}