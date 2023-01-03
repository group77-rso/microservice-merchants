package com.merchants.models.entities;

import javax.persistence.*;

@Entity
@Table(name = "price")
@NamedQueries(value =
        {
                @NamedQuery(name = "PriceEntity.getAll",
                        query = "SELECT p FROM PriceEntity p")
        })
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchantId", referencedColumnName = "id")
    private MerchantsEntity merchant;

    @Column(name = "productId")
    private Integer productId;

    @Column(name = "price")
    private float price;

    @Column(name = "link")
    private String link;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne
    public MerchantsEntity getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantsEntity merchant) {
        this.merchant = merchant;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}