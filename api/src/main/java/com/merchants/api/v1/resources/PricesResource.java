package com.merchants.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import com.merchants.lib.Price;
import com.merchants.services.beans.PriceBean;
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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@Log
@ApplicationScoped
@Path("/prices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PricesResource {

    private Logger log = Logger.getLogger(PricesResource.class.getName());

    @Inject
    private PriceBean priceBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    @Operation(description = "Gets a list of all prices from DB.", summary = "Get all prices")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of prices",
                    content = @Content(schema = @Schema(implementation = Price.class, type = SchemaType.ARRAY),
                            example = "{\n" +
                                    "  \"logoUrl\": \"https://www.mercatorgroup.si/assets/Logotip-lezec/mercator-logotip-positive-lezeci.png\",\n" +
                                    "  \"merchantId\": 1001,\n" +
                                    "  \"name\": \"Mercator\"\n" +
                                    "}"),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list"),
                            @Header(name = "requestId", description = "Unique request id.")})})
    public Response getPrices(@Parameter(hidden = true) @HeaderParam("requestId") String requestId) {

        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        List<Price> prices = priceBean.getPrices();

        log.log(Level.INFO, String.format("Listing %d prices", prices.size()) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK)
                .entity(prices)
                .header("X-Total-Count", prices.size())
                .header("requestId", requestId)
                .build();
    }

    @PUT
    @Operation(description = "Update price information.", summary = "Update price")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Price successfully updated.")
    })
    @Path("{priceId}")
    public Response putMerchant(@Parameter(hidden = true) @HeaderParam("requestId") String requestId,
                                @Parameter(description = "Price ID.", required = true, example = "1001") @PathParam("priceId") Integer priceId,
                                @RequestBody(
                                        description = "DTO object with price information.",
                                        required = true, content = @Content(
                                        schema = @Schema(implementation = Price.class)))
                                        Price price) {
        requestId = requestId != null ? requestId : UUID.randomUUID().toString();

        price = priceBean.putPrices(priceId, price);

        if (price == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.info(String.format("Price with id %d was updated.", priceId) + " - requestId: " + requestId);
        return Response.status(Response.Status.OK).header("requestId", requestId).build();
    }

}
