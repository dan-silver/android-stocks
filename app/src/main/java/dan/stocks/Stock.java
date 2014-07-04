package dan.stocks;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by dan on 6/14/14.
 */
public class Stock extends SugarRecord<Stock> {
    public double lastPrice;
    public String companyName;
    public String ticker;
    public double change;
    public double changePercent;
    public double dayLow;
    public double dayHigh;
    public String marketCap;
    public int apiId;
    public String exchange;
    public Category category;

    public Stock(Context c) {
        super(c);
    }

    void updateMarketInfo(ImageAdapter adapter, double change, double changePercent, double lastPrice, double high, double low, String cap) {
        this.change = change;
        this.changePercent = changePercent;
        this.lastPrice = lastPrice;
        this.dayHigh = high;
        this.dayLow = low;
        this.marketCap = cap;
        save();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public String getDisplayName() {
        if (this.companyName != null) return companyName;
        return ticker;
    }

    public String getExchange() {
        return exchange.replace("NasdaqNM", "Nasdaq").toUpperCase();
    }

    public double getOpen() {
        return lastPrice - change;
    }
}
