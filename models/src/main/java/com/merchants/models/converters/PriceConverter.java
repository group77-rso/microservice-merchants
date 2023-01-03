package com.merchants.models.converters;

import com.merchants.lib.Price;
import com.merchants.models.entities.PriceEntity;

public class PriceConverter {


    public static Price toDto(PriceEntity entity) {

        Price dto = new Price();
        dto.setProductId(entity.getProductId());
        dto.setMerchant(entity.getMerchant().getName());
        dto.setPrice(entity.getPrice());
        dto.setProductLink(entity.getLink());

        return dto;
    }

    public static PriceEntity toEntity(Price price) {
        PriceEntity entity = new PriceEntity();
        entity.setPrice(price.getPrice());
        entity.setLink(price.getProductLink());
        entity.setProductId(price.getProductId());
        // todo: potrebno je se settati merchanta
        return entity;
    }
}
