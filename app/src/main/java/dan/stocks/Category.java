package dan.stocks;

import android.content.Context;

import com.orm.SugarRecord;

/**
 * Created by dan on 7/3/14.
 */
public class Category extends SugarRecord<Category> {
    String name;

    public Category(Context context) {
        super(context);
    }

    public Category(Context applicationContext, String name) {
        super(applicationContext);
        this.name = name;
    }
}
