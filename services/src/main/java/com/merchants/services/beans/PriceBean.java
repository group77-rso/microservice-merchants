package com.merchants.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.merchants.lib.Price;
import com.merchants.models.converters.PriceConverter;
import com.merchants.models.entities.PriceEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class PriceBean {

    private Logger log = Logger.getLogger(PriceBean.class.getName());

    @Inject
    @PersistenceContext
    private EntityManager em;

    public List<Price> getPrices() {

        TypedQuery<PriceEntity> query = em.createNamedQuery(
                "PriceEntity.getAll", PriceEntity.class);

        List<PriceEntity> resultList = query.getResultList();

        return resultList.stream().map(PriceConverter::toDto).collect(Collectors.toList());

    }

    public List<Price> getPricesFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, PriceEntity.class, queryParameters).stream()
                .map(PriceConverter::toDto).collect(Collectors.toList());
    }

    public Price getPrices(Integer id) {

        PriceEntity priceEntity = em.find(PriceEntity.class, id);

        if (priceEntity == null) {
            throw new NotFoundException();
        }

        Price price = PriceConverter.toDto(priceEntity);

        return price;
    }

    public Price getPriceForProductAndMerchant(Integer productId, Integer merchantId) {

        String sql = "SELECT p FROM PriceEntity p" +
                " LEFT JOIN FETCH p.merchant m" +
                " WHERE m.id = :merchantId" +
                " AND p.productId = :productId";

        PriceEntity result = em.createQuery(sql, PriceEntity.class)
                .setParameter("productId", productId)
                .setParameter("merchantId", merchantId)
                .getSingleResult();

        if (result == null) {
            throw new NotFoundException();
        }
        return PriceConverter.toDto(result);
    }

    public Price createPrices(Price price) {

        PriceEntity priceEntity = PriceConverter.toEntity(price);

        try {
            beginTx();
            em.persist(priceEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (priceEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return PriceConverter.toDto(priceEntity);
    }

    public Price putPrices(Integer id, Price price) {

        PriceEntity c = em.find(PriceEntity.class, id);

        if (c == null) {
            return null;
        }

        PriceEntity updatedPriceEntity = PriceConverter.toEntity(price);

        try {
            beginTx();
            updatedPriceEntity.setId(c.getId());
            updatedPriceEntity = em.merge(updatedPriceEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return PriceConverter.toDto(updatedPriceEntity);
    }

    public boolean deletePrices(Integer id) {

        PriceEntity price = em.find(PriceEntity.class, id);

        if (price != null) {
            try {
                beginTx();
                em.remove(price);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
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

    public Set<Price> findPricesForProduct(Integer productId) {

        String sql = "SELECT p FROM PriceEntity p" +
                " LEFT JOIN FETCH p.merchant m" +
                " WHERE p.productId = :productId";

        List<PriceEntity> result = em.createQuery(sql, PriceEntity.class)
                .setParameter("productId", productId)
                .getResultList();

        if (result == null) {
            throw new NotFoundException();
        }

        Set<Price> priceList = result.stream().map(PriceConverter::toDto).collect(Collectors.toSet());
        return priceList;
    }
}
