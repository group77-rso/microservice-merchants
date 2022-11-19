package com.example.merchants.models.entities;

import javax.persistence.*;
import java.time.Instant;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return name;
    }

    public void setTitle(String name) {
        this.name = name;
    }
}