package com.example.products.api.v1.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.example.products.lib.Product;
import com.example.products.services.beans.ProductBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;



@ApplicationScoped
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductsResource {

    private Logger log = Logger.getLogger(ProductsResource.class.getName());

    @Inject
    private ProductBean productBean;


    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get all products.", summary = "Get all products")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of products",
                    content = @Content(schema = @Schema(implementation = Product.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getImageMetadata() {

        List<Product> imageMetadata = productBean.getProductsFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(imageMetadata).build();
    }


    @Operation(description = "Get metadata for an image.", summary = "Get metadata for an image")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Image metadata",
                    content = @Content(
                            schema = @Schema(implementation = Product.class))
            )})
    @GET
    @Path("/{imageMetadataId}")
    public Response getImageMetadata(@Parameter(description = "Metadata ID.", required = true)
                                     @PathParam("imageMetadataId") Integer imageMetadataId) {

        Product product = productBean.getProducts(imageMetadataId);

        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(product).build();
    }

    @Operation(description = "Add image metadata.", summary = "Add metadata")
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Metadata successfully added."
            ),
            @APIResponse(responseCode = "405", description = "Validation error .")
    })
    @POST
    public Response createImageMetadata(@RequestBody(
            description = "DTO object with image metadata.",
            required = true, content = @Content(
            schema = @Schema(implementation = Product.class))) Product product) {

        if (product.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else {
            product = productBean.createProduct(product);
        }

        return Response.status(Response.Status.CONFLICT).entity(product).build();

    }


    @Operation(description = "Update metadata for an image.", summary = "Update metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Metadata successfully updated."
            )
    })
    @PUT
    @Path("{imageMetadataId}")
    public Response putImageMetadata(@Parameter(description = "Metadata ID.", required = true)
                                     @PathParam("imageMetadataId") Integer imageMetadataId,
                                     @RequestBody(
                                             description = "DTO object with image metadata.",
                                             required = true, content = @Content(
                                             schema = @Schema(implementation = Product.class)))
                                     Product product){

        product = productBean.putProduct(imageMetadataId, product);

        if (product == null) {
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
    @Path("{imageMetadataId}")
    public Response deleteImageMetadata(@Parameter(description = "Metadata ID.", required = true)
                                        @PathParam("imageMetadataId") Integer imageMetadataId){

        boolean deleted = productBean.deleteProduct(imageMetadataId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }





}
