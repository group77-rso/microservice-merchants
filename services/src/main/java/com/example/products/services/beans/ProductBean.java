package com.example.products.services.beans;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;

import com.example.products.lib.Product;
import com.example.products.models.converters.ProductsConverter;
import com.example.products.models.entities.ProductEntity;


@RequestScoped
public class ProductBean {

    private Logger log = Logger.getLogger(ProductBean.class.getName());

    @Inject
    private EntityManager em;

    public List<Product> getProducts() {

        TypedQuery<ProductEntity> query = em.createNamedQuery(
                "ProductsEntity.getAll", ProductEntity.class);

        List<ProductEntity> resultList = query.getResultList();

        return resultList.stream().map(ProductsConverter::toDto).collect(Collectors.toList());

    }

    public List<Product> getProductsFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, ProductEntity.class, queryParameters).stream()
                .map(ProductsConverter::toDto).collect(Collectors.toList());
    }

    public Product getProducts(Integer id) {

        ProductEntity productEntity = em.find(ProductEntity.class, id);

        if (productEntity == null) {
            throw new NotFoundException();
        }

        Product product = ProductsConverter.toDto(productEntity);

        return product;
    }

    public Product createProduct(Product product) {

        ProductEntity productEntity = ProductsConverter.toEntity(product);

        try {
            beginTx();
            em.persist(productEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        if (productEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return ProductsConverter.toDto(productEntity);
    }

    public Product putProduct(Integer id, Product product) {

        ProductEntity c = em.find(ProductEntity.class, id);

        if (c == null) {
            return null;
        }

        ProductEntity updatedProductEntity = ProductsConverter.toEntity(product);

        try {
            beginTx();
            updatedProductEntity.setId(c.getId());
            updatedProductEntity = em.merge(updatedProductEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        return ProductsConverter.toDto(updatedProductEntity);
    }

    public boolean deleteProduct(Integer id) {

        ProductEntity product = em.find(ProductEntity.class, id);

        if (product != null) {
            try {
                beginTx();
                em.remove(product);
                commitTx();
            }
            catch (Exception e) {
                rollbackTx();
            }
        }
        else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
