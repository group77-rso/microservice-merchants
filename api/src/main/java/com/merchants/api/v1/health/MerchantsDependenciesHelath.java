package com.merchants.api.v1.health;

import com.merchants.services.config.MicroserviceLocations;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Readiness
@ApplicationScoped
public class MerchantsDependenciesHelath implements HealthCheck {

    @Inject
    private MicroserviceLocations microserviceLocations;

    @Override
    public HealthCheckResponse call() {
        String urlProducts = microserviceLocations.getProducts() + "/v1/products/ping/";
        if (pingDependency(urlProducts)) {
            return HealthCheckResponse.up(MerchantsDependenciesHelath.class.getSimpleName());
        }
        return HealthCheckResponse.down(MerchantsDependenciesHelath.class.getSimpleName());
    }

    private boolean pingDependency(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            int code = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .statusCode();
            return code < 300;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }


}
