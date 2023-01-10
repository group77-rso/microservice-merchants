package com.merchants.api.v1.health;


import com.merchants.services.config.RestProperties;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Liveness
@ApplicationScoped
public class MerchantsHealth implements HealthCheck {

    @Inject
    private RestProperties restProperties;

    @Override
    public HealthCheckResponse call() {
        if (restProperties.getBroken()) {
            return HealthCheckResponse.down(MerchantsHealth.class.getSimpleName());
        } else {
            return HealthCheckResponse.up(MerchantsHealth.class.getSimpleName());
        }
    }
}