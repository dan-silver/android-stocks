package dan.stocks;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.Random;


public class MyActivity extends FragmentActivity implements StockListFragment.OnStockSelectedListener, StockDetailFragment.OnStockRemoveListener{
    public static final String LOG_TAG = "STOCKS_LOG";
    int selectionPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Stock.deleteAll(Stock.class);
        setContentView(R.layout.stocks);

        if (inSinglePaneLayout()) { //single pane
            StockListFragment listFragment = new StockListFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            listFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction().add(R.id.fragment_container, listFragment).commit();
        }
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
        getListFragment().refreshStocks();
    }
    public void createStock() {
//        StocksDataSource dataSource = new StocksDataSource(this);
        Stock s = new Stock(getApplicationContext(), randomStockName());
        s.save();
//        dataSource.close();
        getListFragment().updateListWithNewStock(s);
        selectionPosition = getListFragment().setLastSelected();
    }

    public String randomStockName() {
        int i = new Random().nextInt(6);
        switch (i) {
            case 0: return "T";
            case 1: return "VZ";
            case 2: return "S";
            case 3: return "CMCSA";
            case 4: return "DISH";
            case 5: return "GOOG";
            case 6: return "DELL";
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
        selectionPosition = pos;
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        StockDetailFragment detailFragment = (StockDetailFragment) getSupportFragmentManager().findFragmentById(R.id.stock_detail_fragment);

        if (detailFragment != null) {
            // If article frag is available, we're in two-pane layout...
            // Call a method in the ArticleFragment to update its content
            detailFragment.updateArticleView(pos, id);

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
        getListFragment().removeStockFromList(selectionPosition).delete();
        getListFragment().setNextSelected();
    }
}