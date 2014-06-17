package dan.stocks;

import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Color;
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
    static int mCurCheckPosition = 0;
    OnStockSelectedListener mCallback;

    public StockListFragment() {
    }

    public void setStockSelected(final int p) {
        getListView().post(new Runnable() {
            @Override
            public void run() {
                getListView().requestFocusFromTouch();
                getListView().setSelection(p);
                getListView().requestFocus();
                mCurCheckPosition = p;
                mCallback.onStockSelected(mCurCheckPosition, ((ImageAdapter) getListAdapter()).stocks.get(mCurCheckPosition).getId());
            }
        });
    }

    public int setNextSelected() {
        if (getListView().getCount() > mCurCheckPosition) {
            setStockSelected(mCurCheckPosition);
            return mCurCheckPosition;
        } else if (getListView().getCount() > 0) {
            setStockSelected(mCurCheckPosition - 1);
            return mCurCheckPosition - 1;
        }
        return -1;
    }


    public int setLastSelected() {
        int i = getListAdapter().getCount() - 1;
        setStockSelected(i);
        return i;
    }


    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnStockSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onStockSelected(int pos, long id);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<Stock> stocks = Stock.listAll(Stock.class);
        Log.v(MyActivity.LOG_TAG, "There are " + stocks.size() + " stocks.");
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
        getListView().setBackgroundColor(Color.parseColor("#323232"));
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
        mCallback.onStockSelected(position, ((ImageAdapter) getListAdapter()).stocks.get(position).getId());
        v.setSelected(true);
        mCurCheckPosition = position;
    }

    public void updateListWithNewStock(Stock s) {
        if (getListAdapter() != null) {
            ((ImageAdapter) getListAdapter()).add(s);
        }
    }

    public Stock removeStockFromList(int pos) {
        if (getListAdapter() != null) {
            return ((ImageAdapter) getListAdapter()).remove(pos);
        }
        return null;
    }

    public void refreshStocks() {
        ((ImageAdapter) getListAdapter()).refreshStocks();
    }

}
