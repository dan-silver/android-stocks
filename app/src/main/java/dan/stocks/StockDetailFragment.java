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
import android.widget.ProgressBar;
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
    ProgressBar loadingIcon;
    public int currentlySelected;
    private GraphViewData[] data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stock_detail, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadingIcon = (ProgressBar) getActivity().findViewById(R.id.graph_loading_icon);
        hideLoadingIcon();
        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            Log.v(MyActivity.LOG_TAG, "ARGS != NULL");
            updateArticleView(args.getLong(STOCK_DB_ID));
        }

        Button maxZoom = (Button) getActivity().findViewById(R.id.maxZoom);
        maxZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomChart();
                Log.v(MyActivity.LOG_TAG, "Max zooming");
            }
        });
    }

    public void updateArticleView(long id) {
        removeGraph();
        Stock s = Stock.findById(Stock.class, id);
        if (s != null) {
            TextView tickerTV = (TextView) getActivity().findViewById(R.id.stock_detail_ticker);
            if (tickerTV != null) {
                tickerTV.setText(s.ticker);

                TextView companyNameTV = (TextView) getActivity().findViewById(R.id.stock_detail_company_name);
                companyNameTV.setText(s.companyName);

                TextView lastPriceTV = (TextView) getActivity().findViewById(R.id.stock_detail_last_price);
                lastPriceTV.setText(ImageAdapter.RoundTo2Decimals(s.lastPrice));

                TextView changeTV = (TextView) getActivity().findViewById(R.id.stock_detail_change);
                changeTV.setText(ImageAdapter.RoundTo2Decimals(s.change));

                TextView changePercentTV = (TextView) getActivity().findViewById(R.id.stock_detail_change_percent);
                changePercentTV.setText("(" + ImageAdapter.RoundTo2Decimals(s.changePercent) + "%)");

                TextView rangeTV = (TextView) getActivity().findViewById(R.id.stock_detail_range);
                rangeTV.setText(ImageAdapter.RoundTo2Decimals(s.dayLow) + " - " + ImageAdapter.RoundTo2Decimals(s.dayHigh));

                TextView openTV = (TextView) getActivity().findViewById(R.id.stock_detail_open);
                openTV.setText(ImageAdapter.RoundTo2Decimals(s.getOpen()));

                TextView capTV = (TextView) getActivity().findViewById(R.id.stock_detail_cap);
                capTV.setText(s.marketCap);

                TextView exchangeTV = (TextView) getActivity().findViewById(R.id.stock_detail_exchange);
                exchangeTV.setText(s.getExchange());

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

    public void fetchStockHistory(final int apiId) {
        currentlySelected = apiId;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(MyActivity.API_URL + "stocks/" + apiId + "/prices.json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                if (apiId != currentlySelected) return;
                try {
                    JSONArray res = new JSONArray(response);
                    data = new GraphViewData[res.length()];
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject o = res.getJSONObject(i);
                        data[i] = new GraphViewData(o.getLong("datetime") * 1000, o.getDouble("value"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateGraph();
            }
        });
    }

    private void removeGraph() {
        showLoadingIcon();
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.graph);
        if (layout != null) {
            GraphView gv = (GraphView) layout.findViewWithTag("actual_graph");
            if (gv != null) layout.removeView(gv);
        }
    }

    private void updateGraph() {
        if (data.length == 0) return;
        Activity activity = getActivity();
        if (activity == null) return;
        LinearLayout layout = (LinearLayout) activity.findViewById(R.id.graph);
        if (layout != null) {
            GraphView graphView = new LineGraphView(getActivity(), "");
            graphView.addSeries(new GraphViewSeries(data));
            graphView.setTag("actual_graph");
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
            graphView.setViewPort(data[data.length - 1].getX() - 2.62974383e9, 2.62974383e9);
            hideLoadingIcon();
            layout.addView(graphView);
        }
    }

    public void zoomChart() {
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.graph);
        GraphView gv = (GraphView) layout.findViewWithTag("actual_graph");
        gv.setViewPort(data[0].getX(), data[data.length - 1].getX() - data[0].getX());
        gv.redrawAll();
    }

    private void showLoadingIcon() {
        loadingIcon.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIcon() {
        loadingIcon.setVisibility(View.GONE);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
