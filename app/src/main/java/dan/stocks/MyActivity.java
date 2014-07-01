package dan.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MyActivity extends FragmentActivity implements StockListFragment.OnStockSelectedListener, StockListFragment.ListEmptyListener, StockListFragment.ItemRemovedListener {
    public static final String LOG_TAG = "STOCKS_LOG";
    public static final String API_URL = "http://104.131.249.221/";
    public static final int REQUEST_SEARCH_RESULT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getActionBar().hide();
//        getApplicationContext().deleteDatabase("sugar_stocks.db");
        setContentView(R.layout.stocks);
        populateDualPaneFragments();
    }

    private void populateDualPaneFragments() {
        getFragmentManager().beginTransaction().add(R.id.fragment_detail_large, new StockDetailFragment()).commit();
        getFragmentManager().beginTransaction().add(R.id.stock_list_fragment, new StockListFragment()).commit();
    }

    @Override
    public void isEmpty() {
        switchToSearchView();
    }



    public void switchToSearchView() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, REQUEST_SEARCH_RESULT);
    }

    public void switchToStockDetailView(long stock_id) {
        StockDetailFragment detailFragment = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putLong(StockDetailFragment.STOCK_DB_ID, stock_id);
        detailFragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment_detail_large, detailFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SEARCH_RESULT && resultCode == Activity.RESULT_OK) {
            switchToStockDetailView(data.getLongExtra("STOCK", 0));
            createStock(Stock.findById(Stock.class, data.getLongExtra("STOCK", 0)));
        }
    }

    @Override
    public void itemRemoved(long displayedDetailItemApiId) {
        long displayedDetailItem = getDetailFragment().currentlySelected;
        if (displayedDetailItemApiId == displayedDetailItem) {
            getListFragment().setStockSelected(0);
        }
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
                switchToSearchView();
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

    public void createStock(Stock stock) {
        getListFragment().updateListWithNewStock(stock);
        getListFragment().setLastSelected();
    }

    public boolean inSinglePaneLayout() {
        return findViewById(R.id.fragment_container) != null;
    }

    public StockListFragment getListFragment() {
        int list_fragment_id = inSinglePaneLayout() ? R.id.fragment_container : R.id.stock_list_fragment;
        return ((StockListFragment) getFragmentManager().findFragmentById(list_fragment_id));
    }

    public StockDetailFragment getDetailFragment() {
        return ((StockDetailFragment) getFragmentManager().findFragmentById(R.id.fragment_detail_large));
    }

    @Override
    public void onStockSelected(int pos, long id) {
        Fragment frag = getFragmentManager().findFragmentById(R.id.fragment_detail_large);
        ((StockDetailFragment) frag).updateArticleView(id);
    }

    void fetchStockMarketOverview(final ImageAdapter adapter, String apiIds) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("id", apiIds);
        client.get(API_URL + "stocks.json", params, new AsyncHttpResponseHandler() {
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
                                parsed_stock.getDouble("lastPrice"),
                                parsed_stock.getDouble("dayHigh"),
                                parsed_stock.getDouble("dayLow"),
                                parsed_stock.getString("cap"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }
}
