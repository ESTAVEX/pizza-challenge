package models;

import java.util.ArrayList;
import java.util.List;

import utility.Enums;

/**
 * Created by mo on 3/28/2017.
 */

public class Pizza {

    private String mPizzaName;
    private int mPrice;
    private List<String> mIngredients;
    private List<Enums.Categories> mCategory;

    public Pizza(String name, int price, List<String> ingredients) {
        this.mIngredients = new ArrayList<>();
        this.mCategory = new ArrayList<>();

        this.mPizzaName = name;
        this.mPrice = price;
        this.mIngredients = ingredients;

        SetCategory();
    }

    private void SetCategory() {
        int cheeseNumber = 0;
        int meatNumber = 0;
        int oliveNumber = 0;
        int mozzarelaNumber = 0;
        int mushroomNumber = 0;

        for (String ingredient : mIngredients) {
            if (ingredient.contains("cheese"))
                cheeseNumber++;

            if (ingredient.contains("meat")  ||
                ingredient.contains("kebab") ||
                ingredient.contains("beef")  ||
                ingredient.contains("ham"))
                meatNumber++;

            if (ingredient.contains("olive"))
                oliveNumber++;

            if (ingredient.contains("mushroom"))
                mushroomNumber++;

            if (ingredient.contains("mozzarella"))
                mozzarelaNumber++;
        }

        // If it's more than 0 or 1 according to what it is, we set it in a category
        if (cheeseNumber > 1)                           this.mCategory.add(Enums.Categories.WITH_MORE_CHEESE);
        if (meatNumber > 0)                             this.mCategory.add(Enums.Categories.WITH_MEAT);
        if (meatNumber > 0 && oliveNumber > 0)          this.mCategory.add(Enums.Categories.WITH_MEAT_OLIVE);
        if (mozzarelaNumber > 0 && mushroomNumber > 0)  this.mCategory.add(Enums.Categories.WITH_MOZZARELA_MUSHROOM);
    }

    // Getters and setters
    public String getPizzaName() {
        return this.mPizzaName;
    }

    public void setPizzaName(String mPizzaName) {
        this.mPizzaName = mPizzaName;
    }

    public int getPrice() {
        return this.mPrice;
    }

    public void setPrice(int mPrice) {
        this.mPrice = mPrice;
    }

    public List<String> getIngredients() {
        return this.mIngredients;
    }

    public void setIngredients(List<String> mIngredients) {
        this.mIngredients = mIngredients;
    }

    public List<Enums.Categories> getCategory() {
        return this.mCategory;
    }

    public void setCategory(List<Enums.Categories> mCategory) {
        this.mCategory = mCategory;
    }

}
