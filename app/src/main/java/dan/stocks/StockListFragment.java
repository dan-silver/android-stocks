package dan.stocks;

import android.app.Activity;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by dan on 6/15/14.
 */
public class StockListFragment extends ListFragment {
    boolean mDualPane;
    static int mCurCheckPosition = 0;
    private OnStockSelectedListener mCallback;
    private ItemRemovedListener item_removed_listener;
    private ListEmptyListener list_empty_listener;

    public StockListFragment() {
    }

    public void setStockSelected(final int p) {
        if (p == -1) return;
        if (getListImageAdapter().getCount() == 0) {
            list_empty_listener.isEmpty();
            return;
        }
        getListView().post(new Runnable() {
            @Override
            public void run() {
                getListView().requestFocusFromTouch();
                getListView().setSelection(p);
                getListView().requestFocus();
                mCurCheckPosition = p;
                if (getListImageAdapter().stocks != null)
                    mCallback.onStockSelected(mCurCheckPosition, getListImageAdapter().stocks.get(mCurCheckPosition).getId());
            }
        });
    }

    public void setLastSelected() {
        int i = getListAdapter().getCount() - 1;
        setStockSelected(i);
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

        View detailsFrame = getView().findViewById(R.id.fragment_detail_large);
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

        ListView listView = getListView();
        SwipeDismissListViewTouchListener touchListener =
                 new SwipeDismissListViewTouchListener(getListView(),
                         new SwipeDismissListViewTouchListener.DismissCallbacks() {
                             @Override
                             public boolean canDismiss(int position) {
                                 return true;
                             }

                             public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                 for (int position : reverseSortedPositions) {
                                     Stock s = getListImageAdapter().remove(position);
                                     Toast.makeText(getActivity().getApplicationContext(), s.getDisplayName() + " has been removed",
                                             Toast.LENGTH_LONG).show();
                                             item_removed_listener.itemRemoved(s.apiId);
                                             s.delete();
                                     if (getListImageAdapter().isEmpty()) list_empty_listener.isEmpty();
                                 }
                             }
                         });
         listView.setOnTouchListener(touchListener);
         listView.setOnScrollListener(touchListener.makeScrollListener());
    }
    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.fragment_detail_large) != null) {
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
        try {
            list_empty_listener = (ListEmptyListener) activity;
            item_removed_listener = (ItemRemovedListener) activity;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
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
            getListImageAdapter().add(s);
        }
    }
    public ImageAdapter getListImageAdapter() {
        return (ImageAdapter) getListAdapter();
    }

    public interface ListEmptyListener {
        public void isEmpty();
    }
    public interface ItemRemovedListener {
        public void itemRemoved(long displayedDetailItemApiId);
    }
}
