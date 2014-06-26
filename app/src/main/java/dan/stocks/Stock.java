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
    public int apiId;

    public Stock(Context c) {
        super(c);
    }

    public Stock(Context c, String ticker) {
        super(c);
        this.ticker = ticker.toUpperCase();
    }

    void updateMarketInfo(ImageAdapter adapter, double change, double changePercent, double lastPrice) {
        this.change = change;
        this.changePercent = changePercent;
        this.lastPrice = lastPrice;
        save();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public String getDisplayName() {
        if (this.companyName != null) return companyName;
        return ticker;
    }
}
