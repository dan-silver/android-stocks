package dan.stocks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dan on 6/14/14.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    Context context;
    public List<Category> categories;
    int layoutResourceId;

    public CategorySpinnerAdapter(Context context, int layoutResourceId, List<Category> categories) {
        super(context, layoutResourceId, categories);
        this.context = context;
        this.categories = categories;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity)context).getLayoutInflater().inflate(layoutResourceId, parent, false);
        }
        TextView categoryTextView = (TextView) convertView.findViewById(R.id.spinner_category);
        Category category = categories.get(position);
        categoryTextView.setText(category.name);
        return convertView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}