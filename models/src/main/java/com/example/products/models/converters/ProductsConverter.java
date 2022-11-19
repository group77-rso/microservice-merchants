package com.example.products.models.converters;

import com.example.products.lib.Product;
import com.example.products.models.entities.ProductEntity;

public class ProductsConverter {

    public static Product toDto(ProductEntity entity) {

        Product dto = new Product();
        dto.setProductId(entity.getId());
        dto.setName(entity.getTitle());

        return dto;

    }

    public static ProductEntity toEntity(Product dto) {

        ProductEntity entity = new ProductEntity();
        entity.setTitle(dto.getName());

        return entity;

    }

}
