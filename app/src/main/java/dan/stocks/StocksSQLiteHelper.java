package dan.stocks;

/**
 * Created by dan on 6/15/14.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StocksSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_STOCKS = "stocks";
    public static final String COLUMN_ID = "_id";
    public static final String COMPANY_NAME = "companyName";
    public static final String TICKER = "ticker";

    private static final String DATABASE_NAME = "sets.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_STOCKS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COMPANY_NAME + " text, "
            + TICKER + " text not null" + ");";

    public StocksSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(StocksSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        onCreate(db);
    }

}