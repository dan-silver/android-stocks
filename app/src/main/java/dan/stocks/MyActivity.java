package dan.stocks;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MyActivity extends FragmentActivity implements StockListFragment.OnStockSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stocks);
//        if (savedInstanceState == null) return;

        if (findViewById(R.id.fragment_container) != null) { //single pane
            // Create an instance of ExampleFragment
            StockListFragment firstFragment = new StockListFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        } else { //dual pane
//            StockListFragment headlines = (StockListFragment) getSupportFragmentManager().findFragmentById(R.id.headlines_fragment);
//            headlines.createList();
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

    private void refreshStocks() {
        getListFragment().refreshStocks();
    }
    public void createStock() {
        getListFragment().addStock();
    }

    public boolean inSinglePaneLayout() {
        return findViewById(R.id.fragment_container) != null;
    }
    public StockListFragment getListFragment() {
        int list_fragment_id;
        if (inSinglePaneLayout()) { //single pane
            list_fragment_id = R.id.fragment_container;
        } else {
            list_fragment_id = R.id.stock_list_fragment;
        }
        return ((StockListFragment) getFragmentManager().findFragmentById(list_fragment_id));
    }


    @Override
    public void onStockSelected(int position) {
        Log.v("STOCKS", "going to detail " + position);
        // The user selected the headline of an article from the HeadlinesFragment

        // Capture the article fragment from the activity layout
        StockDetailFragment articleFrag = (StockDetailFragment) getSupportFragmentManager().findFragmentById(R.id.stock_detail_fragment);

        if (articleFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
            articleFrag.updateArticleView(position);

        } else {
            Log.v("STOCKS", "in one pane layout");
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            StockDetailFragment newFragment = new StockDetailFragment();
            Bundle args = new Bundle();
            args.putInt(StockDetailFragment.ARG_POSITION, position);
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
}