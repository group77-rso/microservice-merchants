package com.merchants.api.v1.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.logs.cdi.Log;
import com.merchants.lib.Comparison;
import com.merchants.lib.Merchant;
import com.merchants.lib.Price;
import com.merchants.lib.Product;
import com.merchants.services.beans.MerchantBean;
import com.merchants.services.beans.PriceBean;
import com.merchants.services.config.MicroserviceLocations;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Log
@ApplicationScoped
@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CrossOrigin
public class MerchantsResource {

    private final Logger log = Logger.getLogger(MerchantsResource.class.getName());
    private Float conversion;

    @Inject
    private MerchantBean merchantBean;

    @Inject
    private PriceBean priceBean;

    @Inject
    private MicroserviceLocations microserviceLocations;

    @Context
    protected UriInfo uriInfo;

    @GET
    @Operation(description = "Gets a list of all merchants from DB.", summary = "Get all merchants")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of merchants",
                    content = @Content(schema = @Schema(implementation = Merchant.class, type = SchemaType.ARRAY),
                            example = "{\n" +
                                    "  \"logoUrl\": \"https://www.mercatorgroup.si/assets/Logotip-lezec/mercator-logotip-positive-lezeci.png\",\n" +
                                    "  \"merchantId\": 1001,\n" +
                                    "  \"name\": \"Mercator\"\n" +
                                    "}"),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list"),
                            @Header(name = "requestId", description = "Unique request id.")})})
    public Response getMerchants(@Parameter(hidden = true) @HeaderParam("requestId") String requestId) {

        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        List<Merchant> merchants = merchantBean.getMerchantsFilter(uriInfo);

        log.log(Level.INFO, String.format("Listing %d merchants", merchants.size()) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK)
                .entity(merchants)
                .header("X-Total-Count", merchants.size())
                .header("requestId", requestId)
                .build();
    }

    @POST
    @Operation(description = "Used to demonstrate asynchronous post call. Asynchronously calls an endpoint to create a new category.", summary = "Asynchronously creates new category")
    @Path("/async")
    public Response asynchronousPost(@Parameter(hidden = true) @HeaderParam("requestId") String requestId) {

        requestId = requestId != null ? requestId : UUID.randomUUID().toString();
        try {
            String requestBody = "{\n" +
                    "\"name\": \"Nova kategorija\"\n" +
                    "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(microserviceLocations.getProducts() + "/v1/categories"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient.newBuilder()
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(s -> log.log(Level.INFO, String.format("Post je nazaj! Dobili smo %s.", s)));

        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Post was unsuccessful because of %s.", e) + " - requestId: " + requestId);
        }
        return Response.status(Response.Status.OK).header("requestId", requestId).build();
    }


    @GET
    @Operation(description = "Method used to demonstrate asynchronous get call. Calls a slow method that will return a greeting.", summary = "Asynchronously calls a dummy method")
    @Path("/async")
    public Response asynchronousGet(@Parameter(hidden = true) @HeaderParam("requestId") String requestId) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(microserviceLocations.getProducts() + "/v1/products/slow"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            CompletableFuture<Void> response = HttpClient.newBuilder()
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(s -> log.log(Level.INFO, String.format("Get je nazaj! Dobili smo %s.", s)));

        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Post was unsuccessful because of %s.", e));
        }
        return Response.status(Response.Status.OK).header("requestId", requestId).build();
    }

    /**
     * Converts one currency to another
     *
     * @param haveCurrency a three letter currency code (like EUR, USD, RUB...) of a currenci we are converting FROM,
     * @param wantCurrency currency code of a currency we want,
     * @param amount       we want to convert.
     * @return converted value
     * ATTENTION: method uses class variable 'conversion'. Make sure to set it tu null after each conversion process!
     */
    private Float convertCurrency(String haveCurrency, String wantCurrency, String amount) {
        if (this.conversion == null) {
            try {
                // prepare url
                String specifics = String.format("?want=%s&have=%s&amount=1", wantCurrency, haveCurrency);
                URL url = new URL("https://api.api-ninjas.com/v1/convertcurrency" + specifics);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                InputStream responseStream = connection.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseStream);
                this.conversion = Float.valueOf(root.path("new_amount").asText());
                log.info(String.format("Converting prices to %s with convertion rate %.2f", wantCurrency, this.conversion));
            } catch (Exception e) {
                log.info(e.toString());
                return null;
            }
        }
        float convertedValue = Float.parseFloat(amount) * this.conversion;
        return Math.round(convertedValue * 100f) / 100f;
    }

    @GET
    @Operation(description = "Get information about a specific merchant. Including their products and prices.", summary = "Get merchant information")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "merchant details",
                    content = @Content(
                            schema = @Schema(implementation = Merchant.class))
            )})
    @Path("/{merchantId}")
    public Response getMerchant(@Parameter(hidden = true) @HeaderParam("requestId") String requestId,
                                @Parameter(description = "Merchant ID.", required = true, example = "1001") @PathParam("merchantId") Integer merchantId,
                                @Parameter(description = "Three letter currency code.", example = "RUB") @QueryParam("want") String wantCurrency) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();
        Merchant merchant = merchantBean.getMerchants(merchantId);

        if (merchant == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String content = callRestGet(microserviceLocations.getProducts() + "/v1/products/");
        if (content == null) {
            return null;
        }

        // Map objects that we got
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter();

        ArrayList<Product> products = new ArrayList<>();
        try {
            products = objectMapper.readValue(
                    content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));
        } catch (JsonProcessingException e) {
            log.log(Level.SEVERE, String.format("%s : Can not parse json to java object.", this.getClass().getName()) + " - requestId: " + requestId);
        }
        Set<Integer> productIdsForMerchant = merchant.getPrices().stream().map(Price::getProductId).collect(Collectors.toSet());
        Set<Product> productsForMerchant = products.stream().filter(p -> productIdsForMerchant.contains(p.getProductId())).collect(Collectors.toSet());

        productsForMerchant.forEach(product -> setPriceForProduct(product, merchant));

        // ce smo nastavili parameter za pretvarjanje valut
        if (wantCurrency != null) {
            productsForMerchant.forEach(product -> {
                Float newPrice = convertCurrency("EUR", wantCurrency, String.valueOf(product.getPrice()));
                product.setPrice(newPrice);
            });
            this.conversion = null;
        }

        merchant.setProducts(productsForMerchant);
        merchant.setPrices(null);  // ce je to zakomentirano (ali odstranjeno), vracamo do neke mere podvojene podatke

        log.info(String.format("Fetching merchant for id %d.", merchantId) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK).header("requestId", requestId).entity(merchant).build();
    }


    @GET
    @Operation(description = "Get prices for all merchants that have the selected product.", summary = "Get prices for product")
    @Path("/compareprices/{productId}")
    public Response compareMerchantPrices(@Parameter(hidden = true) @HeaderParam("requestId") String requestId,
                                          @Parameter(description = "Product ID.", required = true, example = "1001") @PathParam("productId") Integer productId,
                                          @Parameter(description = "Three letter currency code", example = "RUB") @QueryParam("want") String wantCurrency) throws JsonProcessingException {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        String content = callRestGet(microserviceLocations.getProducts() + "/v1/products/" + productId);
        if (content == null) {
            log.log(Level.WARNING, String.format("Product for id %d was not found.", productId) + " - requestId: " + requestId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Map json to object
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter();
        Product product = objectMapper.readValue(content, Product.class);

        // Poiscemo trgovce, ki imajo izbrani produkt
        Set<Price> pricesForProduct = priceBean.findPricesForProduct(productId);

        // ce smo nastavili parameter za pretvarjanje valut
        if (wantCurrency != null) {
            pricesForProduct.forEach(price -> {
                Float newPrice = convertCurrency("EUR", wantCurrency, String.valueOf(price));
                price.setPrice(newPrice);
            });
            this.conversion = null;
        }

        Comparison comparison = new Comparison();
        comparison.setProduct(product);
        comparison.setPrices(pricesForProduct);

        log.info(String.format("Comparing prices for product with id %s", productId) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK).header("requestId", requestId).entity(comparison).build();
    }

    /**
     * Functions that rends GET request and returns responses string
     *
     * @param urlString url that we want to call
     * @return response content as a string
     */
    private String callRestGet(String urlString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            String response = HttpClient.newBuilder()
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();

            return response;
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, String.format("Connecting %s was unsuccessful. Error %s occured.", urlString, e.getMessage()));
        }
        return "";
    }

    /**
     * Helper method searches DB for prices and sets them on Product object.
     *
     * @param product  product we want to set prices on
     * @param merchant merchant for which we want the price
     */
    private void setPriceForProduct(Product product, Merchant merchant) {
        Integer productId = product.getProductId();
        log.info(String.format("Searching DB for price of product %d for merchant %d", productId, merchant.getMerchantId()));
        Price price = priceBean.getPriceForProductAndMerchant(productId, merchant.getMerchantId());
        log.info("Found: " + price.toString());
        product.setPrice(price.getPrice());
        product.setProductLink(price.getProductLink());
    }

    @POST
    @Operation(description = "Add a merchant.", summary = "Add merchant")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Merchant successfully added."),
            @APIResponse(responseCode = "405", description = "Validation error .")
    })
    @RequestBody(description = "Details of the Item to be created", required = true,
            content = @Content(
                    schema = @Schema(implementation = Merchant.class),
                    examples = @ExampleObject(name = "Adding new merchant", value = "{\n" +
                            "    \"name\": \"Hofer\"\n" +
                            "}")))
    public Response createMerchant(@Parameter(hidden = true) @HeaderParam("requestId") String requestId, Merchant merchant) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        if (merchant.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            merchant = merchantBean.createMerchants(merchant);
        }

        log.info(String.format("New merchant with id %d was created.", merchant.getMerchantId()) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK).header("requestId", requestId).entity(merchant).build();
    }


    @PUT
    @Operation(description = "Update merchant information.", summary = "Update merchant")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Merchant successfully updated.")
    })
    @Path("{merchantId}")
    public Response putMerchant(@Parameter(hidden = true) @HeaderParam("requestId") String requestId,
                                @Parameter(description = "Merchant ID.", required = true, example = "1001") @PathParam("merchantId") Integer merchantId,
                                @RequestBody(
                                        description = "DTO object with merchant information.",
                                        required = true, content = @Content(
                                        schema = @Schema(implementation = Merchant.class)))
                                        Merchant merchant) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        merchant = merchantBean.putMerchants(merchantId, merchant);

        if (merchant == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.info(String.format("Merchant with id %d was updated.", merchantId) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK).header("requestId", requestId).build();
    }

    @DELETE
    @Operation(description = "Delete merchant from DB.", summary = "Delete merchant")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Merchant successfully deleted."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found."
            )
    })
    @Path("{merchantId}")
    public Response deleteMerchant(@Parameter(hidden = true) @HeaderParam("requestId") String requestId,
                                   @Parameter(description = "Merchant ID.", required = true, example = "1001") @PathParam("merchantId") Integer merchantId) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        boolean deleted = merchantBean.deleteMerchants(merchantId);

        log.info("Item deleted from DB" + " - requestId: " + requestId);
        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).header("requestId", requestId).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).header("requestId", requestId).build();
        }
    }

    @GET
    @Path("/ping")
    @Operation(description = "Simple ping method only to check if microservice is responding.", summary = "Ping")
    public Response ping() {
        return Response.status(Response.Status.OK).build();
    }

}
