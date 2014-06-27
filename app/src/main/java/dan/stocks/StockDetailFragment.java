package dan.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
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
//            updateArticleView(args.getInt(STOCK_DB_ID));
        }
    }

    public void updateArticleView(long id) {
        removeGraph();
        Stock s = Stock.findById(Stock.class, id);
        if (s != null) {
            TextView tickerTV = (TextView) getActivity().findViewById(R.id.stock_detail_ticker);
            if (tickerTV != null) {
                tickerTV.setText("(" + s.ticker + ")");

                TextView companyNameTV = (TextView) getActivity().findViewById(R.id.stock_detail_company_name);
                companyNameTV.setText(s.companyName);

                TextView lastPriceTV = (TextView) getActivity().findViewById(R.id.stock_detail_last_price);
                lastPriceTV.setText(ImageAdapter.RoundTo2Decimals(s.lastPrice));

                TextView changeTV = (TextView) getActivity().findViewById(R.id.stock_detail_change);
                changeTV.setText(ImageAdapter.RoundTo2Decimals(s.change));

                TextView changePercentTV = (TextView) getActivity().findViewById(R.id.stock_detail_change_percent);
                changePercentTV.setText("(" + ImageAdapter.RoundTo2Decimals(s.changePercent) + ")");

                TextView rangeTV = (TextView) getActivity().findViewById(R.id.stock_detail_range);
                rangeTV.setText(ImageAdapter.RoundTo2Decimals(s.dayLow) + " - " + ImageAdapter.RoundTo2Decimals(s.dayHigh));

                TextView openTV = (TextView) getActivity().findViewById(R.id.stock_detail_open);
                openTV.setText(ImageAdapter.RoundTo2Decimals(s.getOpen()));

                TextView capTV = (TextView) getActivity().findViewById(R.id.stock_detail_cap);
                capTV.setText(s.marketCap);

                if (s.changePercent >= 0) {
                    changePercentTV.setTextColor(Color.parseColor("#3d9400"));
                    changeTV.setTextColor(Color.parseColor("#3d9400"));
                } else {
                    changePercentTV.setTextColor(Color.parseColor("#dd4b39"));
                    changeTV.setTextColor(Color.parseColor("#dd4b39"));
                }

                fetchStockHistory(s.apiId);
            }
        }
    }

    public void fetchStockHistory(int apiId) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(MyActivity.API_URL + "stocks/" + apiId + "/prices.json", new AsyncHttpResponseHandler() {
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

                if (data != null) updateGraph(data);
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
        Activity activity = getActivity();
        if (activity == null) return;
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.graph);
        if (layout != null) {
            GraphView graphView = new LineGraphView(getActivity(), "");
            graphView.addSeries(new GraphViewSeries(data));
//          optional - activate scaling / zooming
            graphView.setScalable(true);
            graphView.setScrollable(true);
            final android.text.format.DateFormat df = new android.text.format.DateFormat();
            graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX)
                        return df.format("MM/dd/yyyy", new Date((long) value)).toString();
                    return null; // let graphview generate Y-axis label for us
                }
            });
            graphView.setViewPort(data[0].getX(), data[data.length - 1].getX() - data[0].getX());
            layout.addView(graphView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
