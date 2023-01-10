package com.merchants.api.v1.graphgl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merchants.lib.Merchant;
import com.merchants.lib.Price;
import com.merchants.lib.Product;
import com.merchants.services.beans.MerchantBean;
import com.merchants.services.config.MicroserviceLocations;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequestScoped
@GraphQLApi
public class MerchantsQueriesAndMutations {

    private final Logger log = Logger.getLogger(MerchantsQueriesAndMutations.class.getName());

    @Inject
    private MerchantBean merchantBean;

    @Inject
    private MicroserviceLocations microserviceLocations;

    // QUERIES

    @Query
    public List<Merchant> getMerchants() {

        List<Merchant> merchants = merchantBean.getMerchantsWithPrices();
        setProducts(merchants);
        return merchants;
    }

    @Query
    public Merchant getMerchant(@Name("Merchant_id") Integer id) {
        return merchantBean.getMerchants(id);
    }

    @Query
    public Merchant getMerchantPrices(@Name("Merchant_id") Integer id) {
        return merchantBean.getMerchants(id);
    }

    // MUTATIONS

    @Mutation
    public Merchant addNewMerchant(@Name("merchantName") String merchantName) {
        Merchant newMerchant = new Merchant();
        newMerchant.setName(merchantName);
        return merchantBean.createMerchants(newMerchant);
    }

    @Mutation
    public DeleteResponse deleteMerchant(@NotNull Integer id) {
        return new DeleteResponse(merchantBean.deleteMerchants(id));
    }

    // helper functions

    private void setProducts(List<Merchant> merchants) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(microserviceLocations.getProducts() + "/v1/products/"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            String content = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();

            if (content == null || content.equals("")) {
                return;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter();
            ArrayList<Product> products = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
            log.info(products.toString());

            for (Merchant merchant : merchants) {
                Set<Integer> merchantProducts = merchant.getPrices().stream().map(Price::getProductId).collect(Collectors.toSet());
                merchant.setProducts(products.stream().filter(p -> merchantProducts.contains(p.getProductId())).collect(Collectors.toSet()));
                for (Product product : merchant.getProducts()) {
                    Optional<Price> price = merchant.getPrices().stream().filter(p -> p.getProductId().equals(product.getProductId())).findFirst();
                    price.ifPresent(value -> product.setPrice(value.getPrice()));
                }
            }

        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, String.format("%s : Error while retrieving products. %s", this.getClass().getName(), e));
        }
    }
}
