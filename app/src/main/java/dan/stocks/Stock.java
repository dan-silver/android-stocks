package dan.stocks;

import android.content.Context;
import android.media.Image;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by dan on 6/14/14.
 */
public class Stock extends SugarRecord<Stock> {
    public double lastPrice;
    public String companyName;
    public String ticker;
    public double change;
    public double changePercent;
    public int id;

    public Stock(Context c) {
        super(c);
    }

    public Stock(Context c, String ticker) {
        super(c);
        this.ticker = ticker;
    }

    void updateStockPrice(final ImageAdapter adapter) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("symbol", this.ticker);
        client.get("http://dev.markitondemand.com/Api/v2/Quote/json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                JSONObject res;
                try {
                    Log.v("STOCK", response);
                    res = new JSONObject(response);
                    lastPrice = res.getDouble("LastPrice");
                    change = res.getDouble("Change");
                    changePercent = res.getDouble("ChangePercent");
                    companyName = res.getString("Name");
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
