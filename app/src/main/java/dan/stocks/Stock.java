package dan.stocks;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by dan on 6/14/14.
 */
public class Stock {
    public double lastPrice;
    public String companyName;
    public String ticker;

    public Stock() {
        super();
    }

    public Stock(String companyName, String ticker) {
        super();
        this.companyName = companyName;
        this.ticker = ticker;
    }

    void updateStockPrice() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("symbol", this.ticker);
        client.get("http://dev.markitondemand.com/Api/v2/Quote/json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                JSONObject res;
                try {
                    res = new JSONObject(response);
                    lastPrice = res.getDouble("LastPrice");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
