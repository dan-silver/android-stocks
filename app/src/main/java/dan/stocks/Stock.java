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
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    void getStockCompanyInfo(final ImageAdapter adapter) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("ticker", this.ticker);
        client.get("http://enigmatic-reaches-7783.herokuapp.com/stock.json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                JSONObject res;
                try {
                    res = new JSONObject(response);
                    //lastPrice = res.getDouble("LastPrice");
                    //change = res.getDouble("Change");
                    //changePercent = res.getDouble("ChangePercent");
                    companyName = res.getString("company");

                    apiId = res.getInt("apiId");
                    save();
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
