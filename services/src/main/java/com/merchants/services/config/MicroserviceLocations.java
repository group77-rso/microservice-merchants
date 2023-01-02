package com.merchants.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;


@ConfigBundle("microservice-locations")
@ApplicationScoped
public class MicroserviceLocations {

    @ConfigValue(watch = true)
    private String products;

    public String getProducts() {
        return this.products;
    }

    public void setProducts(final String products) {
        this.products = products;
    }
}
