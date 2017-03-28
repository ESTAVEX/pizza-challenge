package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 3/28/2017.
 */

public class Group extends ArrayList<Pizza> {

    private String mName;
    private Pizza mCheapest;

    public Group(String Name) {
        this.mName = Name;
    }

    public int getPercentage(List<Pizza> pizzas) {
        return Math.abs((this.size() * 100) / pizzas.size());
    }

    public Pizza getCheapest() {
        return mCheapest;
    }

    private void setCheapest(Pizza pizza) {
        if (mCheapest == null) {
            mCheapest = pizza;
        } else if (mCheapest.getPrice() > pizza.getPrice()) {
            mCheapest = pizza;
        }
    }

    @Override
    public boolean add(Pizza pizza) {
        setCheapest(pizza);
        return super.add(pizza);
    }

    public String getName() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

}
