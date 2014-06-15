package dan.stocks;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dan on 6/15/14.
 */
public class StockListFragment extends ListFragment {
    boolean mDualPane;
    int mCurCheckPosition = 0;
    OnStockSelectedListener mCallback;

    public StockListFragment() {
    }
    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnStockSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onStockSelected(int position);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Stock> stocks = new ArrayList<Stock>();
        stocks.add(new Stock("MSFT"));
        stocks.add(new Stock("AAPL"));

        setListAdapter(new ImageAdapter(getActivity(), R.layout.grid_element, stocks));

        View detailsFrame = getActivity().findViewById(R.id.stock_detail_fragment);
        mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        if (mDualPane) {
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            // Make sure our UI is in the correct state.
//            showDetails(mCurCheckPosition);
        }
    }
    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.stock_detail_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnStockSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onStockSelected(position);
        getListView().setItemChecked(position, true);
        Log.d("STOCKS", "" + position);
    }
    public void addStock() {
        ((ImageAdapter) getListAdapter()).add("VZ");
    }

    public void refreshStocks() {
        ((ImageAdapter) getListAdapter()).refreshStocks();
    }

}
