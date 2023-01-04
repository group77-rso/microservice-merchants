package com.merchants.api.v1.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantsResource {

    private Logger log = Logger.getLogger(MerchantsResource.class.getName());
    private Float conversion;

    @Inject
    private MerchantBean merchantBean;

    @Inject
    private PriceBean priceBean;

    @Inject
    private MicroserviceLocations microserviceLocations;
    private static HttpURLConnection conn;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get all merchants.", summary = "Get all merchants")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of merchants",
                    content = @Content(schema = @Schema(implementation = Merchant.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getMerchants() {

        List<Merchant> merchants = merchantBean.getMerchantsFilter(uriInfo);
        log.log(Level.INFO, String.format("Listing %d merchants", merchants.size()));
        return Response.status(Response.Status.OK).entity(merchants).build();
    }

    @POST
    @Path("/async")
    public Response asynchronousPost() {

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
            log.log(Level.SEVERE, String.format("Post was unsuccessful because of %s.", e));
        }
        return Response.status(Response.Status.OK).build();
    }


    @GET
    @Path("/async")
    public Response asynchronousGet() {

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
        return Response.status(Response.Status.OK).build();
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


    @Operation(description = "Get metadata for an image.", summary = "Get metadata for an image")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Image metadata",
                    content = @Content(
                            schema = @Schema(implementation = Merchant.class))
            )})
    @GET
    @Path("/{merchantId}")
    public Response getMerchants(@Parameter(description = "Merchant ID.", required = true)
                                 @PathParam("merchantId") Integer merchantId,
                                 @QueryParam("want") String wantCurrency) {

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
            log.log(Level.SEVERE, String.format("%s : Can not parse json to java object.", this.getClass().getName()));
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


        return Response.status(Response.Status.OK).entity(merchant).build();
    }


    @GET
    @Path("/compareprices/{productId}")
    public Response compareMerchantPrices(@Parameter(description = "Product ID.", required = true)
                                          @PathParam("productId") Integer productId,
                                          @QueryParam("want") String wantCurrency) throws JsonProcessingException {

        String content = callRestGet(microserviceLocations.getProducts() + "/v1/products/" + productId);
        if (content == null) {
            log.log(Level.WARNING, String.format("Product for id %d was not found.", productId));
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

        return Response.status(Response.Status.OK).entity(comparison).build();
    }

    /**
     * Functions that rends GET request and returns responses string
     *
     * @param urlString url that we want to call
     * @return response content as a string
     */
    private String callRestGet(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();
            if (status > 300) {
                log.log(Level.SEVERE, String.format("Reaching %s failed with status %d.", url, status));
                conn.disconnect();
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("Connecting %s was unsuccessful. Error %s occured.", urlString, e.getMessage()));
        } finally {
            conn.disconnect();
        }
        return content.toString();
    }

    private void setPriceForProduct(Product product, Merchant merchant) {
        Integer productId = product.getProductId();
        log.info(String.format("Searching DB for price of product %d for merchant %d", productId, merchant.getMerchantId()));
        Price price = priceBean.getPriceForProductAndMerchant(productId, merchant.getMerchantId());
        log.info("Found: " + price.toString());
        product.setPrice(price.getPrice());
        product.setProductLink(price.getProductLink());
    }

    @Operation(description = "Add a merchant.", summary = "Add merchant")
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Merchant successfully added."
            ),
            @APIResponse(responseCode = "405", description = "Validation error .")
    })
    @POST
    public Response createMerchant(@RequestBody(
            description = "DTO object with merchants.",
            required = true, content = @Content(
            schema = @Schema(implementation = Merchant.class))) Merchant merchant) {

        if (merchant.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            merchant = merchantBean.createMerchants(merchant);
        }

        return Response.status(Response.Status.CONFLICT).entity(merchant).build();

    }


    @Operation(description = "Update metadata for an image.", summary = "Update metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Metadata successfully updated."
            )
    })
    @PUT
    @Path("{merchantId}")
    public Response putMerchant(@Parameter(description = "Metadata ID.", required = true)
                                @PathParam("merchantId") Integer merchantId,
                                @RequestBody(
                                        description = "DTO object with image metadata.",
                                        required = true, content = @Content(
                                        schema = @Schema(implementation = Merchant.class)))
                                        Merchant merchant) {

        merchant = merchantBean.putMerchants(merchantId, merchant);

        if (merchant == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.NOT_MODIFIED).build();

    }

    @Operation(description = "Delete metadata for an image.", summary = "Delete metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Metadata successfully deleted."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found."
            )
    })
    @DELETE
    @Path("{merchantId}")
    public Response deleteMerchant(@Parameter(description = "Metadata ID.", required = true)
                                   @PathParam("merchantId") Integer merchantId) {

        boolean deleted = merchantBean.deleteMerchants(merchantId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


}
