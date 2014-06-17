package dan.stocks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dan on 6/15/14.
 */
public class StockDetailFragment extends Fragment {
    final static String STOCK_DB_ID = "stock_db_id";
    final static String CURRENT_POSITION = "mPosition";
    OnStockRemoveListener removeCallback;
    Stock currentStock;

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stock_detail, container, false);
    }
    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateArticleView(-1, args.getInt(STOCK_DB_ID));

            //mCurrentPosition = args.getInt(CURRENT_POSITION);
        }
//          else if (mCurrentPosition != -1) {
//            // Set article based on saved instance state defined during onCreateView
//            updateArticleView(mCurrentPosition);
//        }

        Button removeStock = (Button) getActivity().findViewById(R.id.remove_stock);
        removeStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCallback.onStockRemoved();
//                mCurrentPosition = -1;
            }
        });
    }

    public interface OnStockRemoveListener {
        public void onStockRemoved();
    }

    public void updateArticleView(int pos, long id) {
        TextView tickerTV = (TextView) getActivity().findViewById(R.id.stock_detail_ticker);

        Stock s = Stock.findById(Stock.class, id);
        if (s != null) {
            tickerTV.setText(s.ticker);
        }
     }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
//        outState.putInt(STOCK_DB_ID, mCurrentPosition);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            removeCallback = (OnStockRemoveListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
