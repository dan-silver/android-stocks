package dan.stocks;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orm.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MyActivity extends FragmentActivity implements StockListFragment.OnStockSelectedListener, StockDetailFragment.OnStockRemoveListener{
    public static final String LOG_TAG = "STOCKS_LOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        //setupFetchMarketAlarm();
    }

    public class AlarmReceiver extends TimerTask {
        @Override
        public void run() {
            Log.v("STOCKS", "refreshStocks()");
            refreshStocks();
        }
    }


    private void setupFetchMarketAlarm() {
        Timer myTimer = new Timer();
        AlarmReceiver myTimerTask= new AlarmReceiver();
        myTimer.scheduleAtFixedRate(myTimerTask,0,8000);


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

        fetchStockMarketOverview(((ImageAdapter) getListFragment().getListAdapter()), str.toString());
    }
    public void createStock() {
        Stock s = new Stock(getApplicationContext(), randomStockName());
        s.save();
        getListFragment().updateListWithNewStock(s);
        s.getStockCompanyInfo((ImageAdapter) getListFragment().getListAdapter());
        getListFragment().setLastSelected();
    }

    public String randomStockName() {
        int i = new Random().nextInt(7);
        switch (i) {
            case 0: return "T";
            case 1: return "VZ";
            case 2: return "S";
            case 3: return "CMCSA";
            case 4: return "DISH";
            case 5: return "GOOG";
            case 6: return "DELL";
            case 7: return "MSFT";
            default: return "DTV";
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

    @Override
    public void onStockRemoved() {
        getListFragment().removeStockFromList().delete();
    }

    void fetchStockMarketOverview(final ImageAdapter adapter, String apiIds) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("id", apiIds);
        client.get("http://enigmatic-reaches-7783.herokuapp.com/stocks.json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray res = new JSONArray(response);
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject stock = res.getJSONObject(i);
                        Stock s = Stock.find(Stock.class, "api_id = ?", Integer.toString(stock.getInt("id"))).get(0);
                        s.change = stock.getDouble("change");
                        s.changePercent = stock.getDouble("changePercent");
                        s.lastPrice = stock.getDouble("lastPrice");
                        s.save();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ListView listView = getListFragment().getListView();

                //@Todo not setting selected after update
                final int index = listView.getSelectedItemPosition();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.addAll(Stock.listAll(Stock.class));
                        getListFragment().setStockSelected(index);

                    }
                });


//                LinearLayout v = (LinearLayout) getListFragment().getListAdapter().getView(0, view, parent);
                        //resetDataWith(Stock.listAll(Stock.class));

            }
        });

    }
}