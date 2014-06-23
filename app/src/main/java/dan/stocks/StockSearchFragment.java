package dan.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
    /** Items entered by the user is stored in this ArrayList variable */
    ArrayList<String> list = new ArrayList<String>();

    /** Declaring an ArrayAdapter to set items to ListView */
    ArrayAdapter<String> adapter;
    ArrayList<String> tickers = new ArrayList<String>();
    private FinishedSearchListener finishedSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
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
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                finishedSearch.searchResult(tickers.get(position));
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
        client.get("http://enigmatic-reaches-7783.herokuapp.com/stocks.json", params, new AsyncHttpResponseHandler() {
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
        tickers.clear();
        JSONArray array = new JSONArray(response);
        for (int i=0;i<array.length();i++) {
            JSONObject stock = (JSONObject) array.get(i);
            tickers.add(stock.getString("ticker"));
            adapter.add(stock.getString("company") + " (" + stock.getString("ticker") + ")");
        }
        adapter.notifyDataSetChanged();
    }
    public interface FinishedSearchListener {
        public void searchResult(String ticker);
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
