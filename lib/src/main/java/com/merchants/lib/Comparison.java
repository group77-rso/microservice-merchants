package com.merchants.lib;

import java.util.Set;

public class Comparison {

    private Product product;
    private Set<Price> prices;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Set<Price> getPrices() {
        return prices;
    }

    public void setPrices(Set<Price> prices) {
        this.prices = prices;
    }
}
