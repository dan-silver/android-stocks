package dan.stocks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.LineGraphView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by dan on 6/15/14.
 */
public class StockDetailFragment extends Fragment {
    final static String STOCK_DB_ID = "stock_db_id";
    OnStockRemoveListener removeCallback;

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
            updateArticleView(args.getInt(STOCK_DB_ID));
        }

        Button removeStock = (Button) getActivity().findViewById(R.id.remove_stock);
        removeStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCallback.onStockRemoved();
            }
        });

    }

    public interface OnStockRemoveListener {
        public void onStockRemoved();
    }

    public void updateArticleView(long id) {
        removeGraph();
        Stock s = Stock.findById(Stock.class, id);
        if (s != null) {
            TextView tickerTV = (TextView) getActivity().findViewById(R.id.stock_detail_ticker);
            tickerTV.setText(s.ticker);
            fetchStockHistory(s.apiId);
        }
    }

    public void fetchStockHistory(int apiId) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://enigmatic-reaches-7783.herokuapp.com/stocks/" + apiId + "/prices.json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                GraphViewData[] data;
                try {
                    JSONArray res = new JSONArray(response);
                    DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
                    data = new GraphViewData[res.length()];
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject o = res.getJSONObject(i);
                        data[i] = new GraphViewData(o.getLong("datetime") * 1000, o.getDouble("value"));
                        //data[i] = new GraphViewData(1403367785894l + i * 10000l, i*10);
                        //data[i] = new GraphViewData(1403367785894l + i * 10000l, i*10);
                        Log.v("a1a1", dateTimeFormatter.format(new Date((long) data[i].getX())));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    data = null;
                }


                updateGraph(data);
            }
        });
    }

    private void removeGraph() {
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.graph);
        if (layout != null) {
            layout.removeAllViews();
        }
    }

    private void updateGraph(GraphViewData[] data) {
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.graph);
        if (layout != null) {
            GraphView graphView = new LineGraphView(getActivity().getApplicationContext(), "");
            graphView.addSeries(new GraphViewSeries(data));
//            graphView.setViewPort(2, 40); // set view port, start=2, size=40
//
//            // optional - activate scaling / zooming
            graphView.setScrollable(true);
            graphView.setScalable(true);
            final android.text.format.DateFormat df = new android.text.format.DateFormat();
            graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX)
                        return df.format("MM/dd/yyyy", new Date((long) value)).toString();
                    return null; // let graphview generate Y-axis label for us
                }
            });
            layout.addView(graphView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            removeCallback = (OnStockRemoveListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
