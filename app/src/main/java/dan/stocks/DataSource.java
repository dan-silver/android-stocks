package dan.stocks;

/**
 * Created by dan on 6/15/14.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dan on 12/7/13.
 */

public class DataSource {

    // Database fields
    private SQLiteDatabase database;
    private StocksSQLiteHelper dbHelper;
    private String[] allColumns = { StocksSQLiteHelper.COLUMN_ID,
            StocksSQLiteHelper.COMPANY_NAME, StocksSQLiteHelper.TICKER };

    public DataSource(Context context) {
        dbHelper = new StocksSQLiteHelper(context);
    }

    public DataSource open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public Stock insert(String ticker) {
        ContentValues values = new ContentValues();
        values.put(StocksSQLiteHelper.TICKER, ticker);
        long insertId = database.insert(StocksSQLiteHelper.TABLE_STOCKS, null,
                values);
        Cursor cursor = database.query(StocksSQLiteHelper.TABLE_STOCKS,
                allColumns, StocksSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Stock newStock = cursorToRow(cursor);
        cursor.close();

        return newStock;
    }


    public void deleteStock(long id) {
        System.out.println("Set deleted with id: " + id);
        database.delete(StocksSQLiteHelper.TABLE_STOCKS, StocksSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Stock> getAllRows() {
        List<Stock> stocks = new ArrayList<Stock>();

        Cursor cursor = database.query(StocksSQLiteHelper.TABLE_STOCKS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Stock s = cursorToRow(cursor);
            stocks.add(s);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return stocks;
    }

    public Stock get(int id) {
        Stock s = new Stock();
        Cursor cursor = database.query(StocksSQLiteHelper.TABLE_STOCKS,
                allColumns, StocksSQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            s = cursorToRow(cursor);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return s;
    }
    private Stock cursorToRow(Cursor cursor) {
        Stock stock = new Stock();
        stock.id = (int) cursor.getLong(0);
        stock.companyName = cursor.getString(1);
        stock.ticker = cursor.getString(2);
        return stock;
    }
}
