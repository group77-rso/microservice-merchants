package com.merchants.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import com.merchants.lib.Merchant;
import com.merchants.models.converters.MerchantsConverter;
import com.merchants.models.entities.MerchantsEntity;
import org.eclipse.microprofile.metrics.annotation.Counted;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class MerchantBean {

    private Logger log = Logger.getLogger(MerchantBean.class.getName());

    @Inject
    private EntityManager em;

    public List<Merchant> getMerchants() {

        TypedQuery<MerchantsEntity> query = em.createNamedQuery(
                "MerchantsEntity.getAll", MerchantsEntity.class);

        List<MerchantsEntity> resultList = query.getResultList();

        return resultList.stream().map(MerchantsConverter::toDto).collect(Collectors.toList());
    }

    @Counted(name = "getMerchantsWithPrices_counter")
    public List<Merchant> getMerchantsWithPrices() {

        String sql = "SELECT DISTINCT m FROM MerchantsEntity m " +
                "LEFT JOIN FETCH m.prices ps";
        List<MerchantsEntity> resultList = em.createQuery(sql, MerchantsEntity.class).getResultList();

        return resultList.stream().map(m -> MerchantsConverter.toDto(m, true)).collect(Collectors.toList());

    }

    public List<Merchant> getMerchantsFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, MerchantsEntity.class, queryParameters).stream()
                .map(MerchantsConverter::toDto).collect(Collectors.toList());
    }

    public Merchant getMerchants(Integer id) {

        MerchantsEntity merchantEntity = em.find(MerchantsEntity.class, id);

        if (merchantEntity == null) {
            throw new NotFoundException();
        }

        Merchant merchant = MerchantsConverter.toDto(merchantEntity, true);

        return merchant;
    }

    public Merchant createMerchants(Merchant merchant) {

        MerchantsEntity merchantEntity = MerchantsConverter.toEntity(merchant);

        try {
            beginTx();
            em.persist(merchantEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (merchantEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return MerchantsConverter.toDto(merchantEntity);
    }

    public Merchant putMerchants(Integer id, Merchant merchant) {

        MerchantsEntity c = em.find(MerchantsEntity.class, id);

        if (c == null) {
            return null;
        }

        MerchantsEntity updatedMerchantsEntity = MerchantsConverter.toEntity(merchant);

        try {
            beginTx();
            updatedMerchantsEntity.setId(c.getId());
            updatedMerchantsEntity = em.merge(updatedMerchantsEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return MerchantsConverter.toDto(updatedMerchantsEntity);
    }

    public boolean deleteMerchants(Integer id) {

        MerchantsEntity merchant = em.find(MerchantsEntity.class, id);

        if (merchant != null) {
            try {
                beginTx();
                em.remove(merchant);
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
}
