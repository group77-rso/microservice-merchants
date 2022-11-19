package com.example.products.models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products")
@NamedQueries(value =
        {
                @NamedQuery(name = "ProductEntity.getAll",
                        query = "SELECT prod FROM ProductEntity prod")
        })
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    private String title;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}