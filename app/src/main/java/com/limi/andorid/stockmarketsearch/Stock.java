package com.limi.andorid.stockmarketsearch;

/**
 * Created by limi on 2017/11/20.
 */

public class Stock {
    private String symbol;
    private double price;
    private double change;
    private double change_percent;
    private long time;

    public Stock(String symbol, double price, double change, double change_percent, long time) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.change_percent = change_percent;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Stock(String symbol, double price, double change, double change_percent) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.change_percent = change_percent;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChange_percent() {
        return change_percent;
    }

    public void setChange_percent(double change_percent) {
        this.change_percent = change_percent;
    }
}
