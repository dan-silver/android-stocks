package dan.stocks;

import java.util.Random;

/**
 * Created by dan on 6/14/14.
 */
public class Stock {
    public double lastPrice;
    public String companyName;
    public String ticker;

    public Stock() {
        super();
    }

    public Stock(String companyName, String ticker) {
        super();
        this.companyName = companyName;
        this.ticker = ticker;
    }

    void updateStockPrice() {
        this.lastPrice = new Random().nextDouble() * 100;
    }

}
