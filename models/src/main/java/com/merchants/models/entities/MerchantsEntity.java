package com.merchants.models.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merchants")
@NamedQueries(value =
        {
                @NamedQuery(name = "MerchantsEntity.getAll",
                        query = "SELECT m FROM MerchantsEntity m")
        })
public class MerchantsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "logourl")
    private String logoUrl;

    @OneToMany(mappedBy = "merchant", targetEntity = PriceEntity.class)
    private Set<PriceEntity> prices = new HashSet<>();

    public Set<PriceEntity> getPrices() {
        return prices;
    }

    public void setPrices(Set<PriceEntity> prices) {
        this.prices = prices;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}