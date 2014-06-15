package dan.stocks;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.new_stock:
                createStock();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createStock() {
        PlaceholderFragment fragment = (PlaceholderFragment) getFragmentManager().findFragmentById(R.id.container);
        fragment.addStock();
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        ImageAdapter adapter;

        public PlaceholderFragment() {
        }
        public String randomString(int length) {
            char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < length; i++) {
                char c = chars[random.nextInt(chars.length)];
                sb.append(c);
            }
            return sb.toString();
        }
        public String upperCaseFirstChar(String s) {
            return s.substring(0,1).toUpperCase() + s.substring(1);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            List<Stock> stocks = new ArrayList<Stock>();
            for (int i=0;i<100;i++) {
                double random = new Random().nextDouble();
                stocks.add(new Stock((random * 1000), upperCaseFirstChar(randomString((int) (Math.random() * (7) + 3))), randomString((int)(Math.random() * (3) + 2)).toUpperCase()));
            }

            View view = inflater.inflate(R.layout.fragment_my,container,false);
            GridView gridView = (GridView) view.findViewById(R.id.gridview);
            this.adapter = new ImageAdapter(view.getContext(), R.layout.grid_element, stocks);
            gridView.setAdapter(adapter);
            return view;
        }

        public void addStock() {
            Toast.makeText(getActivity(), "Creating new stock from fragment", Toast.LENGTH_SHORT).show();
            adapter.add(new Stock((new Random().nextDouble() * 1000), upperCaseFirstChar(randomString((int) (Math.random() * (7) + 3))), randomString((int)(Math.random() * (3) + 2)).toUpperCase()));
        }
    }
}
