package dan.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dan on 6/22/14.
 */
public class StockSearchFragment extends Fragment {
    ListView listView;
    /**
     * Items entered by the user is stored in this ArrayList variable
     */
    ArrayList<String> list = new ArrayList<String>();

    /**
     * Declaring an ArrayAdapter to set items to ListView
     */
    ArrayAdapter<String> adapter;
    private FinishedSearchListener finishedSearch;
    JSONArray array;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        array = new JSONArray();
        listView = (ListView) getView().findViewById(R.id.search_results_list_view);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.stock_search_list_item, R.id.stock_search_list_content, list);
        listView.setAdapter(adapter);
        Button search = (Button) getView().findViewById(R.id.stock_search_execute);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        getInputField().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER) {
                    search();
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stock s;
                try {
                    JSONObject o = array.getJSONObject(position);
                    s = new Stock(getActivity());
                    s.companyName = o.getString("company");
                    s.apiId = o.getInt("id");
                    s.change = o.getDouble("change");
                    s.changePercent = o.getDouble("changePercent");
                    s.lastPrice = o.getDouble("lastPrice");
                    s.ticker = o.getString("ticker");
                    s.dayLow = o.getDouble("dayLow");
                    s.dayHigh = o.getDouble("dayHigh");
                    s.marketCap = o.getString("cap");
                    s.save();

                } catch (JSONException e) {
                    e.printStackTrace();
                    s = null;
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getInputField().getWindowToken(), 0);
                finishedSearch.searchResult(s);
            }
        });

    }

    private EditText getInputField() {
        return (EditText) getView().findViewById(R.id.stock_search_edit_text);
    }

    private String getSearchInput() {
        return getInputField().getText().toString();
    }

    private void search() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams("search", getSearchInput());
        client.get(MyActivity.API_URL + "stocks.json", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                try {
                    parseResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseResponse(String response) throws JSONException {
        adapter.clear();
        array = new JSONArray(response);
        for (int i = 0; i < array.length(); i++) {
            JSONObject stock = (JSONObject) array.get(i);
            adapter.add(stock.getString("company") + " (" + stock.getString("ticker") + ")");
        }
        adapter.notifyDataSetChanged();
    }

    public interface FinishedSearchListener {
        public void searchResult(Stock s);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            finishedSearch = (FinishedSearchListener) activity;
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }
}
