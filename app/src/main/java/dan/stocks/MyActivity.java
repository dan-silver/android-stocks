package dan.stocks;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MyActivity extends FragmentActivity implements StockListFragment.OnStockSelectedListener {
    public static final String LOG_TAG = "STOCKS_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getActionBar().hide();
        //getApplicationContext().deleteDatabase("sugar_stocks.db");
        setContentView(R.layout.stocks);


        if (inSinglePaneLayout()) { //single pane
            StockListFragment listFragment = new StockListFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            listFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction().add(R.id.fragment_container, listFragment).commit();
        }
//        setupFetchMarketAlarm();
    }

    public class AlarmReceiver extends TimerTask {
        @Override
        public void run() {
            refreshStocks();
        }
    }


    private void setupFetchMarketAlarm() {
        Timer myTimer = new Timer();
        AlarmReceiver myTimerTask = new AlarmReceiver();
        myTimer.scheduleAtFixedRate(myTimerTask, 0, 8000);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.new_stock:
                createStock();
                return true;
            case R.id.refresh:
                refreshStocks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshStocks() {
        StringBuilder str = new StringBuilder();
        for (Stock s : Stock.listAll(Stock.class)) {
            str.append(s.apiId).append(",");
        }

        fetchStockMarketOverview((getListFragment().getListImageAdapter()), str.toString());
    }

    public void createStock() {
        Stock s = new Stock(getApplicationContext(), randomStockTicker());
        s.save();
        getListFragment().updateListWithNewStock(s);
        s.getStockCompanyInfo(getListFragment().getListImageAdapter());
        s.updateMarketInfo(getListFragment().getListImageAdapter(), 4, 5, 6);
        getListFragment().setLastSelected();
    }

    public String randomStockTicker() {
        int i = new Random().nextInt(7);
        switch (i) {
            case 0:
                return "T";
            case 1:
                return "VZ";
            case 2:
                return "S";
            case 3:
                return "CMCSA";
            case 4:
                return "DISH";
            case 5:
                return "GOOG";
            case 6:
                return "DELL";
            case 7:
                return "MSFT";
            default:
                return "DTV";
        }

    }

    public boolean inSinglePaneLayout() {
        return findViewById(R.id.fragment_container) != null;
    }

    public StockListFragment getListFragment() {
        int list_fragment_id = inSinglePaneLayout() ? R.id.fragment_container : R.id.stock_list_fragment;
        return ((StockListFragment) getFragmentManager().findFragmentById(list_fragment_id));
    }

    @Override
    public void onStockSelected(int pos, long id) {
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        StockDetailFragment detailFragment = (StockDetailFragment) getSupportFragmentManager().findFragmentById(R.id.stock_detail_fragment);

        if (detailFragment != null) {
            // If article frag is available, we're in two-pane layout...
            // Call a method in the ArticleFragment to update its content
            detailFragment.updateArticleView(id);

        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            StockDetailFragment newFragment = new StockDetailFragment();
            Bundle args = new Bundle();
            args.putLong(StockDetailFragment.STOCK_DB_ID, id);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }
    }

    void fetchStockMarketOverview(final ImageAdapter adapter, String apiIds) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("id", apiIds);
        client.get("http://enigmatic-reaches-7783.herokuapp.com/stocks.json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    processMarketData(adapter, response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processMarketData(final ImageAdapter adapter, String response) throws JSONException {
        JSONArray res = new JSONArray(response);
        HashMap<Integer, JSONObject> hash = new HashMap<Integer, JSONObject>(); //keys are apiIds, values are JSON objects
        for (int i = 0; i < res.length(); i++) {
            hash.put(res.getJSONObject(i).getInt("id"), res.getJSONObject(i));
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            final Stock existing_reference = adapter.getItem(i);
            //search apiIds for this stock to get the position of the results in res[]

            final JSONObject parsed_stock = hash.get(existing_reference.apiId);
            if (parsed_stock == null) break;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        existing_reference.updateMarketInfo(adapter,
                                parsed_stock.getDouble("change"),
                                parsed_stock.getDouble("changePercent"),
                                parsed_stock.getDouble("lastPrice"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
