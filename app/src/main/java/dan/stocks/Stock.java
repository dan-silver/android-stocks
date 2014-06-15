package dan.stocks;

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

    public Stock(double lastPrice, String companyName, String ticker) {
        super();
        this.lastPrice = lastPrice;
        this.companyName = companyName;
        this.ticker = ticker;
    }
}
