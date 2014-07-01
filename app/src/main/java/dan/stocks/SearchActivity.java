package dan.stocks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class SearchActivity extends Activity {
    ArrayList<String> list = new ArrayList<String>();
    ListView listView;
    ArrayAdapter<String> adapter;
    JSONArray array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_search);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        hideNoStocksFoundMessage();
        array = new JSONArray();
        listView = (ListView) findViewById(R.id.search_results_list_view);
        adapter = new ArrayAdapter<String>(this, R.layout.stock_search_list_item, R.id.stock_search_list_content, list);
        listView.setAdapter(adapter);
        Button search = (Button) findViewById(R.id.stock_search_execute);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeKeyboard();
                search();
            }
        });

        getInputField().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER) {
                    closeKeyboard();
                    search();
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                Stock s = new Stock(getApplicationContext());
                try {
                    JSONObject o = array.getJSONObject(position);
                    s.companyName = o.getString("company");
                    s.apiId = o.getInt("id");
                    s.change = o.getDouble("change");
                    s.lastPrice = o.getDouble("lastPrice");
                    s.ticker = o.getString("ticker");
                    s.dayLow = o.getDouble("dayLow");
                    s.dayHigh = o.getDouble("dayHigh");
                    s.marketCap = o.getString("cap");
                    s.changePercent = o.getDouble("changePercent");
                    s.exchange = o.getString("exchange");
                    s.save();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                s.save();

                Intent intent = new Intent(getApplicationContext(), MyActivity.class);
                intent.putExtra("STOCK", s.getId());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getInputField().getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private EditText getInputField() {
        return (EditText) findViewById(R.id.stock_search_edit_text);
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
        hideNoStocksFoundMessage();
        adapter.notifyDataSetChanged();
        if (adapter.getCount() == 0) {
            displayNoStocksFoundMessage();
        }
    }

    private void displayNoStocksFoundMessage() {
        TextView tv = (TextView) findViewById(R.id.stock_search_no_stocks_found);
        tv.setVisibility(View.VISIBLE);
    }

    private void hideNoStocksFoundMessage() {
        TextView tv = (TextView) findViewById(R.id.stock_search_no_stocks_found);
        tv.setVisibility(View.GONE);
    }
}

