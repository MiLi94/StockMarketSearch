package com.limi.andorid.stockmarketsearch;

/**
 * Created by limi on 2017/11/19.
 */

public class AutoCompleteItem {
    private String symbol;
    private String name;
    private String exchange;

    public AutoCompleteItem(String symbol, String name, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return symbol + " - " + name + "(" + exchange + ")";
    }
}
