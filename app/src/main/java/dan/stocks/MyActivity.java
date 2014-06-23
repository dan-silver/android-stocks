package dan.stocks;


import android.app.Fragment;
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


public class MyActivity extends FragmentActivity implements StockSearchFragment.FinishedSearchListener, StockListFragment.OnStockSelectedListener, StockListFragment.ListEmptyListener {
    public static final String LOG_TAG = "STOCKS_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
//        getActionBar().hide();
        //getApplicationContext().deleteDatabase("sugar_stocks.db");
        setContentView(R.layout.stocks);
        populateDualPaneFragments();
    }

    private void populateDualPaneFragments() {
        getFragmentManager().beginTransaction().add(R.id.stock_detail_fragment, new StockDetailFragment()).commit();
        getFragmentManager().beginTransaction().add(R.id.stock_list_fragment, new StockListFragment()).commit();
    }

    private void clearDualPaneFragments() {
        removeFragment(getFragmentManager().findFragmentById(R.id.stock_detail_fragment));
        removeFragment(getFragmentManager().findFragmentById(R.id.stock_list_fragment));
    }

    private void populateSearchFragment() {
        getFragmentManager().beginTransaction().add(R.id.stock_search_fragment, new StockSearchFragment()).commit();
    }

    private void removeFragment(Fragment frag) {
        if (frag != null) getFragmentManager().beginTransaction().remove(frag);
    }

    @Override
    public void isEmpty() {
        switchToSearchView();
    }

    public void switchToSearchView() {
        clearSearchFragment();
        setContentView(R.layout.stocks_search);
        populateSearchFragment();
    }

    private void clearSearchFragment() {
        removeFragment(getFragmentManager().findFragmentById(R.id.stock_search_fragment));
    }

    @Override
    public void searchResult(String ticker) {
        clearDualPaneFragments();
        createStock(ticker);
        setContentView(R.layout.stocks);
        populateDualPaneFragments();
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

    public void createStock(String ticker) {
        Stock s = new Stock(getApplicationContext(), ticker);
        s.save();
        getListFragment().updateListWithNewStock(s);
        s.getStockCompanyInfo(getListFragment().getListImageAdapter());
        s.updateMarketInfo(getListFragment().getListImageAdapter(), 4, 5, 6);
        getListFragment().setLastSelected();
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
        StockDetailFragment detailFragment = (StockDetailFragment) getFragmentManager().findFragmentById(R.id.stock_detail_fragment);
        detailFragment.updateArticleView(id);
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
