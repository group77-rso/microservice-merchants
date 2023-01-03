package com.merchants.api.v1.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantsResource {

    private Logger log = Logger.getLogger(MerchantsResource.class.getName());

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
                                 @PathParam("merchantId") Integer merchantId) {

        Merchant merchant = merchantBean.getMerchants(merchantId);

        if (merchant == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            URL url = new URL(microserviceLocations.getProducts() + "/v1/products");
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);// 5000 milliseconds = 5 seconds
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            String inputLine;
            StringBuilder content = new StringBuilder();
            if (status > 300) {
                log.log(Level.SEVERE, String.format("Status %d", status));
                conn.disconnect();
                return null;
            }
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Map objects that we got
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter();

            ArrayList<Product> products = objectMapper.readValue(
                    content.toString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class));

            Set<Integer> productIdsForMerchant = merchant.getPrices().stream().map(Price::getProductId).collect(Collectors.toSet());
            Set<Product> productsForMerchant = products.stream().filter(p -> productIdsForMerchant.contains(p.getProductId())).collect(Collectors.toSet());

            productsForMerchant.forEach(product -> setPriceForProduct(product, merchant));
            merchant.setProducts(productsForMerchant);
            merchant.setPrices(null);  // ce je to zakomentirano (ali odstranjeno), vracamo do neke mere podvojene podatke
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("Finding products failed with error %s", e.getMessage()));
        } finally {
            conn.disconnect();
        }

        return Response.status(Response.Status.OK).entity(merchant).build();
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
