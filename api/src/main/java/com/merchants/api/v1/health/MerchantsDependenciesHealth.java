package com.merchants.api.v1.health;

import com.merchants.services.config.MicroserviceLocations;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
public class MerchantsDependenciesHealth implements HealthCheck {

    @Inject
    private MicroserviceLocations microserviceLocations;

    @Override
    public HealthCheckResponse call() {
        String urlProducts = microserviceLocations.getProducts() + "/v1/products/ping/";
        if (pingDependency(urlProducts)) {
            return HealthCheckResponse.up(MerchantsDependenciesHealth.class.getSimpleName());
        }
        return HealthCheckResponse.down(MerchantsDependenciesHealth.class.getSimpleName());
    }

    private boolean pingDependency(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }


}
