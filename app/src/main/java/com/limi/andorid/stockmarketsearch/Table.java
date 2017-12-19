package com.limi.andorid.stockmarketsearch;

/**
 * Created by limi on 2017/11/20.
 */

public class Table {
    private String label;
    private String content;
    private boolean isIncrease;

    public Table(String label, String content) {
        this.label = label;
        this.content = content;
        if (label.equals("Change (Change Percent)")) {
            String temp = content.split(" ")[0];
            double change_value = Double.parseDouble(temp);
            this.label = "Change";
            isIncrease = change_value > 0;
        } else if (label.equals("Stock Ticker Symbol")) {
            this.label = "Stock Symbol";
        } else if (label.contains("Close")) {
            this.label = "Close";
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isIncrease() {
        return isIncrease;
    }

    public void setIncrease(boolean increase) {
        isIncrease = increase;
    }
}
