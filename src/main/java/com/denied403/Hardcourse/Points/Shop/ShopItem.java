package com.denied403.Hardcourse.Points.Shop;

public enum ShopItem {
    JUMP_BOOTS(1500),
    DOUBLE_JUMP(2000),
    JUMP_BOOST_ALL(1500), // per player
    TEMP_CHECKPOINT(7500),
    COSMETICS(0);

    private final int cost;

    ShopItem(int cost) {
        this.cost = cost;
    }

    public int cost() {
        return cost;
    }
}
